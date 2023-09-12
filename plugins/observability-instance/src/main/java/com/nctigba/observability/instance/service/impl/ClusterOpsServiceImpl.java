/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.service.impl;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.nctigba.observability.instance.constants.MetricsLine;
import com.nctigba.observability.instance.dto.cluster.ClusterHealthState;
import com.nctigba.observability.instance.dto.cluster.ClusterStateDto;
import com.nctigba.observability.instance.dto.cluster.ClustersDto;
import com.nctigba.observability.instance.dto.cluster.NodeAndCompDto;
import com.nctigba.observability.instance.dto.cluster.NodeRelationDto;
import com.nctigba.observability.instance.dto.cluster.SyncSituation;
import com.nctigba.observability.instance.dto.cluster.SyncSituationDto;
import com.nctigba.observability.instance.exception.InstanceException;
import com.nctigba.observability.instance.mapper.ClustersMapper;
import com.nctigba.observability.instance.service.ClusterManager;
import com.nctigba.observability.instance.service.ClusterOpsService;
import com.nctigba.observability.instance.service.MetricsService;
import com.nctigba.observability.instance.util.SshSession;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.opengauss.admin.common.enums.ops.DeployTypeEnum;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.plugin.facade.HostUserFacade;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static com.nctigba.observability.instance.dto.cluster.NodeRelationDto.getDefaultRelationDto;
import static com.nctigba.observability.instance.dto.cluster.NodeRelationDto.setNodeState;
import static com.nctigba.observability.instance.dto.cluster.SyncSituationDto.getDefaultSituationDto;

/**
 * ClusterOpsServiceImpl
 *
 * @author liupengfei
 * @since 2023/8/11
 */
@Service
@Slf4j
public class ClusterOpsServiceImpl implements ClusterOpsService {
    private static final MetricsLine[] CLUSTER_METRICS = {
            MetricsLine.CPU,
            MetricsLine.MEMORY,
            MetricsLine.IOPS_R_TOTAL,
            MetricsLine.NETWORK_IN_TOTAL,
            MetricsLine.NETWORK_OUT_TOTAL,
            MetricsLine.INSTANCE_QPS,
            MetricsLine.INSTANCE_DB_RESPONSETIME_P80,
            MetricsLine.INSTANCE_DB_RESPONSETIME_P95,
            MetricsLine.CLUSTER_PRIMARY_WAL_SEND_PRESSURE,
            MetricsLine.CLUSTER_PRIMARY_WAL_WRITE_PER_SEC,
            MetricsLine.CLUSTER_WAL_RECEIVED_DELAY,
            MetricsLine.CLUSTER_WAL_WRITE_DELAY,
            MetricsLine.CLUSTER_WAL_REPLAY_DELAY
    };
    private static final long CACHE_TIMEOUT = 15000L;
    private static final long CACHE_CLEAN_INTERVAL = 60000L;
    private static final TimedCache<String, ClusterHealthState> CLUSTER_STATE_CACHE;

    static {
        CLUSTER_STATE_CACHE = CacheUtil.newTimedCache(CACHE_TIMEOUT);
        CLUSTER_STATE_CACHE.schedulePrune(CACHE_CLEAN_INTERVAL);
    }

    @Autowired
    private MetricsService metricsService;
    @Autowired
    private ClusterManager clusterManager;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostFacade hostFacade;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostUserFacade hostUserFacade;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private EncryptionUtils encryptionUtils;
    @Autowired
    private ClustersMapper clustersMapper;

    @Override
    public List<ClustersDto> listClusters() {
        List<ClustersDto> list = new ArrayList<>();
        List<OpsClusterVO> clusters = clusterManager.getAllOpsCluster();
        for (OpsClusterVO cluster : clusters) {
            if (!DeployTypeEnum.CLUSTER.equals(cluster.getDeployType())) {
                continue;
            }
            ClustersDto clustersDto = ClustersDto.of(cluster);
            list.add(clustersDto);
        }
        return list;
    }

    @Override
    public List<JSONObject> nodes(String clusterId) {
        OpsClusterVO cluster = getOpsClusterVOById(clusterId);
        ClusterHealthState stateCache = getClusterHealthState(clusterId, cluster);
        List<OpsClusterNodeVO> clusterNodes = cluster.getClusterNodes();
        List<SyncSituationDto> nodeSyncSituationDtoList = new CopyOnWriteArrayList<>();

        // get standby nodes
        getClusterStandbyNodes(nodeSyncSituationDtoList, cluster);

        // check
        checkPrimaryNode(cluster, stateCache, nodeSyncSituationDtoList);

        // get cm、om state
        HashMap<String, Map<String, String>> cmAndOmState = getCmAndOmState(clusterNodes);
        return nodeSyncSituationDtoList.stream().map(syn -> {
            NodeAndCompDto com = new NodeAndCompDto();
            com.setCmAgentState(
                    cmAndOmState.get("cmAgent").getOrDefault(syn.getHostIp(), NodeAndCompDto.BinState.STOP.getCode()));
            com.setCmServerState(
                    cmAndOmState.get("cmServer").getOrDefault(syn.getHostIp(), NodeAndCompDto.BinState.STOP.getCode()));
            com.setOmMonitorState(cmAndOmState.get("omMonitor").getOrDefault(syn.getHostIp(),
                    NodeAndCompDto.BinState.STOP.getCode()));
            JSONObject jsonObject = new JSONObject();
            jsonObject.putAll(BeanUtil.beanToMap(syn));
            jsonObject.putAll(BeanUtil.beanToMap(com));
            jsonObject.remove("walSyncState");
            return jsonObject;
        }).collect(Collectors.toList());
    }

    private HashMap<String, Map<String, String>> getCmAndOmState(List<OpsClusterNodeVO> clusterNodes) {
        ConcurrentHashMap<String, String> cmServer = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, String> cmAgent = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, String> omMonitor = new ConcurrentHashMap<>();
        CountDownLatch countDownLatch = ThreadUtil.newCountDownLatch(clusterNodes.size());
        for (OpsClusterNodeVO clusterNode : clusterNodes) {
            ThreadUtil.execute(() -> {
                try {
                    execute(cmServer, cmAgent, omMonitor, clusterNode);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        HashMap<String, Map<String, String>> map = new HashMap<>();
        map.put("cmServer", cmServer);
        map.put("cmAgent", cmAgent);
        map.put("omMonitor", omMonitor);
        return map;
    }

    private void execute(Map<String, String> cmServer, Map<String, String> cmAgent, Map<String, String> omMonitor,
                         OpsClusterNodeVO clusterNode) {
        SshSession sshSession = null;
        try {
            sshSession = getSshSession(clusterNode);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        if (sshSession == null) {
            cmServer.put(clusterNode.getPublicIp(), "Unknown");
            cmAgent.put(clusterNode.getPublicIp(), "Unknown");
            omMonitor.put(clusterNode.getPublicIp(), "Unknown");
            return;
        }
        String executeRes = null;
        try {
            executeRes = sshSession.execute("ps -aux | grep \"cm_agent\\|cm_server\\|om_monitor\" | grep -v grep");
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        if (executeRes == null) {
            cmServer.put(clusterNode.getPublicIp(), "Unknown");
            cmAgent.put(clusterNode.getPublicIp(), "Unknown");
            omMonitor.put(clusterNode.getPublicIp(), "Unknown");
            return;
        }
        String[] binMessage = executeRes.split(StrUtil.LF);
        for (String bin : binMessage) {
            String[] s = bin.replaceAll(" +", " ").split(" ");
            if (bin.contains("cm_agent")) {
                cmAgent.put(clusterNode.getPublicIp(), s[7]);
                continue;
            }
            if (bin.contains("cm_server")) {
                cmServer.put(clusterNode.getPublicIp(), s[7]);
                continue;
            }
            if (bin.contains("om_monitor")) {
                omMonitor.put(clusterNode.getPublicIp(), s[7]);
            }
        }
    }

    private static void checkPrimaryNode(OpsClusterVO cluster, ClusterHealthState stateCache,
                                         List<SyncSituationDto> retList) {
        if (retList.size() == cluster.getClusterNodes().size()) {
            return;
        }
        Set<String> standbySet = retList.stream().map(SyncSituationDto::getHostIp).collect(Collectors.toSet());
        for (OpsClusterNodeVO clusterNode : cluster.getClusterNodes()) {
            if (!standbySet.contains(clusterNode.getPublicIp())) {
                SyncSituationDto dto = new SyncSituationDto();
                dto.setClusterId(cluster.getClusterId());
                dto.setNodeId(clusterNode.getNodeId());
                dto.setHostIp(clusterNode.getPublicIp());
                dto.setNodeName(stateCache.getNodeName().get(clusterNode.getPublicIp()));
                dto.setLocalAddr(clusterNode.getPublicIp() + ":" + clusterNode.getDbPort());
                dto.setRole(stateCache.getNodeRole().getOrDefault(clusterNode.getPublicIp(), "Unknown"));
                dto.setNodeState(stateCache.getNodeState().getOrDefault(clusterNode.getPublicIp(), "Unknown"));
                retList.add(0, dto);
            }
        }
    }

    @NotNull
    private OpsClusterVO getOpsClusterVOById(String clusterId) {
        Optional<OpsClusterVO> clusterVOOptional = clusterManager.getAllOpsCluster().stream()
                .filter(clusterVO -> clusterVO.getClusterId().equals(clusterId)).findFirst();
        if (clusterVOOptional.isEmpty()) {
            throw new InstanceException("The cluster does not exist");
        }
        return clusterVOOptional.get();
    }

    @Override
    public List<NodeRelationDto> relation(String clusterId) {
        OpsClusterVO cluster = getOpsClusterVOById(clusterId);
        ClusterHealthState stateCache = getClusterHealthState(clusterId, cluster);
        List<OpsClusterNodeVO> clusterNodes = cluster.getClusterNodes();
        ArrayList<NodeRelationDto> retList = new ArrayList<>();
        HashSet<String> allAliveNodeIpSet = new HashSet<>();
        for (OpsClusterNodeVO node : clusterNodes) {
            if ("Primary".equalsIgnoreCase(stateCache.getNodeRole().get(node.getPublicIp()))) {
                List<NodeRelationDto> standbyList = clustersMapper.relation(node.getNodeId());
                standbyList.forEach(standby -> setNodeState(standby, stateCache));
                NodeRelationDto primary = getDefaultRelationDto(stateCache, node, standbyList);
                retList.add(primary);
                allAliveNodeIpSet
                        .addAll(standbyList.stream().map(NodeRelationDto::getHostIp).collect(Collectors.toList()));
                allAliveNodeIpSet.add(primary.getHostIp());
            }
        }

        // check
        if (allAliveNodeIpSet.size() != clusterNodes.size()) {
            for (OpsClusterNodeVO node : clusterNodes) {
                if (!allAliveNodeIpSet.contains(node.getPublicIp())) {
                    retList.add(getDefaultRelationDto(stateCache, node, new ArrayList<>()));
                }
            }
        }
        return retList;
    }

    @Override
    public List<ClusterStateDto> allClusterState() {
        ArrayList<ClusterStateDto> list = new ArrayList<>();
        List<OpsClusterVO> clusters = clusterManager.getAllOpsCluster().stream()
                .filter(cluster -> DeployTypeEnum.CLUSTER.equals(cluster.getDeployType())).collect(Collectors.toList());
        refreshAllClusterState(clusters);
        clusters.forEach(cluster -> {
            ClusterHealthState stateCache = CLUSTER_STATE_CACHE.get(cluster.getClusterId(), false);
            list.add(ClusterStateDto.of(cluster, stateCache));
        });
        return list;
    }

    private void refreshAllClusterState(List<OpsClusterVO> clusters) {
        CountDownLatch countDown = ThreadUtil.newCountDownLatch(clusters.size());
        for (OpsClusterVO cluster : clusters) {
            if (!DeployTypeEnum.CLUSTER.equals(cluster.getDeployType())) {
                continue;
            }
            ThreadUtil.execute(() -> {
                try {
                    if (!CLUSTER_STATE_CACHE.containsKey(cluster.getClusterId())) {
                        refreshHealthStateCache(cluster);
                    }
                } finally {
                    countDown.countDown();
                }
            });
        }
        try {
            countDown.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new InstanceException(e.getMessage(), e);
        }
    }

    private ClusterHealthState getClusterHealthState(String clusterId, OpsClusterVO cluster) {
        if (!CLUSTER_STATE_CACHE.containsKey(cluster.getClusterId())) {
            refreshHealthStateCache(cluster);
        }
        return CLUSTER_STATE_CACHE.get(clusterId, false);
    }

    @Override
    public List<SyncSituationDto> allStandbyNodes() {
        List<OpsClusterVO> clusters = clusterManager.getAllOpsCluster().stream()
                .filter(cluster -> DeployTypeEnum.CLUSTER.equals(cluster.getDeployType())).collect(Collectors.toList());
        refreshAllClusterState(clusters);
        CopyOnWriteArrayList<SyncSituationDto> retList = new CopyOnWriteArrayList<>();
        CountDownLatch countDownLatch = ThreadUtil.newCountDownLatch(clusters.size());
        for (OpsClusterVO cluster : clusters) {
            ThreadUtil.execute(() -> {
                try {
                    getClusterStandbyNodes(retList, cluster);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        try {
            countDownLatch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new InstanceException(e.getMessage(), e);
        }
        return retList;
    }

    @Override
    public Map<String, Object> clusterMetrics(String clusterId, Long start, Long end, Integer step) {
        HashMap<String, Object> metricsNodes = new HashMap<>();
        OpsClusterVO opsClusterVOById = getOpsClusterVOById(clusterId);
        List<OpsClusterNodeVO> clusterNodes = opsClusterVOById.getClusterNodes();
        List<Pair<String, Future<Map<String, Object>>>> futureList = clusterNodes.stream()
                .map(node -> new Pair<>(node.getNodeId(),
                        ThreadUtil.execAsync(
                                () -> metricsService.listBatch(CLUSTER_METRICS, node.getNodeId(), start, end, step))))
                .collect(Collectors.toList());
        queryPrimaryWalTotalIncrease(end, metricsNodes, clusterNodes);
        for (Pair<String, Future<Map<String, Object>>> future : futureList) {
            Map<String, Object> nodeRes;
            try {
                nodeRes = future.getValue().get(5, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.error(e.getMessage(), e);
                continue;
            }
            metricsNodes.put("time", nodeRes.get("time"));
            nodeRes.remove("time");
            getMetricsNodes(metricsNodes, future, nodeRes);
        }
        return metricsNodes;
    }

    private void queryPrimaryWalTotalIncrease(Long end, HashMap<String, Object> metricsNodes,
                                              List<OpsClusterNodeVO> clusterNodes) {
        LocalDateTime currentDate = LocalDateTime.ofInstant(Instant.ofEpochSecond(end), ZoneId.systemDefault());
        Long startOfTheDay = currentDate.toLocalDate().atStartOfDay().minusSeconds(1)
                .toEpochSecond(ZoneOffset.of("+8"));
        Long fiveDaysAgo = LocalDateTime.of(currentDate.toLocalDate().minusDays(5), LocalTime.of(23, 59, 59))
                .toEpochSecond(ZoneOffset.of("+8"));
        List<Pair<String, Future<Map<String, Object>>>> historyPrimaryWalWriteTotalFutureList = clusterNodes.stream()
                .map(node -> new Pair<>(node.getNodeId(),
                        ThreadUtil.execAsync(() -> metricsService.listBatch(new MetricsLine[]{
                                MetricsLine.CLUSTER_PRIMARY_WAL_WRITE_TOTAL
                        }, node.getNodeId(), fiveDaysAgo, startOfTheDay, 60 * 60 * 24)))).collect(Collectors.toList());
        List<Pair<String, Future<Map<String, Object>>>> todayPrimaryWalWriteTotalFutureList = clusterNodes.stream()
                .map(node -> new Pair<>(node.getNodeId(),
                        ThreadUtil.execAsync(() -> metricsService.listBatch(new MetricsLine[]{
                                MetricsLine.CLUSTER_PRIMARY_WAL_WRITE_TOTAL
                        }, node.getNodeId(), startOfTheDay, end, (int) (end - startOfTheDay)))))
                .collect(Collectors.toList());
        List<Date> historyTimeLine = new ArrayList<>();
        getHistoryResult(metricsNodes, historyPrimaryWalWriteTotalFutureList, historyTimeLine);
        getTodayResult(metricsNodes, todayPrimaryWalWriteTotalFutureList, historyTimeLine);
        Collections.sort(historyTimeLine);
        metricsNodes.put("CLUSTER_PRIMARY_WAL_WRITE_TOTAL_TIME", historyTimeLine);
    }

    @SuppressWarnings("unchecked")
    private static void getTodayResult(HashMap<String, Object> metricsNodes,
                                       List<Pair<String, Future<Map<String, Object>>>> futureList, List<Date> time) {
        List<Date> todayTimeLine = new ArrayList<>();
        for (Pair<String, Future<Map<String, Object>>> futurePair : futureList) {
            Map<String, Object> today = null;
            try {
                today = futurePair.getValue().get(5, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.error(e.getMessage(), e);
                continue;
            }
            todayTimeLine = (List<Date>) today.get("time");
            for (Date aLong : todayTimeLine) {
                if (!time.contains(aLong)) {
                    time.add(aLong);
                }
            }
            log.info("...........{}", today.entrySet());
            for (Map.Entry<String, Object> nodeMetrics : today.entrySet()) {
                if (!metricsNodes.containsKey(nodeMetrics.getKey())) {
                    metricsNodes.put(nodeMetrics.getKey(), MapUtil.of(futurePair.getKey(), nodeMetrics.getValue()));
                    continue;
                }
                Map<String, Object> metricsNodesValue = (Map<String, Object>) metricsNodes.get(nodeMetrics.getKey());
                List<Object> valueList = (List<Object>) metricsNodesValue.get(futurePair.getKey());
                valueList.addAll((Collection<?>) nodeMetrics.getValue());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void getHistoryResult(HashMap<String, Object> metricsNodes,
                                         List<Pair<String, Future<Map<String, Object>>>> futureList, List<Date> time) {
        for (Pair<String, Future<Map<String, Object>>> futurePair : futureList) {
            Map<String, Object> history = null;
            try {
                history = futurePair.getValue().get(5, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.error(e.getMessage(), e);
                continue;
            }
            List<Date> timeLine = (List<Date>) history.get("time");
            for (Date aLong : timeLine) {
                if (!time.contains(aLong)) {
                    time.add(aLong);
                }
            }
            getMetricsNodes(metricsNodes, futurePair, history);
        }
    }

    private static void getMetricsNodes(HashMap<String, Object> metricsNodes,
                                        Pair<String, Future<Map<String, Object>>> future, Map<String, Object> nodeRes) {
        for (Map.Entry<String, Object> nodeMetrics : nodeRes.entrySet()) {
            if (!metricsNodes.containsKey(nodeMetrics.getKey())) {
                metricsNodes.put(nodeMetrics.getKey(), MapUtil.of(future.getKey(), nodeMetrics.getValue()));
                continue;
            }
            Map<String, Object> metricsNodesValue = (Map<String, Object>) metricsNodes.get(nodeMetrics.getKey());
            metricsNodesValue.put(future.getKey(), nodeMetrics.getValue());
        }
    }

    private void getClusterStandbyNodes(List<SyncSituationDto> retList, OpsClusterVO cluster) {
        ClusterHealthState stateCache = CLUSTER_STATE_CACHE.get(cluster.getClusterId(), false);
        List<OpsClusterNodeVO> clusterNodes = cluster.getClusterNodes();
        List<SyncSituationDto> standbyList = new ArrayList<>();
        for (OpsClusterNodeVO clusterNode : clusterNodes) {
            if ("Primary".equalsIgnoreCase(stateCache.getNodeRole().get(clusterNode.getPublicIp()))) {
                List<SyncSituation> syncSituations = clustersMapper.getSyncSituation(clusterNode.getNodeId());
                syncSituations.forEach(s -> {
                    SyncSituationDto standby = getDefaultSituationDto(s, clusterNodes, cluster.getClusterId());
                    standby.setNodeName(stateCache.getNodeName().get(standby.getHostIp()));
                    standby.setRole(stateCache.getNodeRole().get(standby.getHostIp()));
                    standby.setNodeState(stateCache.getNodeState().get(standby.getHostIp()));
                    standby.setPrimaryAddr(clusterNode.getPublicIp() + ":" + clusterNode.getDbPort());
                    standby.setLocalAddr(standby.getHostIp() + ":" + clusterNode.getDbPort());
                    standbyList.add(standby);
                });
                break;
            }
        }
        checkStandbyNode(cluster, stateCache, standbyList);
        retList.addAll(standbyList);
    }

    private static void checkStandbyNode(OpsClusterVO cluster, ClusterHealthState stateCache,
                                         List<SyncSituationDto> standbyList) {
        // check, the cluster might not have a primary node (pending...) or disconnect
        ArrayList<String> allNodeIp = new ArrayList<>();
        for (Map.Entry<String, String> entry : stateCache.getNodeRole().entrySet()) {
            if (!"Primary".equals(entry.getValue())) {
                allNodeIp.add(entry.getKey());
            }
        }
        if (standbyList.isEmpty()) {
            for (OpsClusterNodeVO clusterNode : cluster.getClusterNodes()) {
                SyncSituationDto dto = new SyncSituationDto();
                dto.setClusterId(cluster.getClusterId());
                dto.setNodeId(clusterNode.getNodeId());
                dto.setHostIp(clusterNode.getPublicIp());
                dto.setNodeName(clusterNode.getHostname());
                dto.setLocalAddr(clusterNode.getPublicIp() + ":" + clusterNode.getHostPort());
                dto.setRole(stateCache.getNodeRole().getOrDefault(clusterNode.getPublicIp(), "Unknown"));
                dto.setNodeState("Unknown");
                standbyList.add(dto);
            }
            return;
        }
        if (standbyList.size() != allNodeIp.size()) {
            Set<String> standbyIps = standbyList.stream().map(SyncSituationDto::getHostIp).collect(Collectors.toSet());
            for (String ip : allNodeIp) {
                if (!standbyIps.contains(ip)) {
                    SyncSituationDto dto = new SyncSituationDto();
                    dto.setClusterId(cluster.getClusterId());
                    dto.setNodeId(cluster.getClusterNodes().stream().filter(node -> node.getPublicIp().equals(ip))
                            .findFirst().get().getNodeId());
                    dto.setNodeName(stateCache.getNodeName().get(ip));
                    dto.setLocalAddr(ip + ":" + cluster.getClusterNodes().get(0).getDbPort());
                    dto.setRole(stateCache.getNodeRole().get(ip));
                    dto.setNodeState(stateCache.getNodeState().get(ip));
                    standbyList.add(dto);
                }
            }
        }
    }

    private void refreshHealthStateCache(OpsClusterVO cluster) {
        List<OpsClusterNodeVO> clusterNodes = cluster.getClusterNodes();
        SshSession sshSession = null;

        // Select a host that you can connect to
        for (OpsClusterNodeVO clusterNode : clusterNodes) {
            try {
                sshSession = getSshSession(clusterNode);
                break;
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        if (sshSession == null) {
            log.error("Failed to connect cluster[{}]", cluster.getClusterId());
            CLUSTER_STATE_CACHE.put(cluster.getClusterId(), new ClusterHealthState());
            return;
        }
        ClusterHealthState healthState = getHealthState(sshSession, cluster.getEnvPath());
        CLUSTER_STATE_CACHE.put(cluster.getClusterId(), healthState);
    }

    private SshSession getSshSession(OpsClusterNodeVO clusterNode) throws IOException {
        OpsHostEntity hostEntity = hostFacade.getById(clusterNode.getHostId());
        OpsHostUserEntity userEntity = hostUserFacade.listHostUserByHostId(clusterNode.getHostId()).stream()
                .filter(p -> p.getUsername().equals(clusterNode.getInstallUserName())).findFirst().orElseThrow(
                        () -> new InstanceException("The node information corresponding to the host is not found"));
        return SshSession.getSession(hostEntity.getPublicIp(), hostEntity.getPort(), userEntity.getUsername(),
                encryptionUtils.decrypt(userEntity.getPassword()));
    }

    private ClusterHealthState getHealthState(SshSession sshSession, String envPath) {
        String command = "source " + envPath + " && gs_om -t status --detail";
        String result = null;
        try {
            result = sshSession.execute(command);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        if (result == null) {
            return new ClusterHealthState();
        }
        ClusterHealthState res = new ClusterHealthState();
        Map<String, String> cmState = new HashMap<>(1);
        int cmIndex = result.indexOf("CMServer State");
        if (cmIndex > 0) {
            cmState(result, cmState, cmIndex);
        }
        res.setCmState(cmState);
        int clusterStateIndex = result.indexOf("cluster_state");
        String clusterState = null;
        if (clusterStateIndex > 0) {
            int splitIndex = result.indexOf(":", clusterStateIndex);
            int lineEndIndex = result.indexOf(StrUtil.LF, clusterStateIndex);
            clusterState = result.substring(splitIndex + 1, lineEndIndex).trim();
        }
        res.setClusterState(clusterState);
        int datanodeStateIndex = result.indexOf("Datanode State");
        if (datanodeStateIndex > 0) {
            nodeState(result, datanodeStateIndex, res);
        }
        return res;
    }

    private static void cmState(String result, Map<String, String> cmState, int cmIndex) {
        int splitIndex = result.indexOf("------------------", cmIndex);
        String dataNodeStateStr = result.substring(splitIndex);
        String[] dataNode = dataNodeStateStr.split(StrUtil.LF);
        for (String s : dataNode) {
            String[] s1 = s.replaceAll(" +", " ").split(" ");
            if (s1.length == 6) {
                cmState.put(s1[2], s1[5].trim());
            }
        }
    }

    private static void nodeState(String result, int datanodeStateIndex, ClusterHealthState res) {
        Map<String, String> nodeState = new HashMap<>(1);
        Map<String, String> nodeRole = new HashMap<>(1);
        Map<String, String> nodeName = new HashMap<>(1);
        int splitIndex = result.indexOf("------------------", datanodeStateIndex);
        String dataNodeStateStr = result.substring(splitIndex);
        String[] dataNode = dataNodeStateStr.split(StrUtil.LF);
        for (String s : dataNode) {
            String[] s1 = s.replaceAll(" +", " ").split(" ");
            String state = "";
            String role = "";
            if (s1.length == 1) {
                continue;
            }
            if (s1.length >= 9) {
                for (int i = 8; i < s1.length; i++) {
                    state += (s1[i] + " ");
                }
                role = s1[7];
            }
            if (s1.length == 8) {
                state = s1[7];
                role = s1[6];
            }
            nodeState.put(s1[2], state.trim());
            nodeRole.put(s1[2], role);
            nodeName.put(s1[2], s1[1]);
        }
        res.setNodeState(nodeState);
        res.setNodeRole(nodeRole);
        res.setNodeName(nodeName);
    }
}
