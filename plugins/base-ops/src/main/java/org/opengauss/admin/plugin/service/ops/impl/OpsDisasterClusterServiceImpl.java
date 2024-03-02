/*
 * Copyright (c) 2022-2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 *           http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.admin.plugin.service.ops.impl;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.common.utils.StringUtils;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterNodeEntity;
import org.opengauss.admin.plugin.domain.entity.ops.OpsDeviceManagerEntity;
import org.opengauss.admin.plugin.domain.entity.ops.OpsDisasterClusterEntity;
import org.opengauss.admin.plugin.domain.model.ops.DisasterBody;
import org.opengauss.admin.plugin.domain.model.ops.DisasterContext;
import org.opengauss.admin.plugin.domain.model.ops.DisasterMonitor;
import org.opengauss.admin.plugin.domain.model.ops.DisasterQueryResult;
import org.opengauss.admin.plugin.domain.model.ops.JschResult;
import org.opengauss.admin.plugin.domain.model.ops.OpsDisasterCluster;
import org.opengauss.admin.plugin.domain.model.ops.OpsDisasterHost;
import org.opengauss.admin.plugin.domain.model.ops.OpsDisasterSubCluster;
import org.opengauss.admin.plugin.domain.model.ops.SshCommandConstants;
import org.opengauss.admin.plugin.domain.model.ops.WsSession;
import org.opengauss.admin.plugin.domain.model.ops.cache.TaskManager;
import org.opengauss.admin.plugin.domain.model.ops.cache.WsConnectorManager;
import org.opengauss.admin.plugin.mapper.ops.OpsClusterNodeMapper;
import org.opengauss.admin.plugin.mapper.ops.OpsDisasterClusterMapper;
import org.opengauss.admin.plugin.service.ops.IOpsClusterNodeService;
import org.opengauss.admin.plugin.service.ops.IOpsClusterService;
import org.opengauss.admin.plugin.service.ops.IOpsDeviceManagerService;
import org.opengauss.admin.plugin.service.ops.IOpsDisasterClusterService;
import org.opengauss.admin.plugin.utils.DeviceManagerUtil;
import org.opengauss.admin.plugin.utils.JschUtil;
import org.opengauss.admin.plugin.utils.WsUtil;
import org.opengauss.admin.system.mapper.ops.OpsHostMapper;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.plugin.facade.HostUserFacade;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.opengauss.util.PSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.io.IOException;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * disaster cluster service implement
 *
 * @author wbd
 * @since 2024/1/26 17:38
 **/
@Slf4j
@Service
public class OpsDisasterClusterServiceImpl extends ServiceImpl<OpsDisasterClusterMapper, OpsDisasterClusterEntity>
    implements IOpsDisasterClusterService {
    private static final Pattern CLUSTER_STATE = Pattern.compile("cluster_state\\s+:\\s+(\\S+)");

    // 'ddr_cluster_stat': 'normal'
    private static final Pattern DISASTER_CLUSTER_STATE = Pattern.compile("\'ddr_cluster_stat\':\\s+\'(\\S+)\'");

    private static final String CLUSTER_STATE_COMMAND = "gs_om -t status --detail";

    private static final String DISASTER_CLUSTER_STATE_COMMAND = "gs_ddr -t query";

    private static final String HAS_SPLIT_STATUS = "26";

    private static final String NORMAL_STATUS = "1";

    private static final String QUERY_CAPACITY_COMMAND =
        "lsscsi -i -s |grep `ll %s | awk '{print $11}'` | awk '{print $8}'";

    private static final Map<String, String> STATUS_MAP = new HashMap();

    static {
        STATUS_MAP.put(NORMAL_STATUS, "Normal");
        STATUS_MAP.put(HAS_SPLIT_STATUS, "Split");
        STATUS_MAP.put("23", "Synchronizing");
        STATUS_MAP.put("33", "To be recovered");
        STATUS_MAP.put("34", "Interrupted");
        STATUS_MAP.put("35", "Invalid");
        STATUS_MAP.put("110", "Standby");
    }

    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostFacade hostFacade;

    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostUserFacade hostUserFacade;

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Autowired
    private IOpsClusterNodeService opsClusterNodeService;

    @Autowired
    private WsUtil wsUtil;

    @Autowired
    private JschUtil jschUtil;

    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private EncryptionUtils encryptionUtils;

    @Autowired
    private WsConnectorManager wsConnectorManager;

    @Autowired
    private OpsDisasterClusterMapper disasterClusterMapper;

    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private OpsHostMapper opsHostMapper;

    @Autowired
    private IOpsClusterService opsClusterService;

    @Autowired
    private OpsClusterNodeMapper opsClusterNodeMapper;

    @Autowired
    private IOpsDeviceManagerService opsDeviceManagerService;

    @Override
    public List<OpsDisasterCluster> listCluster() {
        List<OpsDisasterCluster> result = new ArrayList<>();
        // 先查询出来ops_disaster_cluster和ops_cluster以及ops_cluster_node关联查询
        List<Map<String, String>> clusters = disasterClusterMapper.queryDisasterClusterInfo();
        if (CollectionUtils.isEmpty(clusters)) {
            return result;
        }
        List<String> hostIds = clusters.stream()
            .map(cluster -> String.valueOf(cluster.get("host_id"))).collect(Collectors.toList());
        List<OpsHostEntity> hosts = opsHostMapper.selectBatchIds(hostIds);
        // 将host_id和host_ip绑定，用户后续直接获取
        Map<String, String> hostIdIpMapping = new HashMap<>();
        hosts.forEach(host -> hostIdIpMapping.put(host.getHostId(), host.getPublicIp()));
        // 按照容灾集群名分类
        Map<String, List<Map<String, String>>> disasterMap = new LinkedHashMap<>();
        clusters.forEach(cluster -> disasterMap.computeIfAbsent(cluster.get("disaster_id"), value -> new ArrayList<>())
            .add(cluster));
        disasterMap.forEach((disasterKey, disasterValue) -> {
            OpsDisasterCluster opsDisasterClusterVO = new OpsDisasterCluster();
            opsDisasterClusterVO.setDisasterClusterName(disasterKey);
            // 按照子集群名再分类
            Map<String, List<Map<String, String>>> clusterMap = new LinkedHashMap<>();
            disasterValue.forEach(
                cluster -> clusterMap.computeIfAbsent(cluster.get("cluster_id"), value -> new ArrayList<>())
                    .add(cluster));
            List<OpsDisasterSubCluster> clusterNodes = new ArrayList<>();
            clusterMap.forEach((clusterKey, clusterValue) -> {
                OpsDisasterSubCluster opsDisasterSubClusterVO = new OpsDisasterSubCluster();
                opsDisasterSubClusterVO.setClusterName(clusterValue.get(0).get("cluster_id"));
                opsDisasterSubClusterVO.setClusterRole(
                    clusterValue.get(0).get("cluster_id").equals(clusterValue.get(0).get("primary_cluster_id"))
                        ? "PRIMARY"
                        : "STANDBY");
                opsDisasterSubClusterVO.setVersion(clusterValue.get(0).get("version"));
                opsDisasterSubClusterVO.setVersionNum(clusterValue.get(0).get("version_num"));
                opsDisasterSubClusterVO.setDeployType("CLUSTER");
                opsDisasterSubClusterVO.setDbUser(clusterValue.get(0).get("database_username"));
                opsDisasterSubClusterVO.setDbPort(Integer.parseInt(clusterValue.get(0).get("port")));
                opsDisasterSubClusterVO.setEnvPath(clusterValue.get(0).get("env_path"));
                Map<String, String> masterNode = clusterValue.stream()
                    .filter(map -> "MASTER".equals(map.get("cluster_role"))).findFirst()
                    .orElseThrow(() -> new OpsException("master node information not found"));
                log.error("master host_id is {}", masterNode.get("host_id"));
                opsDisasterSubClusterVO.setMasterIp(hostIdIpMapping.get(String.valueOf(masterNode.get("host_id"))));
                List<Map<String, String>> slaveNodes = clusterValue.stream()
                    .filter(map -> "SLAVE".equals(map.get("cluster_role"))).collect(Collectors.toList());
                opsDisasterSubClusterVO.setSlaveIps(slaveNodes.stream()
                    .map(slaveNode -> hostIdIpMapping.get(String.valueOf(slaveNode.get("host_id"))))
                    .collect(Collectors.toList()));
                clusterNodes.add(opsDisasterSubClusterVO);
            });
            opsDisasterClusterVO.setSubClusters(clusterNodes);
            result.add(opsDisasterClusterVO);
        });
        return result;
    }

    @Override
    public void monitor(String clusterId, String businessId) {
        Future<?> future = threadPoolTaskExecutor.submit(() -> {
            doMonitor(clusterId, businessId);
        });
        TaskManager.registry(businessId, future);
    }

    private Session getSessionByClusterId(String clusterId) {
        OpsClusterNodeEntity nodeEntity = getClusterMasterNodeEntity(clusterId);
        OpsHostEntity hostEntity = hostFacade.getById(nodeEntity.getHostId());
        if (Objects.isNull(hostEntity)) {
            throw new OpsException("Node host information does not exist");
        }
        OpsHostUserEntity hostUserEntity = hostUserFacade.getById(nodeEntity.getInstallUserId());
        if (Objects.isNull(hostUserEntity)) {
            throw new OpsException("Node installation user information does not exist");
        }
        return jschUtil.getSession(hostEntity.getPublicIp(), hostEntity.getPort(), hostUserEntity.getUsername(),
                encryptionUtils.decrypt(hostUserEntity.getPassword()))
            .orElseThrow(() -> new OpsException("Install user connection failed"));
    }

    private void doMonitor(String clusterId, String businessId) {
        Session primarySession = null;
        Session standbySession = null;
        WsSession wsSession = wsConnectorManager.getSession(businessId)
            .orElseThrow(() -> new OpsException("response session does not exist"));
        try {
            // 根据容灾集群ID查询出来子集群信息
            OpsDisasterClusterEntity opsDisasterClusterEntity = getById(clusterId);
            if (opsDisasterClusterEntity == null) {
                throw new OpsException(clusterId + " is not exist");
            }
            primarySession = getSessionByClusterId(opsDisasterClusterEntity.getPrimaryClusterId());
            standbySession = getSessionByClusterId(opsDisasterClusterEntity.getStandbyClusterId());
            // 主集群的环境变量路径
            OpsClusterEntity primaryClusterEntity = opsClusterService.getById(
                opsDisasterClusterEntity.getPrimaryClusterId());
            // 备集群的环境变量路径
            OpsClusterEntity standbyClusterEntity = opsClusterService.getById(
                opsDisasterClusterEntity.getStandbyClusterId());
            queryClusterState(wsSession, primarySession, primaryClusterEntity.getEnvPath(), standbySession,
                standbyClusterEntity.getEnvPath());
        } catch (OpsException e) {
            log.error("monitor occur exception");
        } finally {
            if (Objects.nonNull(primarySession) && primarySession.isConnected()) {
                primarySession.disconnect();
            }
            if (Objects.nonNull(standbySession) && standbySession.isConnected()) {
                standbySession.disconnect();
            }
            wsUtil.close(wsSession);
        }
    }

    private void queryClusterState(WsSession wsSession, Session primarySession, String primaryEnvPath,
        Session standbySession, String standbyEnvPath) {
        AtomicBoolean hasError = new AtomicBoolean(false);
        while (wsSession.getSession().isOpen() && !hasError.get()) {
            DisasterMonitor monitorVO = new DisasterMonitor();
            CountDownLatch countDownLatch = new CountDownLatch(3);
            threadPoolTaskExecutor.submit(() -> {
                try {
                    monitorVO.setTime(System.currentTimeMillis());
                    monitorVO.setDisasterClusterState(
                        queryState(DISASTER_CLUSTER_STATE_COMMAND, primarySession, primaryEnvPath,
                            DISASTER_CLUSTER_STATE));
                } catch (OpsException e) {
                    log.error("queryDisasterClusterState failed.");
                    hasError.set(true);
                } finally {
                    countDownLatch.countDown();
                }
            });
            threadPoolTaskExecutor.submit(() -> {
                try {
                    monitorVO.setPrimaryClusterState(
                        queryState(CLUSTER_STATE_COMMAND, primarySession, primaryEnvPath, CLUSTER_STATE));
                } catch (OpsException e) {
                    log.error("querySubClusterState primary failed.");
                    hasError.set(true);
                } finally {
                    countDownLatch.countDown();
                }
            });
            threadPoolTaskExecutor.submit(() -> {
                try {
                    monitorVO.setStandbyClusterState(
                        queryState(CLUSTER_STATE_COMMAND, standbySession, standbyEnvPath, CLUSTER_STATE));
                } catch (OpsException e) {
                    log.error("querySubClusterState standby failed.");
                    hasError.set(true);
                } finally {
                    countDownLatch.countDown();
                }
            });
            try {
                countDownLatch.await();
                wsUtil.sendText(wsSession, JSON.toJSONString(monitorVO));
                TimeUnit.SECONDS.sleep(1L);
            } catch (InterruptedException e) {
                log.error("waiting for thread to be interrupted", e);
                throw new OpsException("monitor error");
            }
        }
    }

    private String queryState(String command, Session session, String envPath, Pattern pattern) {
        try {
            JschResult jschResult = jschUtil.executeCommand(command, session, envPath);
            if (jschResult.getExitCode() != 0) {
                log.error("Failed to get status info, exit code: {}, log: {}", jschResult.getExitCode(),
                    jschResult.getResult());
                throw new OpsException("Failed to get status information");
            }
            String result = jschResult.getResult();
            // 正则取
            Matcher matcher = pattern.matcher(result);
            // 取不到时默认不可用
            String clusterState = "Unknown";
            if (matcher.find()) {
                clusterState = matcher.group(1);
            }
            return clusterState;
        } catch (IOException | InterruptedException e) {
            log.error("Failed to get status information", e);
            throw new OpsException("Failed to get status information");
        }
    }

    @Override
    public Map<String, Object> listClusterAndDeviceManager() {
        // 过滤条件1：必须是资源池化的单机群，即is_enable_dss为1
        // select distinct cluster_id from ops_cluster_node where is_enable_dss = 1
        List<String> clusterNames = opsClusterNodeMapper.queryDssCluster();
        log.info("clusterNames is {}", clusterNames);
        // 过滤条件2：排除容灾集群中已经存在
        // select primary_cluster_id,standby_cluster_id  from ops_disaster_cluster
        List<String> usedClusterNames = new ArrayList<>();
        List<OpsDisasterClusterEntity> disasterClusterEntityList = list();
        disasterClusterEntityList.forEach(opsDisasterClusterEntity -> {
            if (clusterNames.contains(opsDisasterClusterEntity.getPrimaryClusterId())) {
                clusterNames.remove(opsDisasterClusterEntity.getPrimaryClusterId());
                usedClusterNames.add(opsDisasterClusterEntity.getPrimaryClusterId());
            }
            if (clusterNames.contains(opsDisasterClusterEntity.getStandbyClusterId())) {
                clusterNames.remove(opsDisasterClusterEntity.getStandbyClusterId());
                usedClusterNames.add(opsDisasterClusterEntity.getStandbyClusterId());
            }
        });
        log.info("usedClusterNames is {}", usedClusterNames);
        // 查询deviceManager的信息
        List<String> deviceManagerNames = opsDeviceManagerService.list()
            .stream()
            .map(opsDeviceManager -> opsDeviceManager.getName())
            .collect(Collectors.toList());
        log.info("deviceManagerNames is {}", deviceManagerNames);
        Map<String, Object> result = new HashMap<>();
        result.put("cluster", clusterNames);
        result.put("deviceManager", deviceManagerNames);
        // 磁阵信息全部展示
        return result;
    }

    @Override
    public AjaxResult install(DisasterBody disasterBody) {
        // 检查是否符合满足搭建容灾集群的要求
        Session primarySession = getSessionByClusterId(disasterBody.getPrimaryClusterName());
        Session standbySession = getSessionByClusterId(disasterBody.getStandbyClusterName());
        DisasterContext disasterContext = new DisasterContext();
        disasterContext.setPrimarySession(primarySession);
        disasterContext.setStandbySession(standbySession);
        disasterContext.setPrimaryClusterEntity(opsClusterService.getById(disasterBody.getPrimaryClusterName()));
        disasterContext.setStandbyClusterEntity(opsClusterService.getById(disasterBody.getStandbyClusterName()));
        String checkResult = checkBeforeDisasterClusterInstall(disasterBody, disasterContext);
        if (StringUtils.isNotEmpty(checkResult)) {
            return AjaxResult.error(checkResult);
        }
        WsSession primaryWsSession = wsConnectorManager.getSession(disasterBody.getPrimaryBusinessId())
            .orElseThrow(() -> new OpsException("websocket session not exist"));
        WsSession standbyWsSession = wsConnectorManager.getSession(disasterBody.getStandbyBusinessId())
            .orElseThrow(() -> new OpsException("websocket session not exist"));
        threadPoolTaskExecutor.submit(
            () -> doInstall(disasterBody, disasterContext, primaryWsSession, standbyWsSession));
        return AjaxResult.success();
    }

    private String checkBeforeDisasterClusterInstall(DisasterBody disasterBody, DisasterContext disasterContext) {
        String primaryClusterId = disasterBody.getPrimaryClusterName();
        String standbyClusterId = disasterBody.getStandbyClusterName();
        Session primarySession = disasterContext.getPrimarySession();
        Session standbySession = disasterContext.getStandbySession();
        // 要求查询出来的主备集群的xlog个数都为1
        OpsClusterNodeEntity primaryClusterNodeEntity = getClusterMasterNodeEntity(primaryClusterId);
        OpsClusterNodeEntity standbyClusterNodeEntity = getClusterMasterNodeEntity(standbyClusterId);
        String primaryXlogPath = primaryClusterNodeEntity.getXlogLunLinkPath();
        String standbyXlogPath = standbyClusterNodeEntity.getXlogLunLinkPath();
        if (primaryXlogPath.split(",").length != 1 || standbyXlogPath.split(",").length != 1) {
            return "the number of xlog in the primary/standby cluster must be 1 and the capacity must be the same";
        }
        // 容量比较
        try {
            JschResult primaryResult = jschUtil.executeCommand(String.format(QUERY_CAPACITY_COMMAND, primaryXlogPath),
                primarySession);
            JschResult standbyResult = jschUtil.executeCommand(String.format(QUERY_CAPACITY_COMMAND, primaryXlogPath),
                standbySession);
            if (primaryResult.getExitCode() != 0 || standbyResult.getExitCode() != 0 || !primaryResult.getResult()
                .equals(standbyResult.getResult())) {
                return "the xlog capacity of the primary/standby cluster must be the same";
            }
        } catch (IOException | InterruptedException e) {
            log.error("checkBeforeDisasterClusterInstall failed.");
        }
        return "";
    }

    private OpsClusterNodeEntity getClusterMasterNodeEntity(String clusterId) {
        List<OpsClusterNodeEntity> opsClusterNodeEntities = opsClusterNodeService.listClusterNodeByClusterId(clusterId);
        if (CollUtil.isEmpty(opsClusterNodeEntities)) {
            throw new OpsException("Cluster node information does not exist");
        }
        return opsClusterNodeEntities.stream()
            .filter(opsClusterNodeEntity -> opsClusterNodeEntity.getClusterRole().name().equals("MASTER"))
            .findFirst()
            .orElseThrow(() -> new OpsException("Cluster master node information does not exist"));
    }

    private void doInstall(DisasterBody disasterBody, DisasterContext disasterContext, WsSession primaryWsSession,
        WsSession standbyWsSession) {
        try {
            // 封装json文件，直接写入远程机器上供执行命令时调用，需要再主备集群的主节点上执行
            constructDisasterClusterJson(disasterBody, disasterContext, primaryWsSession, standbyWsSession);
            // 主备集群同时调用搭建容灾集群关系step1
            if (!executeInstallStepOne(disasterContext, primaryWsSession, standbyWsSession)) {
                throw new OpsException("Install execute executeInstallStepOne failed");
            }
            // 调用session、先检查状态是否为1，为1则直接调用跳过，如果是26，则先调用启用从资源保护再调用同步接口；
            // 如果是其他状态则返回，提示远程复制pair的状态,需要将状态映射1->正常、23->正在同步、33->待恢复、
            // 34->异常断开、26->已分裂、35->失效、110->备用
            activateSlaveResourceProtectAndSyncPair(disasterBody.getPrimaryDeviceManager(), primaryWsSession,
                standbyWsSession);
            // 主备集群同时调用搭建容灾集群关系step2，输yes
            if (!executeInstallStepTwo(disasterContext, primaryWsSession, standbyWsSession)) {
                throw new OpsException("Install execute executeInstallStepTwo failed");
            }
            wsUtil.sendText(primaryWsSession, "START_QUERY_STATE");
            wsUtil.sendText(standbyWsSession, "START_QUERY_STATE");
            // 查询容灾状态结果 gs_ddr -t query 解析返回结果
            String disasterState = queryState(DISASTER_CLUSTER_STATE_COMMAND, disasterContext.getPrimarySession(),
                disasterContext.getPrimaryClusterEntity().getEnvPath(), DISASTER_CLUSTER_STATE);
            wsUtil.sendText(primaryWsSession, "END_QUERY_STATE");
            wsUtil.sendText(standbyWsSession, "END_QUERY_STATE");
            // 状态不正确时抛出异常
            if (!"Normal".equalsIgnoreCase(disasterState) && !"archive".equalsIgnoreCase(disasterState)) {
                throw new OpsException("Disaster cluster status is " + disasterState);
            }
            // 保存容灾集群信息
            saveDisasterCluster(disasterBody, disasterContext);
            wsUtil.setSuccessFlag(primaryWsSession);
            wsUtil.setSuccessFlag(standbyWsSession);
        } catch (OpsException | InterruptedException | SQLException e) {
            log.error("doInstall failed.");
            wsUtil.sendText(primaryWsSession, e.getMessage());
            wsUtil.sendText(standbyWsSession, e.getMessage());
            wsUtil.setErrorFlag(primaryWsSession);
            wsUtil.setErrorFlag(standbyWsSession);
        }
    }

    private boolean executeInstallStepOne(DisasterContext disasterContext, WsSession primaryWsSession,
        WsSession standbyWsSession) {
        try {
            // 主集群信息
            OpsClusterEntity primaryClusterEntity = disasterContext.getPrimaryClusterEntity();
            Future primaryFuture = threadPoolTaskExecutor.submit(
                () -> doExecuteInstallStepOne(disasterContext.getPrimarySession(), primaryWsSession,
                    primaryClusterEntity.getEnvPath(),
                    "gs_ddr -t start -m primary -X " + primaryClusterEntity.getXmlConfigPath() + " --json "
                        + disasterContext.getPrimaryJsonPath() + " --stage=1"));
            // 备集群信息
            OpsClusterEntity standbyClusterEntity = disasterContext.getStandbyClusterEntity();
            Future standbyFuture = threadPoolTaskExecutor.submit(
                () -> doExecuteInstallStepOne(disasterContext.getStandbySession(), standbyWsSession,
                    standbyClusterEntity.getEnvPath(),
                    "gs_ddr -t start -m disaster_standby -X " + standbyClusterEntity.getXmlConfigPath() + " --json "
                        + disasterContext.getStandbyJsonPath() + " --stage=1"));
            // 此处阻塞获取成功的标识，否则失败直接发给前端失败信息
            return primaryFuture.get().equals(true) && standbyFuture.get().equals(true);
        } catch (OpsException | ExecutionException | InterruptedException e) {
            log.error("executeInstallStepOne failed.");
            return false;
        }
    }

    private boolean doExecuteInstallStepOne(Session session, WsSession wsSession, String envPath, String command) {
        try {
            wsUtil.sendText(wsSession, "START_INSTALL_STEP1");
            JschResult jschResult = jschUtil.executeCommand(envPath, command, session, wsSession, false);
            if (jschResult.getExitCode() != 0 || !jschResult.getResult()
                .contains("Successfully set cm_guc.")) {
                log.error("Failed to execute 'gs_ddr -t start step1', exit code: {}, error message: {}",
                    jschResult.getExitCode(), jschResult.getResult());
                return false;
            }
            wsUtil.sendText(wsSession, "END_INSTALL_STEP1");
        } catch (OpsException | IOException | InterruptedException e) {
            log.error("Failed to execute 'gs_ddr -t start step1'", e);
            return false;
        }
        return true;
    }

    private boolean executeInstallStepTwo(DisasterContext disasterContext, WsSession primaryWsSession,
        WsSession standbyWsSession) {
        try {
            // 主集群信息
            OpsClusterEntity primaryClusterEntity = disasterContext.getPrimaryClusterEntity();
            // 调用命令行2,输入yes
            Future primaryFutureTemp = threadPoolTaskExecutor.submit(
                () -> doInstallStepTwo(disasterContext.getPrimarySession(), primaryWsSession,
                    primaryClusterEntity.getEnvPath(),
                    "gs_ddr -t start -m primary -X " + primaryClusterEntity.getXmlConfigPath() + " --json "
                        + disasterContext.getPrimaryJsonPath() + " --stage=2"));
            // 备集群信息
            OpsClusterEntity standbyClusterEntity = disasterContext.getStandbyClusterEntity();
            Future standbyFutureTemp = threadPoolTaskExecutor.submit(
                () -> doInstallStepTwo(disasterContext.getStandbySession(), standbyWsSession,
                    standbyClusterEntity.getEnvPath(),
                    "gs_ddr -t start -m disaster_standby -X " + standbyClusterEntity.getXmlConfigPath() + " --json "
                        + disasterContext.getStandbyJsonPath() + " --stage=2"));
            // 此处阻塞获取成功的标识，否则失败直接发给前端失败信息
            return primaryFutureTemp.get().equals(true) && standbyFutureTemp.get().equals(true);
        } catch (OpsException | ExecutionException | InterruptedException e) {
            log.error("executeInstallStepTwo failed.");
            return false;
        }
    }

    private boolean doInstallStepTwo(Session session, WsSession wsSession, String envPath, String command) {
        try {
            wsUtil.sendText(wsSession, "START_INSTALL_STEP2");
            Map<String, String> autoResponse = new HashMap<>();
            autoResponse.put("(yes/no)?", "yes");
            JschResult jschResult = jschUtil.executeCommand(envPath, command, session, wsSession, autoResponse);
            if (jschResult.getExitCode() != 0 || !jschResult.getResult()
                .contains("Successfully do dorado disaster recovery start.")) {
                log.error("Failed to execute 'gs_ddr -t start step2', exit code: {}, error message: {}",
                    jschResult.getExitCode(), jschResult.getResult());
                return false;
            }
            wsUtil.sendText(wsSession, "END_INSTALL_STEP2");
        } catch (OpsException | IOException | InterruptedException e) {
            log.error("Failed to execute 'gs_ddr -t start step2'", e);
            return false;
        }
        return true;
    }

    private void constructDisasterClusterJson(DisasterBody disasterBody, DisasterContext disasterContext,
        WsSession primaryWsSession, WsSession standbyWsSession) {
        OpsClusterEntity primaryClusterEntity = disasterContext.getPrimaryClusterEntity();
        OpsClusterEntity standbyClusterEntity = disasterContext.getStandbyClusterEntity();
        List<String> primaryIps = getHostNodeIp(disasterBody.getPrimaryClusterName());
        List<String> standbyIps = getHostNodeIp(disasterBody.getStandbyClusterName());
        if (primaryIps.size() < 2 || standbyIps.size() < 2) {
            throw new OpsException("Primary/Standby node host size is less 2");
        }
        Session primarySession = disasterContext.getPrimarySession();
        Session standbySession = disasterContext.getStandbySession();
        disasterContext.setPrimaryJsonPath(createJsonFile(primaryClusterEntity.getXmlConfigPath()
                .substring(0, primaryClusterEntity.getXmlConfigPath().lastIndexOf("/")),
            constructJsonContent(standbyClusterEntity.getPort(), standbyIps, primaryClusterEntity.getPort(),
                primaryIps), primarySession, primaryWsSession));

        disasterContext.setStandbyJsonPath(createJsonFile(standbyClusterEntity.getXmlConfigPath()
                .substring(0, standbyClusterEntity.getXmlConfigPath().lastIndexOf("/")),
            constructJsonContent(primaryClusterEntity.getPort(), primaryIps, standbyClusterEntity.getPort(),
                standbyIps), standbySession, standbyWsSession));
    }

    private String constructJsonContent(int remotePort, List<String> remoteIps, int localPort,
        List<String> localIps) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("remoteClusterConf", getJsonObject(remotePort, remoteIps));
        jsonObject.put("localClusterConf", getJsonObject(localPort, localIps));
        return jsonObject.toJSONString();
    }

    private JSONObject getJsonObject(int port, List<String> ips) {
        JSONArray shardNodes = new JSONArray();
        for (String ip : ips) {
            JSONObject jsonNodeObject = new JSONObject();
            jsonNodeObject.put("ip", ip);
            jsonNodeObject.put("dataIp", ip);
            shardNodes.add(jsonNodeObject);
        }
        JSONArray shards = new JSONArray();
        shards.add(shardNodes);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("port", port);
        jsonObject.put("shards", shards);
        return jsonObject;
    }

    private List<String> getHostNodeIp(String clusterId) {
        List<OpsClusterNodeEntity> standbyNodeEntities = opsClusterNodeService.listClusterNodeByClusterId(clusterId);
        if (CollUtil.isEmpty(standbyNodeEntities)) {
            throw new OpsException("Cluster node information does not exist");
        }
        List<String> ips = new ArrayList<>();
        standbyNodeEntities.forEach(nodeEntity -> {
            OpsHostEntity hostEntity = hostFacade.getById(nodeEntity.getHostId());
            if (Objects.isNull(hostEntity)) {
                throw new OpsException("Node host information does not exist");
            }
            ips.add(hostEntity.getPublicIp());
        });
        return ips;
    }

    private String createJsonFile(String folder, String jsonContent, Session session, WsSession wsSession) {
        // 查询出来集群表cluster的xml_config_path，主集群的主节点上执行，调用获取session的公共方法
        String jsonFullPath = folder + "/disaster_cluster.json";
        String createDisasterClusterJsonCommand = MessageFormat.format(SshCommandConstants.FILE_CREATE, jsonContent,
            jsonFullPath);
        try {
            JschResult jschResult = jschUtil.executeCommand(createDisasterClusterJsonCommand, session, wsSession);
            if (jschResult.getExitCode() != 0) {
                log.error("Failed to create json file, exit code: {}, error message: {}", jschResult.getExitCode(),
                    jschResult.getResult());
                throw new OpsException("Failed to create json file");
            }
        } catch (IOException | InterruptedException e) {
            log.error("Failed to create json configuration file");
            throw new OpsException("Failed to create json configuration file");
        }
        return jsonFullPath;
    }

    private void activateSlaveResourceProtectAndSyncPair(String deviceManagerName, WsSession primaryWsSession,
        WsSession standbyWsSession) throws InterruptedException, OpsException {
        OpsDeviceManagerEntity opsDeviceManagerEntity = opsDeviceManagerService.getById(deviceManagerName);
        opsDeviceManagerEntity.setPassword(encryptionUtils.decrypt(opsDeviceManagerEntity.getPassword()));
        Map<String, String> authenticateMap = DeviceManagerUtil.authenticate(opsDeviceManagerEntity);
        DisasterQueryResult result = DeviceManagerUtil.queryPairInfo(opsDeviceManagerEntity, authenticateMap);
        switch (result.getRunningStatus()) {
            case NORMAL_STATUS:
                wsUtil.sendText(primaryWsSession, "REMOTE_REPLICATION_PAIR_STATUS_IS_NORMAL");
                wsUtil.sendText(standbyWsSession, "REMOTE_REPLICATION_PAIR_STATUS_IS_NORMAL");
                break;
            case HAS_SPLIT_STATUS:
                wsUtil.sendText(primaryWsSession, "START_SYNC_REMOTE_REPLICATION_PAIR");
                wsUtil.sendText(standbyWsSession, "START_SYNC_REMOTE_REPLICATION_PAIR");
                // 已分裂的先启用从资源保护再同步
                wsUtil.sendText(primaryWsSession, "START_ACTIVATE_SLAVE_PROTECT");
                wsUtil.sendText(standbyWsSession, "START_ACTIVATE_SLAVE_PROTECT");
                DeviceManagerUtil.activateSlaveResourceProtect(opsDeviceManagerEntity, authenticateMap.get("deviceId"),
                    authenticateMap.get("iBaseToken"), authenticateMap.get("cookie"));
                wsUtil.sendText(primaryWsSession, "END_ACTIVATE_SLAVE_PROTECT");
                wsUtil.sendText(standbyWsSession, "END_ACTIVATE_SLAVE_PROTECT");
                wsUtil.sendText(primaryWsSession, "START_SYNC_REMOTE_REPLICATION_PAIR");
                wsUtil.sendText(standbyWsSession, "START_SYNC_REMOTE_REPLICATION_PAIR");
                DeviceManagerUtil.syncPair(opsDeviceManagerEntity, authenticateMap);
                // 修改同步速率，缩短同步时间
                DeviceManagerUtil.modifyReplicationParams(opsDeviceManagerEntity, authenticateMap);
                int count = 0;
                while (true) {
                    DisasterQueryResult queryResult = DeviceManagerUtil.queryPairInfo(opsDeviceManagerEntity,
                        authenticateMap);
                    if (!queryResult.getReplicationProgress().isEmpty()) {
                        wsUtil.sendText(primaryWsSession,
                                "Replication sync progress: " + queryResult.getReplicationProgress() + " %");
                        wsUtil.sendText(standbyWsSession,
                                "Replication sync progress: " + queryResult.getReplicationProgress() + " %");
                    }

                    if (NORMAL_STATUS.equals(queryResult.getRunningStatus())) {
                        wsUtil.sendText(primaryWsSession,
                                "Remote replication pair status is " + STATUS_MAP.getOrDefault(
                                        queryResult.getRunningStatus(), "Unknown"));
                        wsUtil.sendText(standbyWsSession,
                                "Remote replication pair status is " + STATUS_MAP.getOrDefault(
                                        queryResult.getRunningStatus(), "Unknown"));

                        break;
                    }
                    // 如果超过5000s钟则跳出循环
                    if (count > 1000) {
                        log.error("Replication dose not finished in 5000s, replication sync failed...");
                        throw new OpsException("Replication sync failed");
                    }
                    count++;
                    Thread.sleep(5000);
                }
                wsUtil.sendText(primaryWsSession, "END_SYNC_REMOTE_REPLICATION_PAIR");
                wsUtil.sendText(standbyWsSession, "END_SYNC_REMOTE_REPLICATION_PAIR");
                break;
            default:
                throw new OpsException(
                    "can't install,because the remote replication pair status is " + STATUS_MAP.getOrDefault(
                            result.getRunningStatus(), "unknown"));
        }
    }

    private void saveDisasterCluster(DisasterBody disasterBody, DisasterContext disasterContext) throws PSQLException {
        OpsDisasterClusterEntity disasterClusterEntity = new OpsDisasterClusterEntity();
        disasterClusterEntity.setClusterId(disasterBody.getClusterId());
        disasterClusterEntity.setPrimaryClusterId(disasterBody.getPrimaryClusterName());
        disasterClusterEntity.setStandbyClusterId(disasterBody.getStandbyClusterName());
        disasterClusterEntity.setPrimaryClusterDeviceManagerName(disasterBody.getPrimaryDeviceManager());
        disasterClusterEntity.setStandbyClusterDeviceManagerName(disasterBody.getStandbyDeviceManager());
        disasterClusterEntity.setPrimaryJsonPath(disasterContext.getPrimaryJsonPath());
        disasterClusterEntity.setStandbyJsonPath(disasterContext.getStandbyJsonPath());
        save(disasterClusterEntity);
    }

    @Override
    public AjaxResult switchover(DisasterBody disasterBody) {
        // 找主磁阵信息
        OpsDisasterClusterEntity opsDisasterClusterEntity = getById(disasterBody.getClusterId());
        // session获取token、cookie、deviceId
        OpsDeviceManagerEntity opsDeviceManagerEntity = opsDeviceManagerService.getById(
            opsDisasterClusterEntity.getPrimaryClusterDeviceManagerName());
        opsDeviceManagerEntity.setPassword(encryptionUtils.decrypt(opsDeviceManagerEntity.getPassword()));
        Map<String, String> authenticateMap = DeviceManagerUtil.authenticate(opsDeviceManagerEntity);
        DisasterQueryResult result = DeviceManagerUtil.queryPairInfo(opsDeviceManagerEntity, authenticateMap);
        // 先根据状态为同步时再进行switchover操作，如果非同步状态，则提示不能切换
        if (!NORMAL_STATUS.equals(result.getRunningStatus())) {
            return AjaxResult.error("the remote replication pair status is abnormal, can't switchover");
        }
        // 主：gs_ddr -t switchover -m disaster_standby 备：gs_ddr -t switchover -m primary
        // 调用命令行1 执行成功的关键字
        RequestAttributes context = RequestContextHolder.currentRequestAttributes();
        threadPoolTaskExecutor.submit(() -> {
            RequestContextHolder.setRequestAttributes(context);
            doSwitchover(disasterBody, opsDisasterClusterEntity, opsDeviceManagerEntity, authenticateMap);
        });
        return AjaxResult.success();
    }

    private void doSwitchover(DisasterBody disasterBody, OpsDisasterClusterEntity opsDisasterClusterEntity,
        OpsDeviceManagerEntity opsDeviceManagerEntity, Map<String, String> authenticateMap) {
        WsSession primaryWsSession = wsConnectorManager.getSession(disasterBody.getPrimaryBusinessId())
            .orElseThrow(() -> new OpsException("websocket session not exist"));
        WsSession standbyWsSession = wsConnectorManager.getSession(disasterBody.getStandbyBusinessId())
            .orElseThrow(() -> new OpsException("websocket session not exist"));
        Session primarySession = getSessionByClusterId(opsDisasterClusterEntity.getPrimaryClusterId());
        Session standbySession = getSessionByClusterId(opsDisasterClusterEntity.getStandbyClusterId());
        try {
            // 主从切换第一步
            if (!executeSwitchoverStepOne(opsDisasterClusterEntity, primarySession, primaryWsSession, standbySession,
                standbyWsSession)) {
                throw new OpsException("executeSwitchoverStepOne failed");
            }
            // 在主deviceManager调用switchover
            wsUtil.sendText(primaryWsSession, "START_SWITCH_REMOTE_REPLICATION_PAIR");
            wsUtil.sendText(standbyWsSession, "START_SWITCH_REMOTE_REPLICATION_PAIR");
            DeviceManagerUtil.switchMasterToSlave(opsDeviceManagerEntity, authenticateMap);
            wsUtil.sendText(primaryWsSession, "END_SWITCH_REMOTE_REPLICATION_PAIR");
            wsUtil.sendText(standbyWsSession, "END_SWITCH_REMOTE_REPLICATION_PAIR");
            // 主从第二步
            if (!executeSwitchoverStepTwo(opsDisasterClusterEntity, primarySession, primaryWsSession, standbySession,
                standbyWsSession)) {
                throw new OpsException("executeSwitchoverStepTwo failed");
            }
            // Successfully do dorado disaster recovery switchover.
            updateDisasterCluster(opsDisasterClusterEntity);
            wsUtil.setSuccessFlag(primaryWsSession);
            wsUtil.setSuccessFlag(standbyWsSession);
        } catch (OpsException e) {
            log.error("switchover failed.");
            wsUtil.setErrorFlag(primaryWsSession);
            wsUtil.setErrorFlag(standbyWsSession);
        }
    }

    private boolean executeSwitchoverStepOne(OpsDisasterClusterEntity opsDisasterClusterEntity, Session primarySession,
        WsSession primaryWsSession, Session standbySession, WsSession standbyWsSession) {
        try {
            // 主集群信息
            OpsClusterEntity primaryClusterEntity = opsClusterService.getById(
                opsDisasterClusterEntity.getPrimaryClusterId());
            Future primaryFuture = threadPoolTaskExecutor.submit(
                () -> doSwitchoverStepOne(primarySession, primaryWsSession, primaryClusterEntity.getEnvPath(),
                    "gs_ddr -t switchover -m disaster_standby --stage=1"));
            // 备集群信息
            OpsClusterEntity standbyClusterEntity = opsClusterService.getById(
                opsDisasterClusterEntity.getStandbyClusterId());
            Future standbyFuture = threadPoolTaskExecutor.submit(
                () -> doSwitchoverStepOne(standbySession, standbyWsSession, standbyClusterEntity.getEnvPath(),
                    "gs_ddr -t switchover -m primary --stage=1"));
            // 此处阻塞获取成功的标识，否则失败直接发给前端失败信息
            return primaryFuture.get().equals(true) && standbyFuture.get().equals(true);
        } catch (OpsException | ExecutionException | InterruptedException e) {
            log.error("executeCommandStepOne failed.");
            return false;
        }
    }

    private boolean executeSwitchoverStepTwo(OpsDisasterClusterEntity opsDisasterClusterEntity, Session primarySession,
        WsSession primaryWsSession, Session standbySession, WsSession standbyWsSession) {
        try {
            OpsClusterEntity primaryClusterEntity = opsClusterService.getById(
                opsDisasterClusterEntity.getPrimaryClusterId());
            // 调用命令行2,输入yes
            Future primaryFutureTemp = threadPoolTaskExecutor.submit(
                () -> doSwitchoverStepTwo(primarySession, primaryWsSession, primaryClusterEntity.getEnvPath(),
                    "gs_ddr -t switchover -m disaster_standby --stage=2"));
            OpsClusterEntity standbyClusterEntity = opsClusterService.getById(
                opsDisasterClusterEntity.getStandbyClusterId());
            Future standbyFutureTemp = threadPoolTaskExecutor.submit(
                () -> doSwitchoverStepTwo(standbySession, standbyWsSession, standbyClusterEntity.getEnvPath(),
                    "gs_ddr -t switchover -m primary --stage=2"));
            // 此处阻塞获取成功的标识，否则失败直接发给前端失败信息
            return primaryFutureTemp.get().equals(true) && standbyFutureTemp.get().equals(true);
        } catch (OpsException | ExecutionException | InterruptedException e) {
            log.error("executeCommandStepTwo failed.");
            return false;
        }
    }

    private boolean doSwitchoverStepOne(Session session, WsSession wsSession, String envPath, String command) {
        try {
            wsUtil.sendText(wsSession, "START_SWITCHOVER_STEP1");
            JschResult jschResult = jschUtil.executeCommand(envPath, command, session, wsSession, false);
            if (jschResult.getExitCode() != 0 || !jschResult.getResult()
                .contains("Successfully do_first_stage_for_switchover.")) {
                log.error("Failed to execute 'gs_ddr -t switchover step1', exit code: {}, error message: {}",
                    jschResult.getExitCode(), jschResult.getResult());
                return false;
            }
            wsUtil.sendText(wsSession, "END_SWITCHOVER_STEP1");
        } catch (OpsException | IOException | InterruptedException e) {
            log.error("Failed to execute 'gs_ddr -t switchover step1'", e);
            return false;
        }
        return true;
    }

    private boolean doSwitchoverStepTwo(Session session, WsSession wsSession, String envPath, String command) {
        try {
            wsUtil.sendText(wsSession, "START_SWITCHOVER_STEP2");
            Map<String, String> autoResponse = new HashMap<>();
            autoResponse.put("(yes/no)?", "yes");
            JschResult jschResult = jschUtil.executeCommand(envPath, command, session, wsSession, autoResponse);
            if (jschResult.getExitCode() != 0 || !jschResult.getResult()
                .contains("Successfully do dorado disaster recovery switchover.")) {
                log.error("Failed to execute 'gs_ddr -t switchover step2', exit code: {}, error message: {}",
                    jschResult.getExitCode(), jschResult.getResult());
                return false;
            }
            wsUtil.sendText(wsSession, "END_SWITCHOVER_STEP2");
        } catch (OpsException | IOException | InterruptedException e) {
            log.error("Failed to execute 'gs_ddr -t switchover step2'", e);
            return false;
        }
        return true;
    }

    private void updateDisasterCluster(OpsDisasterClusterEntity opsDisasterClusterEntity) {
        // 更新容灾集群表中的主、备集群名称和磁阵名称、json路径
        LambdaUpdateWrapper<OpsDisasterClusterEntity> updateWrapper = Wrappers.lambdaUpdate(
                OpsDisasterClusterEntity.class)
            .set(OpsDisasterClusterEntity::getPrimaryClusterId, opsDisasterClusterEntity.getStandbyClusterId())
            .set(OpsDisasterClusterEntity::getStandbyClusterId, opsDisasterClusterEntity.getPrimaryClusterId())
            .set(OpsDisasterClusterEntity::getPrimaryClusterDeviceManagerName,
                opsDisasterClusterEntity.getStandbyClusterDeviceManagerName())
            .set(OpsDisasterClusterEntity::getStandbyClusterDeviceManagerName,
                opsDisasterClusterEntity.getPrimaryClusterDeviceManagerName())
            .set(OpsDisasterClusterEntity::getPrimaryJsonPath, opsDisasterClusterEntity.getStandbyJsonPath())
            .set(OpsDisasterClusterEntity::getStandbyJsonPath, opsDisasterClusterEntity.getPrimaryJsonPath())
            .eq(OpsDisasterClusterEntity::getClusterId, opsDisasterClusterEntity.getClusterId());
        update(updateWrapper);
    }

    @Override
    public void removeDisasterCluster(DisasterBody disasterBody) {
        // 查询主集群的主节点信息以及主集群的磁阵信息
        OpsDisasterClusterEntity opsDisasterClusterEntity = getById(disasterBody.getClusterId());
        // 主集群信息
        OpsClusterEntity primaryClusterEntity = opsClusterService.getById(
            opsDisasterClusterEntity.getPrimaryClusterId());
        RequestAttributes context = RequestContextHolder.currentRequestAttributes();
        WsSession wsSession = wsConnectorManager.getSession(disasterBody.getBusinessId())
            .orElseThrow(() -> new OpsException("websocket session not exist"));
        Future<?> future = threadPoolTaskExecutor.submit(() -> {
            RequestContextHolder.setRequestAttributes(context);
            doRemoveDisasterCluster(opsDisasterClusterEntity.getPrimaryClusterId(), wsSession,
                primaryClusterEntity, opsDisasterClusterEntity);
        });
        TaskManager.registry(disasterBody.getBusinessId(), future);
    }

    private void doRemoveDisasterCluster(String clusterId, WsSession wsSession, OpsClusterEntity primaryClusterEntity,
        OpsDisasterClusterEntity opsDisasterClusterEntity) {
        try {
            wsUtil.sendText(wsSession, "START_REMOVE_DISASTER");
            wsUtil.sendText(wsSession, "START_STOP_DISASTER");
            executeStop(primaryClusterEntity, clusterId, wsSession, opsDisasterClusterEntity.getPrimaryJsonPath());
            wsUtil.sendText(wsSession, "END_STOP_DISASTER");
            // 回退远程复制pair到搭建容灾集群前的状态
            wsUtil.sendText(wsSession, "START_ROLLBACK_REMOTE_REPLICATION_PAIR");
            rollbackRemoteReplicationPair(opsDisasterClusterEntity.getPrimaryClusterDeviceManagerName(), wsSession);
            wsUtil.sendText(wsSession, "END_ROLLBACK_REMOTE_REPLICATION_PAIR");
            // 删除容灾集群表中的信息
            removeById(opsDisasterClusterEntity.getClusterId());
            wsUtil.sendText(wsSession, "END_REMOVE_DISASTER");
            wsUtil.setSuccessFlag(wsSession);
        } catch (OpsException e) {
            log.error("Failed to execute 'gs_ddr -t stop'");
            wsUtil.setErrorFlag(wsSession);
        }
    }

    // 此方法可以用于回退API的操作到搭建容灾集群前
    private void rollbackRemoteReplicationPair(String deviceManagerName, WsSession wsSession) {
        // 调用session
        OpsDeviceManagerEntity opsDeviceManagerEntity = opsDeviceManagerService.getById(deviceManagerName);
        opsDeviceManagerEntity.setPassword(encryptionUtils.decrypt(opsDeviceManagerEntity.getPassword()));
        Map<String, String> authenticateMap = DeviceManagerUtil.authenticate(opsDeviceManagerEntity);
        // 如果状态已经是分裂，直接调用取消从资源保护
        if (HAS_SPLIT_STATUS.equals(
            DeviceManagerUtil.queryPairInfo(opsDeviceManagerEntity, authenticateMap))) {
            wsUtil.sendText(wsSession, "Remote replication pair had split");
            cancelSlaveResourceProtect(wsSession, opsDeviceManagerEntity, authenticateMap);
            return;
        }
        splitPair(wsSession, opsDeviceManagerEntity, authenticateMap);
        cancelSlaveResourceProtect(wsSession, opsDeviceManagerEntity, authenticateMap);
    }

    private void cancelSlaveResourceProtect(WsSession wsSession, OpsDeviceManagerEntity opsDeviceManagerEntity,
        Map<String, String> authenticateMap) {
        wsUtil.sendText(wsSession, "START_CANCEL_SLAVE_PROTECT");
        // 调用取消从资源保护
        DeviceManagerUtil.cancelSlaveResourceProtect(opsDeviceManagerEntity, authenticateMap.get("deviceId"),
            authenticateMap.get("iBaseToken"), authenticateMap.get("cookie"));
        wsUtil.sendText(wsSession, "END_CANCEL_SLAVE_PROTECT");
    }

    private void splitPair(WsSession wsSession, OpsDeviceManagerEntity opsDeviceManagerEntity,
        Map<String, String> authenticateMap) {
        try {
            wsUtil.sendText(wsSession, "START_SPLIT_REMOTE_REPLICATION_PAIR");
            // 调用分裂
            DeviceManagerUtil.splitPair(opsDeviceManagerEntity, authenticateMap);
            int count = 0;
            while (true) {
                DisasterQueryResult result = DeviceManagerUtil.queryPairInfo(opsDeviceManagerEntity,
                        authenticateMap);
                wsUtil.sendText(wsSession, "Remote replication pair status is " + result.getRunningStatus());
                // 如果已分裂或者超过5s钟则跳出循环
                if (HAS_SPLIT_STATUS.equals(result.getRunningStatus()) || count >= 5) {
                    break;
                }
                count++;
                Thread.sleep(1000);
            }
            wsUtil.sendText(wsSession, "END_SPLIT_REMOTE_REPLICATION_PAIR");
        } catch (InterruptedException e) {
            log.error("splitPair failed.");
            throw new OpsException("splitPair failed");
        }
    }

    private void executeStop(OpsClusterEntity primaryClusterEntity, String clusterId, WsSession wsSession,
        String jsonPath) {
        // 调用命令gs_ddr -t stop
        // ip port installUser password
        Session session = getSessionByClusterId(clusterId);
        String command = "gs_ddr -t stop -X " + primaryClusterEntity.getXmlConfigPath() + " --json " + jsonPath;
        try {
            JschResult jschResult = jschUtil.executeCommand(primaryClusterEntity.getEnvPath(), command, session,
                wsSession, false);
            if (jschResult.getExitCode() != 0 || !jschResult.getResult()
                .contains("Successfully do dorado disaster recovery stop.")) {
                log.error("Failed to execute 'gs_ddr -t stop', exit code: {}, error message: {}",
                    jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("Failed to execute 'gs_ddr -t stop'");
            }
        } catch (IOException | InterruptedException e) {
            log.error("executeStop failed.");
        }
    }

    @Override
    public List<OpsDisasterHost> getHosts(String primaryClusterName, String standbyClusterName) {
        List<OpsDisasterHost> hosts = new ArrayList<>();
        addDisasterHost(primaryClusterName, "PRIMARY", hosts);
        addDisasterHost(standbyClusterName, "STANDBY", hosts);
        return hosts;
    }

    private void addDisasterHost(String clusterId, String clusterRole, List<OpsDisasterHost> hosts) {
        List<OpsClusterNodeEntity> clusterNodeEntities = opsClusterNodeService.listClusterNodeByClusterId(
            clusterId);
        if (CollUtil.isEmpty(clusterNodeEntities)) {
            throw new OpsException("Cluster node information does not exist");
        }
        clusterNodeEntities.forEach(node -> {
            OpsDisasterHost disasterHostVO = new OpsDisasterHost();
            OpsHostEntity hostEntity = hostFacade.getById(node.getHostId());
            if (Objects.isNull(hostEntity)) {
                throw new OpsException("Node host information does not exist");
            }
            OpsHostUserEntity hostUserEntity = hostUserFacade.getRootUserByHostId(node.getHostId());
            if (Objects.isNull(hostUserEntity)) {
                throw new OpsException("Node installation root user information does not exist");
            }
            disasterHostVO.setHostId(node.getHostId());
            disasterHostVO.setPublicIp(hostEntity.getPublicIp());
            disasterHostVO.setPrivateIp(hostEntity.getPrivateIp());
            disasterHostVO.setPort(hostEntity.getPort());
            disasterHostVO.setUserName(hostUserEntity.getUsername());
            disasterHostVO.setPassword(hostUserEntity.getPassword());
            disasterHostVO.setClusterRole(clusterRole);
            disasterHostVO.setNodeRole(node.getClusterRole().name());
            hosts.add(disasterHostVO);
        });
    }
}
