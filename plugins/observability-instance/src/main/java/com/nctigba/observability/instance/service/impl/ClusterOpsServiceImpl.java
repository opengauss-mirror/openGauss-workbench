/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  ClusterOpsServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/
 *  src/main/java/com/nctigba/observability/instance/service/impl/ClusterOpsServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.service.impl;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.nctigba.observability.instance.enums.MetricsLine;
import com.nctigba.observability.instance.model.dto.cluster.ClusterHealthStateDTO;
import com.nctigba.observability.instance.model.dto.cluster.ClusterStateDTO;
import com.nctigba.observability.instance.model.dto.cluster.ClusterStatisticsDTO;
import com.nctigba.observability.instance.model.dto.cluster.ClustersDTO;
import com.nctigba.observability.instance.model.dto.cluster.NodeAndCompDTO;
import com.nctigba.observability.instance.model.dto.cluster.NodeRelationDTO;
import com.nctigba.observability.instance.model.dto.cluster.StateDTO;
import com.nctigba.observability.instance.model.dto.cluster.SwitchRecordDTO;
import com.nctigba.observability.instance.model.dto.cluster.SyncSituationDelayDTO;
import com.nctigba.observability.instance.model.dto.cluster.SyncSituationDTO;
import com.nctigba.observability.instance.exception.InstanceException;
import com.nctigba.observability.instance.mapper.ClusterSwitchoverLogReadMapper;
import com.nctigba.observability.instance.mapper.ClusterSwitchoverRecordMapper;
import com.nctigba.observability.instance.mapper.ClustersMapper;
import com.nctigba.observability.instance.model.entity.ClusterSwitchoverLogReadDO;
import com.nctigba.observability.instance.model.entity.ClusterSwitchoverRecordDO;
import com.nctigba.observability.instance.service.ClusterManager;
import com.nctigba.observability.instance.service.ClusterOpsService;
import com.nctigba.observability.instance.service.ClusterSwitchService;
import com.nctigba.observability.instance.service.MetricsService;
import com.nctigba.observability.instance.util.MessageSourceUtils;
import com.nctigba.observability.instance.util.SshSessionUtils;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.opengauss.admin.common.enums.ops.DeployTypeEnum;
import org.opengauss.admin.common.exception.CustomException;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.plugin.facade.HostUserFacade;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.text.MessageFormat;
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

import static com.nctigba.observability.instance.model.dto.cluster.NodeRelationDTO.getDefaultRelationDto;
import static com.nctigba.observability.instance.model.dto.cluster.SyncSituationDTO.getDefaultSituationDto;

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
    private static final String[] CLUSTER_STATES = {
            "cluster.state.value.Normal",
            "cluster.state.value.Unavailable",
            "cluster.state.value.Degraded",
            "cluster.state.value.Unknown"
    };
    private static final String[] NODE_STATES = {
            "cluster.node.state.Normal",
            "cluster.node.state.Exception"
    };
    private static final String[] NODE_SYNC_STATES = {
            "cluster.node.syncState.Streaming",
            "cluster.node.syncState.Catchup",
            "cluster.node.syncState.Delay",
            "cluster.node.syncState.Unknown"
    };
    private static final long CACHE_TIMEOUT = 15000L;
    private static final long CACHE_CLEAN_INTERVAL = 60000L;
    private static final TimedCache<String, ClusterHealthStateDTO> CLUSTER_STATE_CACHE;

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
    @Autowired
    private ClusterSwitchoverLogReadMapper switchoverLogReadMapper;
    @Autowired
    private ClusterSwitchoverRecordMapper switchoverRecordMapper;
    @Autowired
    private ClusterSwitchService clusterSwitchService;

    @Override
    public List<ClustersDTO> listClusters() {
        List<ClustersDTO> list = new ArrayList<>();
        List<OpsClusterVO> clusters = clusterManager.getAllOpsCluster();
        for (OpsClusterVO cluster : clusters) {
            if (!DeployTypeEnum.CLUSTER.equals(cluster.getDeployType())) {
                continue;
            }
            ClustersDTO clustersDto = ClustersDTO.of(cluster);
            list.add(clustersDto);
        }
        return list;
    }

    @Override
    public JSONObject nodes(String clusterId) {
        OpsClusterVO cluster = clusterManager.getOpsClusterVOById(clusterId);
        ClusterHealthStateDTO stateCache = getClusterHealthState(clusterId, cluster);
        List<SyncSituationDTO> nodeSyncSituationDTOList = new CopyOnWriteArrayList<>();
        // get standby nodes
        if (cluster.getClusterNodes().size() != 1) {
            getClusterStandbyNodes(nodeSyncSituationDTOList, cluster);
        }
        // check
        checkPrimaryNode(cluster, stateCache, nodeSyncSituationDTOList);
        // get cm、om state
        HashMap<String, Map<String, String>> cmAndOmState = getCmAndOmState(cluster);
        List<JSONObject> list = nodeSyncSituationDTOList.stream().map(syn -> {
            NodeAndCompDTO com = new NodeAndCompDTO();
            com.setCmAgentState(
                    cmAndOmState.get("cmAgent").getOrDefault(syn.getNodeId(), NodeAndCompDTO.BinState.STOP.getCode()));
            com.setCmServerState(
                    cmAndOmState.get("cmServer").getOrDefault(syn.getNodeId(), NodeAndCompDTO.BinState.STOP.getCode()));
            com.setOmMonitorState(cmAndOmState.get("omMonitor").getOrDefault(syn.getNodeId(),
                    NodeAndCompDTO.BinState.STOP.getCode()));
            JSONObject jsonObject = new JSONObject();
            jsonObject.putAll(BeanUtil.beanToMap(syn));
            jsonObject.putAll(BeanUtil.beanToMap(com));
            jsonObject.remove("walSyncState");
            return jsonObject;
        }).collect(Collectors.toList());
        // relation
        List<NodeRelationDTO> relation = relation(cluster, stateCache);
        JSONObject res = new JSONObject();
        res.put("nodeList", list);
        res.put("relation", relation);
        return res;
    }

    private HashMap<String, Map<String, String>> getCmAndOmState(OpsClusterVO cluster) {
        ConcurrentHashMap<String, String> cmServer = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, String> cmAgent = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, String> omMonitor = new ConcurrentHashMap<>();
        CountDownLatch countDownLatch = ThreadUtil.newCountDownLatch(cluster.getClusterNodes().size());
        for (OpsClusterNodeVO clusterNode : cluster.getClusterNodes()) {
            ThreadUtil.execute(() -> {
                try {
                    execute(cmServer, cmAgent, omMonitor, clusterNode, cluster.getEnvPath());
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        try {
            if (!countDownLatch.await(60, TimeUnit.SECONDS)) {
                throw new CustomException("Exceeding the specified time to get the state of CM and OM");
            }
        } catch (InterruptedException e) {
            throw new CustomException(e.getMessage());
        }
        HashMap<String, Map<String, String>> map = new HashMap<>();
        map.put("cmServer", cmServer);
        map.put("cmAgent", cmAgent);
        map.put("omMonitor", omMonitor);
        return map;
    }

    private void execute(Map<String, String> cmServer, Map<String, String> cmAgent, Map<String, String> omMonitor,
                         OpsClusterNodeVO clusterNode, String envPath) {
        SshSessionUtils sshSession = null;
        try {
            sshSession = getSshSession(clusterNode);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        if (sshSession == null) {
            cmServer.put(clusterNode.getNodeId(), "Unknown");
            cmAgent.put(clusterNode.getNodeId(), "Unknown");
            omMonitor.put(clusterNode.getNodeId(), "Unknown");
            return;
        }
        String executeRes = "";
        String gaussHome = "";
        if (!StringUtils.hasLength(envPath)) {
            envPath = "~/.bashrc";
        }
        String command = "source " + envPath + " && ps -aux | grep \"cm_agent\\|cm_server\\|om_monitor\"";
        try {
            gaussHome = sshSession.execute("source " + envPath + " && echo $GAUSSHOME");
            executeRes = sshSession.execute(command);
        } catch (IOException | CustomException e) {
            log.error(e.getMessage(), e);
        }
        cmServer.put(clusterNode.getNodeId(), "stop");
        cmAgent.put(clusterNode.getNodeId(), "stop");
        omMonitor.put(clusterNode.getNodeId(), "stop");
        String[] binMessage = executeRes.split(StrUtil.LF);
        if (binMessage.length == 2) {
            return;
        }
        for (String bin : binMessage) {
            String[] s = bin.replaceAll(" +", " ").split(" ");
            if ((gaussHome + "/bin/cm_agent").equals(s[10])) {
                cmAgent.put(clusterNode.getNodeId(), s[7]);
                continue;
            }
            if ((gaussHome + "/bin/cm_server").equals(s[10])) {
                cmServer.put(clusterNode.getNodeId(), s[7]);
                continue;
            }
            if ((gaussHome + "/bin/om_monitor").equals(s[10])) {
                omMonitor.put(clusterNode.getNodeId(), s[7]);
            }
        }
    }

    private void checkPrimaryNode(OpsClusterVO cluster, ClusterHealthStateDTO stateCache,
                                         List<SyncSituationDTO> retList) {
        if (retList.size() == cluster.getClusterNodes().size()) {
            return;
        }
        Set<String> standbySet = retList.stream().map(SyncSituationDTO::getNodeId).collect(Collectors.toSet());
        for (OpsClusterNodeVO clusterNode : cluster.getClusterNodes()) {
            if (!standbySet.contains(clusterNode.getNodeId())) {
                SyncSituationDTO dto = new SyncSituationDTO();
                dto.setClusterId(cluster.getClusterId());
                dto.setNodeId(clusterNode.getNodeId());
                dto.setHostIp(clusterNode.getPublicIp());
                dto.setNodeName(stateCache.getNodeName().getOrDefault(clusterNode.getNodeId(),"Unknown"));
                dto.setLocalAddr(clusterNode.getPublicIp() + ":" + clusterNode.getDbPort());
                dto.setRole(stateCache.getNodeRole().getOrDefault(clusterNode.getNodeId(), "Unknown"));
                dto.setNodeState(stateCache.getNodeState().getOrDefault(clusterNode.getNodeId(), "Unknown"));
                retList.add(0, dto);
            }
        }
    }

    private List<NodeRelationDTO> relation(OpsClusterVO cluster, ClusterHealthStateDTO stateCache) {
        List<OpsClusterNodeVO> clusterNodes = cluster.getClusterNodes();
        ArrayList<NodeRelationDTO> retList = new ArrayList<>();
        HashSet<String> allAliveNodeIdSet = new HashSet<>();
        for (OpsClusterNodeVO node : clusterNodes) {
            if ("Primary".equalsIgnoreCase(stateCache.getNodeRole().get(node.getNodeId()))) {
                // Standby
                List<NodeRelationDTO> standbyList = clustersMapper.relation(node.getNodeId());
                standbyList.forEach(standby -> setNodeId(clusterNodes, standby));
                NodeRelationDTO primary = getDefaultRelationDto(node, standbyList);
                // Cascade Standby
                addCascadeStandby(clusterNodes, allAliveNodeIdSet, standbyList);
                retList.add(primary);
                allAliveNodeIdSet
                        .addAll(standbyList.stream().map(NodeRelationDTO::getNodeId).collect(Collectors.toList()));
                allAliveNodeIdSet.add(primary.getNodeId());
            }
        }

        // check
        if (allAliveNodeIdSet.size() != clusterNodes.size()) {
            for (OpsClusterNodeVO node : clusterNodes) {
                if (!allAliveNodeIdSet.contains(node.getNodeId())) {
                    retList.add(getDefaultRelationDto(node, new ArrayList<>()));
                }
            }
        }
        return retList;
    }

    private void setNodeId(List<OpsClusterNodeVO> clusterNodes, NodeRelationDTO standby) {
        Optional<OpsClusterNodeVO> vo = clusterNodes.stream().filter(n ->
                standby.getHostIp().equals(n.getPublicIp()) || standby.getHostIp().equals(
                        n.getPrivateIp())).findFirst();
        vo.ifPresent(v -> standby.setNodeId(v.getNodeId()));
    }

    private void addCascadeStandby(List<OpsClusterNodeVO> clusterNodes, HashSet<String> allAliveNodeIpSet,
                                   List<NodeRelationDTO> standbyList) {
        for (NodeRelationDTO standby : standbyList) {
            List<NodeRelationDTO> cascadeStandbyList =
                    clustersMapper.relation(standby.getNodeId());
            if (!cascadeStandbyList.isEmpty()) {
                cascadeStandbyList.forEach(cascade -> setNodeId(clusterNodes, cascade));
                standby.setChildren(cascadeStandbyList);
                allAliveNodeIpSet.addAll(cascadeStandbyList.stream().map(
                                NodeRelationDTO::getNodeId)
                        .collect(Collectors.toList()));
            }
        }
    }

    @Override
    public List<ClusterStateDTO> allClusterState() {
        ArrayList<ClusterStateDTO> list = new ArrayList<>();
        List<OpsClusterVO> clusters = clusterManager.getAllOpsCluster().stream()
                .filter(cluster -> DeployTypeEnum.CLUSTER.equals(cluster.getDeployType())).collect(Collectors.toList());
        refreshAllClusterState(clusters);
        clusters.forEach(cluster -> {
            ClusterHealthStateDTO stateCache = CLUSTER_STATE_CACHE.get(cluster.getClusterId(), false);
            list.add(ClusterStateDTO.of(cluster, stateCache));
        });
        return list;
    }

    private synchronized void refreshAllClusterState(List<OpsClusterVO> clusters) {
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
            if (!countDown.await(60, TimeUnit.SECONDS)) {
                throw new InstanceException("Exceeding the specified time to refresh all cluster state");
            }
        } catch (InterruptedException e) {
            throw new InstanceException(e.getMessage(), e);
        }
    }

    private ClusterHealthStateDTO getClusterHealthState(String clusterId, OpsClusterVO cluster) {
        synchronized (clusterId.intern()) {
            if (!CLUSTER_STATE_CACHE.containsKey(cluster.getClusterId())) {
                refreshHealthStateCache(cluster);
            }
            return CLUSTER_STATE_CACHE.get(clusterId, false);
        }
    }

    @Override
    public List<SyncSituationDTO> allStandbyNodes() {
        List<OpsClusterVO> clusters = clusterManager.getAllOpsCluster().stream()
                .filter(cluster -> DeployTypeEnum.CLUSTER.equals(cluster.getDeployType())).collect(Collectors.toList());
        refreshAllClusterState(clusters);
        CopyOnWriteArrayList<SyncSituationDTO> retList = new CopyOnWriteArrayList<>();
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
            if (!countDownLatch.await(10, TimeUnit.SECONDS)) {
                throw new InstanceException("Exceeding the specified time to get standby nodes of cluster");
            }
        } catch (InterruptedException e) {
            throw new InstanceException(e.getMessage(), e);
        }
        return retList;
    }

    @Override
    public Map<String, Object> clusterMetrics(String clusterId, Long start, Long end, Integer step) {
        HashMap<String, Object> metricsNodes = new HashMap<>();
        OpsClusterVO opsClusterVOById = clusterManager.getOpsClusterVOById(clusterId);
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

    @Override
    public ClusterStatisticsDTO statistics() {
        List<ClusterStateDTO> clusterStateDTOS = allClusterState();
        List<SyncSituationDTO> allStandbyNodes = allStandbyNodes();
        List<SyncSituationDTO> nodesDataToStandbyNodesState = allStandbyNodes.stream().map(node -> {
            if (!MessageSourceUtils.getMsg(NODE_STATES[0]).equals(node.getNodeState().getValue())) {
                node.setNodeState(new StateDTO(NODE_STATES[1]));
            }
            return node;
        }).collect(Collectors.toList());
        ClusterStatisticsDTO res = new ClusterStatisticsDTO();
        ClusterStatisticsDTO.PieChart clusterState = ClusterStatisticsDTO.buildPieChart(clusterStateDTOS,
                ClusterStateDTO::getClusterState, CLUSTER_STATES);
        res.setCluster(clusterState);
        ClusterStatisticsDTO.PieChart standbyNodesState = ClusterStatisticsDTO
                .buildPieChart(nodesDataToStandbyNodesState, SyncSituationDTO::getNodeState, NODE_STATES);
        res.setNodeStat(standbyNodesState);
        List<SyncSituationDTO> nodesDataToStandbyNodesSyncState = allStandbyNodes.stream().map(node -> {
            if (node.getReceivedDelay() == null) {
                node.setSyncState(new StateDTO(NODE_SYNC_STATES[3]));
                return node;
            }
            if (parseSize(node.getReceivedDelay()) > 10 * 1024 * 1024) {
                node.setSyncState(new StateDTO(NODE_SYNC_STATES[2]));
            }
            return node;
        }).collect(Collectors.toList());
        ClusterStatisticsDTO.PieChart standbyNodesSyncState = ClusterStatisticsDTO
                .buildPieChart(nodesDataToStandbyNodesSyncState,
                        SyncSituationDTO::getSyncState, NODE_SYNC_STATES);
        res.setNodeSyncStat(standbyNodesSyncState);
        allStandbyNodes.removeIf(node -> node.getReceivedDelay() == null);
        allStandbyNodes.sort((o1, o2) -> {
            long size1 = parseSize(o1.getReceivedDelay());
            long size2 = parseSize(o2.getReceivedDelay());
            return Long.compare(size2, size1);
        });
        List<ClusterStatisticsDTO.BarChart> top5 =
                (allStandbyNodes.size() < 6 ? allStandbyNodes : allStandbyNodes.subList(0, 5)).stream().map(top -> {
                    ClusterStatisticsDTO.BarChart barChart = new ClusterStatisticsDTO.BarChart();
                    barChart.setNodeName(top.getNodeName());
                    barChart.setValue(top.getReceivedDelay());
                    return barChart;
                }).collect(Collectors.toList());
        res.setTop5(top5);
        if (top5.size() == 0) {
            return res;
        }
        if (parseSize(top5.get(0).getValue()) == 0) {
            for (ClusterStatisticsDTO.BarChart barChart : top5) {
                barChart.setTatio((double) 0);
            }
        } else {
            for (ClusterStatisticsDTO.BarChart barChart : top5) {
                barChart.setTatio(NumberUtil.div(parseSize(barChart.getValue()),
                        parseSize(top5.get(0).getValue()), 1));
            }
        }
        return res;
    }

    @Override
    public Page<SwitchRecordDTO> switchRecord(String clusterId, Long start, Long end,
                                              Integer pageSize, Integer pageNum) {
        List<String> nodeIds = clusterManager.getOpsClusterVOById(clusterId).getClusterNodes().stream().map(
                OpsClusterNodeVO::getNodeId).collect(Collectors.toList());
        LambdaQueryWrapper<ClusterSwitchoverRecordDO> wrapper = Wrappers.lambdaQuery(ClusterSwitchoverRecordDO.class)
                .eq(ClusterSwitchoverRecordDO::getClusterId, clusterId)
                .in(ClusterSwitchoverRecordDO::getClusterNodeId, nodeIds);
        if (start != null) {
            wrapper.ge(ClusterSwitchoverRecordDO::getSwitchoverTime, DateUtil.formatDateTime(new Date(start)));
        }
        if (end != null) {
            wrapper.le(ClusterSwitchoverRecordDO::getSwitchoverTime, DateUtil.formatDateTime(new Date(end)));
        }
        wrapper.orderByDesc(ClusterSwitchoverRecordDO::getSwitchoverTime);
        Page<ClusterSwitchoverRecordDO> recordListPage = switchoverRecordMapper.selectPage(Page.of(pageNum, pageSize),
                wrapper);
        Page<SwitchRecordDTO> resPage = new Page<>();
        resPage.setCurrent(pageSize);
        resPage.setSize(pageSize);
        resPage.setTotal(recordListPage.getTotal());
        resPage.setRecords(recordListPage.getRecords().stream().map(record -> {
            SwitchRecordDTO switchRecordDto = new SwitchRecordDTO();
            switchRecordDto.setPrimary(record.getPrimaryIp());
            switchRecordDto.setSwitchTime(record.getSwitchoverTime());
            switchRecordDto.setReason(record.getSwitchoverReasonI18n());
            return switchRecordDto;
        }).collect(Collectors.toList()));
        return resPage;
    }


    @Override
    public void switchRecordUpdate(String clusterId) {
        OpsClusterVO clusterVO = clusterManager.getOpsClusterVOById(clusterId);
        for (OpsClusterNodeVO clusterNode : clusterVO.getClusterNodes()) {
            ClusterSwitchoverLogReadDO logRead = switchoverLogReadMapper.selectOne(
                    Wrappers.lambdaQuery(ClusterSwitchoverLogReadDO.class)
                            .eq(ClusterSwitchoverLogReadDO::getClusterId, clusterId)
                            .eq(ClusterSwitchoverLogReadDO::getClusterNodeId, clusterNode.getNodeId()));
            boolean resetLogLastRead = !switchoverRecordMapper.exists(
                    Wrappers.lambdaQuery(ClusterSwitchoverRecordDO.class)
                            .eq(ClusterSwitchoverRecordDO::getClusterNodeId, clusterNode.getNodeId()));
            String envPath = clusterVO.getEnvPath();
            if (!StringUtils.hasLength(envPath)) {
                envPath = "~/.bashrc";
            }
            clusterSwitchService.recordUpdate(clusterId, clusterNode, logRead, envPath, resetLogLastRead);
        }
    }

    private long parseSize(String s) {
        long size = Long.parseLong(s.split(" ")[0]);
        String unit = s.split(" ")[1];
        if (unit.equalsIgnoreCase("KB")) {
            size *= 1024;
        } else if (unit.equalsIgnoreCase("MB")) {
            size *= 1024 * 1024;
        } else if (unit.equalsIgnoreCase("GB")) {
            size *= 1024 * 1024 * 1024;
        } else {
            return size;
        }
        return size;
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
    private void getTodayResult(HashMap<String, Object> metricsNodes,
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
    private void getHistoryResult(HashMap<String, Object> metricsNodes,
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

    private void getMetricsNodes(HashMap<String, Object> metricsNodes,
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

    private void getClusterStandbyNodes(List<SyncSituationDTO> retList, OpsClusterVO cluster) {
        ClusterHealthStateDTO stateCache = CLUSTER_STATE_CACHE.get(cluster.getClusterId(), false);
        List<OpsClusterNodeVO> clusterNodes = cluster.getClusterNodes();
        List<SyncSituationDTO> standbyList = new ArrayList<>();
        for (OpsClusterNodeVO clusterNode : clusterNodes) {
            if (stateCache.getNodeRole() == null) {

            }
            if ("Primary".equalsIgnoreCase(stateCache.getNodeRole().get(clusterNode.getNodeId()))) {
                List<SyncSituationDelayDTO> syncSituations = clustersMapper.getSyncSituation(clusterNode.getNodeId());
                syncSituations.forEach(s -> {
                    SyncSituationDTO standby = getDefaultSituationDto(s, clusterNodes, cluster.getClusterId());
                    standby.setNodeName(stateCache.getNodeName().get(standby.getNodeId()));
                    standby.setRole(stateCache.getNodeRole().get(standby.getNodeId()));
                    standby.setNodeState(stateCache.getNodeState().get(standby.getNodeId()));
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

    private void checkStandbyNode(OpsClusterVO cluster, ClusterHealthStateDTO stateCache,
                                         List<SyncSituationDTO> standbyList) {
        // check, the cluster might not have a primary node (pending...) or disconnect
        ArrayList<String> allNodeId = new ArrayList<>();
        for (Map.Entry<String, String> entry : stateCache.getNodeRole().entrySet()) {
            if (!"Primary".equals(entry.getValue())) {
                allNodeId.add(entry.getKey());
            }
        }
        if (standbyList.isEmpty() && cluster.getClusterNodes().size() != 1) {
            for (OpsClusterNodeVO clusterNode : cluster.getClusterNodes()) {
                SyncSituationDTO dto = new SyncSituationDTO();
                dto.setClusterId(cluster.getClusterId());
                dto.setNodeId(clusterNode.getNodeId());
                dto.setHostIp(clusterNode.getPublicIp());
                dto.setNodeName(clusterNode.getHostname());
                dto.setLocalAddr(clusterNode.getPublicIp() + ":" + clusterNode.getHostPort());
                dto.setRole(stateCache.getNodeRole().getOrDefault(clusterNode.getNodeId(), "Unknown"));
                dto.setNodeState("Unknown");
                standbyList.add(dto);
            }
            return;
        }
        Optional<OpsClusterNodeVO> primary = cluster.getClusterNodes().stream().filter(clusterNode ->
                "Primary".equalsIgnoreCase(stateCache.getNodeRole().get(clusterNode.getPublicIp()))).findFirst();
        if (standbyList.size() != allNodeId.size()) {
            Set<String> standbyNodeId =
                    standbyList.stream().map(SyncSituationDTO::getNodeId).collect(Collectors.toSet());
            for (String nodeId : allNodeId) {
                if (!standbyNodeId.contains(nodeId)) {
                    SyncSituationDTO dto = new SyncSituationDTO();
                    dto.setPrimaryAddr(primary.map(opsClusterNodeVO ->
                            opsClusterNodeVO.getPublicIp() + ":" + opsClusterNodeVO.getDbPort()).orElse(null)
                    );
                    dto.setClusterId(cluster.getClusterId());
                    dto.setNodeId(nodeId);
                    dto.setNodeName(stateCache.getNodeName().get(nodeId));
                    dto.setLocalAddr(cluster.getClusterNodes().stream().filter(n -> n.getNodeId().equals(nodeId))
                            .findFirst().get().getPublicIp() + ":" + cluster.getClusterNodes().get(0).getDbPort());
                    dto.setRole(stateCache.getNodeRole().getOrDefault(nodeId, "Unknown"));
                    dto.setNodeState(stateCache.getNodeState().getOrDefault(nodeId, "Unknown"));
                    standbyList.add(dto);
                }
            }
        }
    }

    private void refreshHealthStateCache(OpsClusterVO cluster) {
        List<OpsClusterNodeVO> clusterNodes = cluster.getClusterNodes();
        SshSessionUtils sshSession = null;

        // Select a host that you can connect to
        for (OpsClusterNodeVO clusterNode : clusterNodes) {
            try {
                sshSession = getSshSession(clusterNode);
                break;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        ClusterHealthStateDTO healthState = ClusterHealthStateDTO.init(cluster);
        if (sshSession == null) {
            log.error("Failed to connect cluster[{}]", cluster.getClusterId());
            CLUSTER_STATE_CACHE.put(cluster.getClusterId(), healthState);
            return;
        }
        healthState = getHealthState(sshSession, cluster);
        CLUSTER_STATE_CACHE.put(cluster.getClusterId(), healthState);
    }

    /**
     * get sshSession by clusterNode
     *
     * @param clusterNode cluster node
     * @return sshSession
     * @throws IOException IOException
     */
    public SshSessionUtils getSshSession(OpsClusterNodeVO clusterNode) throws IOException, ExecutionException,
            InterruptedException, TimeoutException {
        OpsHostEntity hostEntity = hostFacade.getById(clusterNode.getHostId());
        OpsHostUserEntity userEntity = hostUserFacade.listHostUserByHostId(clusterNode.getHostId()).stream()
                .filter(p -> p.getUsername().equals(clusterNode.getInstallUserName())).findFirst().orElseThrow(
                        () -> new InstanceException("The node information corresponding to the host is not found"));
        return SshSessionUtils.getSessionWithTimeout(hostEntity.getPublicIp(), hostEntity.getPort(),
                userEntity.getUsername(), encryptionUtils.decrypt(userEntity.getPassword()), 5000);
    }

    private ClusterHealthStateDTO getHealthState(SshSessionUtils sshSession, OpsClusterVO clusterVO) {
        String envPath = clusterVO.getEnvPath();
        if (!StringUtils.hasLength(envPath)) {
            envPath = "~/.bashrc";
        }
        String command = "source " + envPath + " && gs_om -t status --detail";
        String result = null;
        try {
            result = sshSession.execute(command);
        } catch (IOException | CustomException e) {
            log.error(e.getMessage(), e);
        }
        ClusterHealthStateDTO res = ClusterHealthStateDTO.init(clusterVO);
        if (result == null) {
            return res;
        }
        Map<String, String> cmState = res.getCmState();
        int cmIndex = result.indexOf("CMServer State");
        if (cmIndex > 0) {
            cmState(result, cmState, cmIndex, clusterVO);
        }
        int clusterStateIndex = result.indexOf("cluster_state");
        if (clusterStateIndex > 0) {
            int splitIndex = result.indexOf(":", clusterStateIndex);
            int lineEndIndex = result.indexOf(StrUtil.LF, clusterStateIndex);
            res.setClusterState(result.substring(splitIndex + 1, lineEndIndex).trim());
        }
        int datanodeStateIndex = result.indexOf("Datanode State");
        if (datanodeStateIndex > 0) {
            nodeState(result, datanodeStateIndex, res, clusterVO);
        }
        return res;
    }

    private void cmState(String result, Map<String, String> cmState, int cmIndex, OpsClusterVO clusterVO) {
        int splitIndex = result.indexOf("------------------", cmIndex);
        String dataNodeStateStr = result.substring(splitIndex);
        String[] dataNode = dataNodeStateStr.split(StrUtil.LF);
        for (String s : dataNode) {
            String[] s1 = s.replaceAll(" +", " ").split(" ");
            if (s1.length == 6) {
                //key: nodeId
                String nodeId = clusterVO.getClusterNodes().stream().filter(node ->
                                s1[2].equals(node.getPublicIp()) || s1[2].equals(node.getPrivateIp()))
                        .findFirst().orElseThrow(() ->
                                new InstanceException("clusterId:" + clusterVO.getClusterId() + " ip:" + s1[2] +
                                        " not found in database")).getNodeId();
                cmState.put(nodeId, s1[5].trim());
            }
        }
    }

    private void nodeState(String result, int datanodeStateIndex, ClusterHealthStateDTO res,
                           OpsClusterVO clusterVO) {
        Map<String, String> nodeState = res.getNodeState();
        Map<String, String> nodeRole = res.getNodeRole();
        Map<String, String> nodeName = res.getNodeName();
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
            //key: nodeId
            String nodeId = clusterVO.getClusterNodes().stream().filter(node ->
                            s1[2].equals(node.getPublicIp()) || s1[2].equals(node.getPrivateIp()))
                    .findFirst().orElseThrow(() ->
                            new InstanceException("clusterId:" + clusterVO.getClusterId() + " ip:" + s1[2] +
                                    " not found in database")).getNodeId();
            nodeState.put(nodeId, s1[s1.length - 1]);
            nodeRole.put(nodeId, s1[6]);
            nodeName.put(nodeId, s1[1]);
        }
    }
}
