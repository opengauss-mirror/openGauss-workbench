/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 * OpsClusterServiceImpl.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/service/ops/impl
 * /OpsClusterServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.system.service.ops.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.opengauss.admin.common.core.domain.entity.ops.*;
import org.opengauss.admin.common.core.domain.model.ops.*;
import org.opengauss.admin.common.core.domain.model.ops.check.*;
import org.opengauss.admin.common.core.handler.ops.cache.TaskManager;
import org.opengauss.admin.common.core.handler.ops.cache.WsConnectorManager;
import org.opengauss.admin.common.enums.ops.ClusterRoleEnum;
import org.opengauss.admin.common.enums.ops.OpenGaussVersionEnum;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.common.utils.ops.WsUtil;
import org.opengauss.admin.system.mapper.ops.OpsClusterMapper;
import org.opengauss.admin.system.plugin.beans.SshLogin;
import org.opengauss.admin.system.service.HostMonitorCacheService;
import org.opengauss.admin.system.service.JschExecutorService;
import org.opengauss.admin.system.service.ops.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.annotation.Resource;

/**
 * @author lhf
 * @date 2022/8/6 17:38
 **/
@Slf4j
@Service
public class OpsClusterServiceImpl extends ServiceImpl<OpsClusterMapper, OpsClusterEntity>
    implements IOpsClusterService {
    @Autowired
    private IHostService hostService;
    @Autowired
    private IHostUserService hostUserService;
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Autowired
    @Lazy
    private IOpsClusterNodeService opsClusterNodeService;
    @Autowired
    private IOpsJdbcDbClusterService opsJdbcDbClusterService;
    @Autowired
    private IOpsCheckService opsCheckService;
    @Autowired
    private WsConnectorManager wsConnectorManager;
    @Autowired
    private WsUtil wsUtil;
    @Resource
    private JschExecutorService jschExecutorService;
    @Resource
    private HostMonitorCacheService hostMonitorCacheService;
    @Autowired
    private EncryptionUtils encryptionUtils;

    @Override
    public List<OpsClusterVO> listCluster() {
        List<OpsClusterVO> res = new ArrayList<>();
        List<OpsClusterEntity> opsClusterEntities = list(
            Wrappers.lambdaQuery(OpsClusterEntity.class).orderByDesc(OpsClusterEntity::getCreateTime));
        List<String> clusterIds = opsClusterEntities.stream()
            .map(OpsClusterEntity::getClusterId)
            .collect(Collectors.toList());
        Map<String, OpsCheckEntity> checkResMap = opsCheckService.mapLastResByClusterIds(clusterIds);
        Map<String, List<OpsClusterNodeEntity>> clusterNodeMap = opsClusterNodeService.listClusterNodeByClusterIds(
            clusterIds);
        Map<String, OpsHostEntity> hostEntityMap = new HashMap<>();
        List<String> hostIds = clusterNodeMap.values()
            .stream()
            .flatMap(nodeList -> nodeList.stream())
            .map(OpsClusterNodeEntity::getHostId)
            .collect(Collectors.toList());
        Map<String, OpsHostUserEntity> hostUserEntityMap = new HashMap<>();
        Map<String, OpsHostUserEntity> rootUserMap = new HashMap<>();
        if (CollUtil.isNotEmpty(hostIds)) {
            List<OpsHostEntity> opsHostEntities = hostService.listByIds(hostIds);
            if (CollUtil.isNotEmpty(opsHostEntities)) {
                hostEntityMap.putAll(
                    opsHostEntities.stream().collect(Collectors.toMap(OpsHostEntity::getHostId, Function.identity())));
                List<OpsHostUserEntity> opsHostUserEntities = hostUserService.listHostUserByHostIdList(
                    opsHostEntities.stream().map(OpsHostEntity::getHostId).collect(Collectors.toList()));
                hostUserEntityMap.putAll(opsHostUserEntities.stream()
                    .collect(Collectors.toMap(OpsHostUserEntity::getHostUserId, Function.identity())));
                hostUserEntityMap.putAll(opsHostUserEntities.stream()
                    .filter(hostUserEntity -> "root".equals(hostUserEntity.getUsername()))
                    .collect(Collectors.toMap(OpsHostUserEntity::getHostId, Function.identity())));
            }
        }
        for (OpsClusterEntity opsClusterEntity : opsClusterEntities) {
            OpsClusterVO opsClusterVO = OpsClusterVO.of(opsClusterEntity);
            String clusterId = opsClusterEntity.getClusterId();
            OpsCheckEntity checkRes = checkResMap.get(clusterId);
            if (Objects.nonNull(checkRes)) {
                HashMap<String, Integer> checkSummary = new HashMap<>();
                CheckVO checkVO = parseCheckResToCheckVO(checkRes.getCheckRes());
                if (Objects.nonNull(checkVO)) {
                    Map<String, List<CheckItemVO>> summary = checkVO.summary();
                    if (CollUtil.isNotEmpty(summary)) {
                        summary.forEach((k, v) -> {
                            checkSummary.put(k, v.size());
                        });
                    }
                }
                opsClusterVO.setCheckSummary(checkSummary);
                opsClusterVO.setLastCheckAt(checkRes.getCreateTime());
            }
            res.add(opsClusterVO);
            List<OpsClusterNodeVO> clusterNodeVOS = new ArrayList<>();
            opsClusterVO.setClusterNodes(clusterNodeVOS);
            List<OpsClusterNodeEntity> opsClusterNodeEntities = clusterNodeMap.get(clusterId);
            if (CollUtil.isEmpty(opsClusterNodeEntities)) {
                continue;
            }
            for (OpsClusterNodeEntity opsClusterNodeEntity : opsClusterNodeEntities) {
                OpsClusterNodeVO opsClusterNodeVO = OpsClusterNodeVO.of(opsClusterNodeEntity);
                String hostId = opsClusterNodeEntity.getHostId();
                OpsHostEntity hostEntity = hostEntityMap.get(hostId);
                if (Objects.nonNull(hostEntity)) {
                    OpsHostUserEntity installUser = hostUserEntityMap.get(opsClusterNodeEntity.getInstallUserId());
                    opsClusterNodeVO.setPublicIp(hostEntity.getPublicIp());
                    opsClusterNodeVO.setPrivateIp(hostEntity.getPrivateIp());
                    opsClusterNodeVO.setHostPort(hostEntity.getPort());
                    OpsHostUserEntity rootUser = hostUserEntityMap.get(hostId);
                    opsClusterNodeVO.setIsRemember(
                        Objects.nonNull(rootUser) && StrUtil.isNotEmpty(rootUser.getPassword()));
                    opsClusterNodeVO.setHostname(hostEntity.getHostname());
                    opsClusterNodeVO.setHostId(hostId);
                    opsClusterNodeVO.setDbPort(opsClusterEntity.getPort());
                    opsClusterNodeVO.setDbName("postgres");
                    opsClusterNodeVO.setDbUser(opsClusterEntity.getDatabaseUsername());
                    opsClusterNodeVO.setDbUserPassword(opsClusterEntity.getDatabasePassword());
                    opsClusterNodeVO.setHostOs(hostEntity.getOs());
                    opsClusterNodeVO.setHostCpuArch(hostEntity.getCpuArch());
                    if (OpenGaussVersionEnum.ENTERPRISE == opsClusterEntity.getVersion()) {
                        opsClusterNodeVO.setInstallPath(opsClusterEntity.getInstallPath());
                    }
                    if (Objects.nonNull(installUser)) {
                        opsClusterNodeVO.setInstallUserName(installUser.getUsername());
                    }
                }
                clusterNodeVOS.add(opsClusterNodeVO);
            }
        }
        return res;
    }

    @Override
    public ClusterSummaryVO summary() {
        ClusterSummaryVO clusterSummaryVO = new ClusterSummaryVO();
        clusterSummaryVO.setClusterNum(count());
        clusterSummaryVO.setHostNum(hostService.count());
        clusterSummaryVO.setNodeNum(opsJdbcDbClusterService.count());
        return clusterSummaryVO;
    }

    @Override
    public void monitor(String clusterId, String hostId, String businessId) {
        OpsClusterEntity clusterEntity = getById(clusterId);
        if (Objects.isNull(clusterEntity)) {
            throw new OpsException("Cluster information does not exist");
        }
        List<OpsClusterNodeEntity> opsClusterNodeEntities = opsClusterNodeService.listClusterNodeByClusterId(clusterId);
        if (CollUtil.isEmpty(opsClusterNodeEntities)) {
            throw new OpsException("Cluster node information does not exist");
        }
        OpsClusterNodeEntity nodeEntity = opsClusterNodeEntities.stream()
            .filter(opsClusterNodeEntity -> opsClusterNodeEntity.getHostId().equals(hostId))
            .findFirst()
            .orElseThrow(() -> new OpsException("Cluster node information does not exist"));
        String installUserId = nodeEntity.getInstallUserId();
        OpsHostEntity hostEntity = hostService.getById(hostId);
        if (Objects.isNull(hostEntity)) {
            throw new OpsException("Node host information does not exist");
        }
        OpsHostUserEntity hostUserEntity = hostUserService.getById(installUserId);
        if (Objects.isNull(hostUserEntity)) {
            throw new OpsException("Node installation user information does not exist");
        }
        final String realDataPath = nodeEntity.getDataPath();
        Future<?> future = threadPoolTaskExecutor.submit(() -> {
            Connection connection = null;
            WsSession wsSession = wsConnectorManager.getSession(businessId)
                .orElseThrow(() -> new OpsException("response session does not exist"));
            try {
                String driver = "org.opengauss.Driver";
                String sourceURL = "jdbc:opengauss://" + hostEntity.getPublicIp() + ":" + clusterEntity.getPort()
                    + "/postgres";
                Properties info = new Properties();
                info.setProperty("user", clusterEntity.getDatabaseUsername());
                info.setProperty("password", encryptionUtils.decrypt(clusterEntity.getDatabasePassword()));
                try {
                    Class.forName(driver);
                    connection = DriverManager.getConnection(sourceURL, info);
                    // create ssH login
                    SshLogin sshLogin = new SshLogin(hostEntity.getPublicIp(), hostEntity.getPort(),
                        hostUserEntity.getUsername(), encryptionUtils.decrypt(hostUserEntity.getPassword()));
                    MonitorParam monitorParam = new MonitorParam(hostEntity.getHostId(), realDataPath,
                        clusterEntity.getVersion(), clusterEntity.getEnvPath());
                    doMonitor(wsSession, sshLogin, monitorParam, connection);
                } catch (Exception e) {
                    log.error("Failed to obtain a connection {}", e.getMessage());
                }
            } finally {
                if (Objects.nonNull(connection)) {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                    }
                }
                wsUtil.close(wsSession);
            }

        });
        TaskManager.registry(businessId, future);
    }

    @Override
    public Map<String, Integer> threadPoolMonitor() {
        Map<String, Integer> res = new HashMap<>();
        int activeCount = threadPoolTaskExecutor.getActiveCount();
        int poolSize = threadPoolTaskExecutor.getPoolSize();
        int corePoolSize = threadPoolTaskExecutor.getCorePoolSize();
        int keepAliveSeconds = threadPoolTaskExecutor.getKeepAliveSeconds();
        res.put("activeCount", activeCount);
        res.put("poolSize", poolSize);
        res.put("corePoolSize", corePoolSize);
        res.put("keepAliveSeconds", keepAliveSeconds);
        return res;
    }

    @Override
    public long countByHostId(String hostId) {
        return opsClusterNodeService.countByHostId(hostId);
    }

    private CheckVO parseCheckResToCheckVO(String result) {
        CheckVO checkVO = new CheckVO();
        // cluster
        CheckClusterVO checkClusterVO = new CheckClusterVO();
        checkClusterVO.setCheckClusterState(parseCheckItem(result, "CheckClusterState"));
        checkClusterVO.setCheckDBParams(parseCheckItem(result, "CheckDBParams"));
        checkClusterVO.setCheckDebugSwitch(parseCheckItem(result, "CheckDebugSwitch"));
        checkClusterVO.setCheckDirPermissions(parseCheckItem(result, "CheckDirPermissions"));
        checkClusterVO.setCheckEnvProfile(parseCheckItem(result, "CheckEnvProfile"));
        checkClusterVO.setCheckReadonlyMode(parseCheckItem(result, "CheckReadonlyMode"));
        checkClusterVO.setCheckDilateSysTab(parseCheckItem(result, "CheckDilateSysTab"));
        checkClusterVO.setCheckProStartTime(parseCheckItem(result, "CheckProStartTime"));
        checkClusterVO.setCheckMpprcFile(parseCheckItem(result, "CheckMpprcFile"));
        checkVO.setCluster(checkClusterVO);
        // database
        CheckDbVO checkDbVO = new CheckDbVO();
        checkDbVO.setCheckCurConnCount(parseCheckItem(result, "CheckCurConnCount"));
        checkDbVO.setCheckPgxcgroup(parseCheckItem(result, "CheckPgxcgroup"));
        checkDbVO.setCheckCursorNum(parseCheckItem(result, "CheckCursorNum"));
        checkDbVO.setCheckNodeGroupName(parseCheckItem(result, "CheckNodeGroupName"));
        checkDbVO.setCheckTableSpace(parseCheckItem(result, "CheckTableSpace"));
        checkDbVO.setCheckSysadminUser(parseCheckItem(result, "CheckSysadminUser"));
        checkDbVO.setCheckHashIndex(parseCheckItem(result, "CheckHashIndex"));
        checkDbVO.setCheckPgxcRedistb(parseCheckItem(result, "CheckPgxcRedistb"));
        checkDbVO.setCheckNodeGroupName(parseCheckItem(result, "CheckNodeGroupName"));
        checkDbVO.setCheckTDDate(parseCheckItem(result, "CheckTDDate"));
        checkVO.setDb(checkDbVO);
        // os
        CheckOSVO checkOSVO = new CheckOSVO();
        checkOSVO.setCheckEncoding(parseCheckItem(result, "CheckEncoding"));
        checkOSVO.setCheckFirewall(parseCheckItem(result, "CheckFirewall"));
        checkOSVO.setCheckKernelVer(parseCheckItem(result, "CheckKernelVer"));
        checkOSVO.setCheckMaxHandle(parseCheckItem(result, "CheckMaxHandle"));
        checkOSVO.setCheckNTPD(parseCheckItem(result, "CheckNTPD"));
        checkOSVO.setCheckOSVer(parseCheckItem(result, "CheckOSVer"));
        checkOSVO.setCheckSysParams(parseCheckItem(result, "CheckSysParams"));
        checkOSVO.setCheckTHP(parseCheckItem(result, "CheckTHP"));
        checkOSVO.setCheckTimeZone(parseCheckItem(result, "CheckTimeZone"));
        checkOSVO.setCheckCPU(parseCheckItem(result, "CheckCPU"));
        checkOSVO.setCheckSshdService(parseCheckItem(result, "CheckSshdService"));
        checkOSVO.setCheckSshdConfig(parseCheckItem(result, "CheckSshdConfig"));
        checkOSVO.setCheckCrondService(parseCheckItem(result, "CheckCrondService"));
        checkOSVO.setCheckStack(parseCheckItem(result, "CheckStack"));
        checkOSVO.setCheckSysPortRange(parseCheckItem(result, "CheckSysPortRange"));
        checkOSVO.setCheckMemInfo(parseCheckItem(result, "CheckMemInfo"));
        checkOSVO.setCheckHyperThread(parseCheckItem(result, "CheckHyperThread"));
        checkOSVO.setCheckMaxProcMemory(parseCheckItem(result, "CheckMaxProcMemory"));
        checkOSVO.setCheckBootItems(parseCheckItem(result, "CheckBootItems"));
        checkOSVO.setCheckKeyProAdj(parseCheckItem(result, "CheckKeyProAdj"));
        checkOSVO.setCheckFilehandle(parseCheckItem(result, "CheckFilehandle"));
        checkOSVO.setCheckDropCache(parseCheckItem(result, "CheckDropCache"));
        checkVO.setOs(checkOSVO);
        // device
        CheckDeviceVO checkDeviceVO = new CheckDeviceVO();
        checkDeviceVO.setCheckBlockdev(parseCheckItem(result, "CheckBlockdev"));
        checkDeviceVO.setCheckDiskFormat(parseCheckItem(result, "CheckDiskFormat"));
        checkDeviceVO.setCheckSpaceUsage(parseCheckItem(result, "CheckSpaceUsage"));
        checkDeviceVO.setCheckInodeUsage(parseCheckItem(result, "CheckInodeUsage"));
        checkDeviceVO.setCheckSwapMemory(parseCheckItem(result, "CheckSwapMemory"));
        checkDeviceVO.setCheckLogicalBlock(parseCheckItem(result, "CheckLogicalBlock"));
        checkDeviceVO.setCheckIOrequestqueue(parseCheckItem(result, "CheckIOrequestqueue"));
        checkDeviceVO.setCheckMaxAsyIOrequests(parseCheckItem(result, "CheckMaxAsyIOrequests"));
        checkDeviceVO.setCheckIOConfigure(parseCheckItem(result, "CheckIOConfigure"));
        checkVO.setDevice(checkDeviceVO);
        // network
        CheckNetworkVO checkNetworkVO = new CheckNetworkVO();
        checkNetworkVO.setCheckMTU(parseCheckItem(result, "CheckMTU"));
        checkNetworkVO.setCheckPing(parseCheckItem(result, "CheckPing"));
        checkNetworkVO.setCheckRXTX(parseCheckItem(result, "CheckRXTX"));
        checkNetworkVO.setCheckNetWorkDrop(parseCheckItem(result, "CheckNetWorkDrop"));
        checkNetworkVO.setCheckMultiQueue(parseCheckItem(result, "CheckMultiQueue"));
        checkNetworkVO.setCheckRouting(parseCheckItem(result, "CheckRouting"));
        checkNetworkVO.setCheckNICModel(parseCheckItem(result, "CheckNICModel"));
        checkVO.setNetwork(checkNetworkVO);
        return checkVO;
    }

    private CheckItemVO parseCheckItem(String result, String item) {
        item = item + ".";
        CheckItemVO checkItemVO = new CheckItemVO();
        checkItemVO.setName(item);
        int indexStart = result.indexOf(item);
        int reIndex = result.indexOf("\n", indexStart);
        int indexEnd = result.indexOf(".............", reIndex);
        if ("CheckMpprcFile.".equals(item)) {
            indexEnd = result.indexOf("Analysis the check result", reIndex);
        }
        if (indexStart == -1) {
            log.warn("gs check item {} not found", item);
            return checkItemVO;
        }
        if (indexEnd <= indexStart) {
            log.error("index end le index start");
            return checkItemVO;
        }
        String substring = result.substring(indexStart, indexEnd);
        if (StrUtil.isNotEmpty(substring)) {
            String[] split = substring.split("\n");
            if (split.length < 2) {
                return checkItemVO;
            }
            StringBuffer msg = new StringBuffer();
            for (int i = 0; i < split.length; i++) {
                String line = split[i];
                if (i == 0) {
                    String status = line.substring(line.lastIndexOf(".") + 1, line.indexOf("\r"));
                    checkItemVO.setStatus(status);
                } else {
                    if (StrUtil.isNotEmpty(line.trim())) {
                        msg.append(line).append("\n");
                    } else {
                        break;
                    }
                }
            }
            checkItemVO.setMsg(msg.toString());
        }
        return checkItemVO;
    }

    private void doMonitor(WsSession wsSession, SshLogin sshLogin, MonitorParam monitorParam, Connection connection) {
        AtomicBoolean hasError = new AtomicBoolean(false);
        while (wsSession.getSession().isOpen() && !hasError.get()) {
            NodeMonitorVO nodeMonitorVO = new NodeMonitorVO();
            CountDownLatch countDownLatch = new CountDownLatch(2);
            threadPoolTaskExecutor.submit(() -> {
                try {
                    nodeMonitorVO.setTime(System.currentTimeMillis());
                    nodeMonitorVO.setCpu(cpu(monitorParam.getHostId()));
                    nodeMonitorVO.setMemory(memory(monitorParam.getHostId()));
                    nodeMonitorVO.setNet(net(monitorParam.getHostId()));
                    nodeMonitorVO.setKernel(kernel(monitorParam.getHostId()));
                    nodeMonitorVO.setMemorySize(memorySize(monitorParam.getHostId()));
                } catch (Exception e) {
                    log.error("time error : {}", e.getMessage());
                    hasError.set(true);
                } finally {
                    countDownLatch.countDown();
                }
            });
            threadPoolTaskExecutor.submit(() -> {
                try {
                    nodeMonitorVO.setState(state(sshLogin, monitorParam.getVersion(), monitorParam.getDataPath(),
                        monitorParam.getEnvPath()));
                    nodeMonitorVO.setLock(lock(connection));
                    nodeMonitorVO.setSession(session(connection));
                    nodeMonitorVO.setConnectNum(connectNum(connection));
                    nodeMonitorVO.setSessionMemoryTop10(sessionMemoryTop10(connection));
                } catch (Exception e) {
                    log.error("state monitor error : {}", e.getMessage());
                    hasError.set(true);
                } finally {
                    countDownLatch.countDown();
                }
            });
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                log.error("monitor await thread is interrupted {}", e.getMessage());
            }
            wsUtil.sendText(wsSession, JSON.toJSONString(nodeMonitorVO));
            try {
                TimeUnit.SECONDS.sleep(5L);
            } catch (InterruptedException e) {
                log.error("monitor sleep is interrupted {}", e.getMessage());
            }
        }
    }

    /**
     * MonitorParam 类的功能说明
     * <p>
     * 该类用于存储监控参数。
     */
    @AllArgsConstructor
    @Data
    private class MonitorParam {
        private String hostId;
        private String dataPath;
        private OpenGaussVersionEnum version;
        private String envPath;
    }

    private List<Map<String, String>> sessionMemoryTop10(Connection connection) {
        String sql = "SELECT * FROM pv_session_memory_detail() ORDER BY usedsize desc limit 10";
        List<Map<String, String>> res = new ArrayList<>();
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                HashMap<String, String> model = new HashMap<>();
                model.put(resultSet.getString("sessid"), resultSet.getString("usedsize"));
                res.add(model);
            }
        } catch (Exception e) {
            log.error("Failed to query session memory top10 {}", e.getMessage());
            throw new OpsException("Failed to query session memory top10");
        }
        return res;
    }

    @Override
    public String connectNum(Connection connection) {
        String sql = "SELECT count(*) FROM (SELECT pg_stat_get_backend_idset() AS backendid) AS s";
        String res = null;
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
            if (resultSet.next()) {
                res = resultSet.getString("count");
            }
        } catch (Exception e) {
            log.error("Failed to query the number of connections  {}", e.getMessage());
            throw new OpsException("Failed to query the number of connections");
        }
        return res;
    }

    @Override
    public String session(Connection connection) {
        String sql = "SELECT count(*) FROM pg_stat_activity";
        String res = null;
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
            if (resultSet.next()) {
                res = resultSet.getString("count");
            }
        } catch (Exception e) {
            log.error("Failed to query the number of connections {}", e.getMessage());
            throw new OpsException("Failed to query the number of connections");
        }
        return res;
    }

    @Override
    public String lock(Connection connection) {
        String sql = "SELECT count(*) FROM pg_locks";
        String res = null;
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
            if (resultSet.next()) {
                res = resultSet.getString("count");
            }
        } catch (Exception e) {
            log.error("Failed to query the number of connections {}", e.getMessage());
            throw new OpsException("Failed to query the number of connections");
        }
        return res;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CheckSummaryVO check(String clusterId, String rootPassword) {
        OpsClusterEntity clusterEntity = getById(clusterId);
        if (clusterEntity.getVersionNum().equals("5.1.0")) {
            throw new OpsException("Unsupported openGauss version: 5.1.0");
        }
        if (Objects.isNull(clusterEntity)) {
            throw new OpsException("Cluster information cannot be empty");
        }
        if (OpenGaussVersionEnum.ENTERPRISE != clusterEntity.getVersion()) {
            throw new OpsException("Only Enterprise edition is supported");
        }
        List<OpsClusterNodeEntity> opsClusterNodeEntities = opsClusterNodeService.listClusterNodeByClusterId(clusterId);
        if (CollUtil.isEmpty(opsClusterNodeEntities)) {
            throw new OpsException("Cluster node information cannot be empty");
        }
        OpsClusterNodeEntity nodeEntity = opsClusterNodeEntities.stream()
            .filter(node -> node.getClusterRole() == ClusterRoleEnum.MASTER)
            .findFirst()
            .orElseThrow(() -> new OpsException("Masternode information not found"));
        String hostId = nodeEntity.getHostId();
        String installUserId = nodeEntity.getInstallUserId();
        OpsHostEntity hostEntity = hostService.getById(hostId);
        if (Objects.isNull(hostEntity)) {
            throw new OpsException("host information does not exist");
        }
        OpsHostUserEntity hostUserEntity = hostUserService.getById(installUserId);
        if (Objects.isNull(hostUserEntity)) {
            throw new OpsException("Installation user information does not exist");
        }
        SshLogin sshLogin = new SshLogin(hostEntity.getPublicIp(), hostEntity.getPort(), hostUserEntity.getUsername(),
            encryptionUtils.decrypt(hostUserEntity.getPassword()));
        String res;
        String rootPwd = getRootPassword(hostId, rootPassword);
        if (hasRootPassword(rootPwd)) {
            res = doCheckWithRootItems(rootPwd, sshLogin, clusterEntity.getEnvPath());
        } else {
            // 如果没有root密码，则跳过root检查项
            res = doCheckSkipRootItems(sshLogin, clusterEntity.getEnvPath());
        }
        OpsCheckEntity opsCheckEntity = new OpsCheckEntity();
        opsCheckEntity.setCheckRes(res);
        opsCheckEntity.setClusterId(clusterId);
        opsCheckService.save(opsCheckEntity);
        CheckVO checkVO = parseCheckResToCheckVO(res);
        return CheckSummaryVO.of(checkVO);
    }

    private boolean hasRootPassword(String rootPassword) {
        return StrUtil.isNotEmpty(rootPassword);
    }

    private String getRootPassword(String hostId, String rootPassword) {
        OpsHostUserEntity rootUserEntity = hostUserService.getRootUserByHostId(hostId);
        String pwd = Objects.nonNull(rootUserEntity) ? rootUserEntity.getPassword() : "";
        return StrUtil.isNotEmpty(pwd) ? pwd : rootPassword;
    }

    private String doCheckWithRootItems(String rootPassword, SshLogin sshLogin, String envPath) {
        String command = "gs_check -e inspect";
        Map<String, String> autoResponse = new HashMap<>();
        autoResponse.put("Please enter root privileges user[root]:", "root");
        autoResponse.put("Please enter password for user[root]:", encryptionUtils.decrypt(rootPassword));
        return doCheckItems(sshLogin, command, envPath, autoResponse);
    }

    private String doCheckSkipRootItems(SshLogin sshLogin, String envPath) {
        String command = "gs_check -e inspect --skip-root-items";
        Map<String, String> autoResponse = new HashMap<>();
        return doCheckItems(sshLogin, command, envPath, autoResponse);
    }

    private String doCheckItems(SshLogin sshLogin, String command, String envPath, Map<String, String> autoResponse) {
        log.info("One-click self-test start");
        try {
            return jschExecutorService.execCommandAutoResponse(sshLogin, command, envPath, autoResponse);
        } catch (Exception e) {
            log.error("One-click self-test results {}", e.getMessage());
            throw new OpsException("One key self-test error");
        } finally {
            log.info("One-click self-test end");
        }
    }

    private String memorySize(String hostId) {
        return hostMonitorCacheService.getMemoryTotal(hostId);
    }

    private String kernel(String hostId) {
        return hostMonitorCacheService.getCpuCoreNum(hostId);
    }

    private String state(SshLogin sshLogin, OpenGaussVersionEnum version, String dataPath, String envPath) {
        if (OpenGaussVersionEnum.ENTERPRISE == version) {
            String command = "gs_om -t status --detail";
            Map<String, Object> res = new HashMap<>();
            String result = jschExecutorService.execCommand(sshLogin, command, envPath);
            int clusterStateIndex = result.indexOf("cluster_state");
            String clusterState = null;
            if (clusterStateIndex >= 1) {
                int splitIndex = result.indexOf(":", clusterStateIndex);
                int lineEndIndex = result.indexOf("\n", clusterStateIndex);
                clusterState = result.substring(splitIndex + 1, lineEndIndex).trim();
                res.put("cluster_state", clusterState);
            }
            return parseNodeStateAndRole(result, res);
        } else {
            String command = "gs_ctl status -D " + dataPath;
            String result = jschExecutorService.execCommand(sshLogin, command, envPath);
            return result.contains("no server running") ? "false" : "true";
        }
    }

    private static String parseNodeStateAndRole(String result, Map<String, Object> res) {
        Map<String, String> nodeState = new HashMap<>(1);
        Map<String, String> nodeRole = new HashMap<>(1);
        int datanodeStateIndex = result.indexOf("Datanode State");
        if (datanodeStateIndex >= 1) {
            int splitIndex = result.indexOf("------------------", datanodeStateIndex);
            String dataNodeStateStr = result.substring(splitIndex);
            String[] dataNode = dataNodeStateStr.split("\n");
            parseNodeStateAndRole(dataNode, nodeState, nodeRole);
        }
        res.put("nodeState", nodeState);
        res.put("nodeRole", nodeRole);
        return JSON.toJSONString(res);
    }

    private static void parseNodeStateAndRole(String[] dataNode, Map<String, String> nodeState,
        Map<String, String> nodeRole) {
        for (String s : dataNode) {
            String[] s1 = s.replaceAll(" +", " ").split(" ");
            String state = "";
            if (s1.length >= 9) {
                for (int i = 8; i < s1.length; i++) {
                    state += (s1[i] + " ");
                }
                nodeState.put(s1[1], state.trim());
                nodeRole.put(s1[1], s1[7]);
            } else if (s1.length >= 8) {
                for (int i = 7; i < s1.length; i++) {
                    state += (s1[i] + " ");
                }
                nodeState.put(s1[1], state.trim());
                nodeRole.put(s1[1], s1[6]);
            }
        }
    }

    private List<NodeNetMonitor> net(String hostId) {
        List<NodeNetMonitor> res = new ArrayList<>();
        String[] netMonitor = hostMonitorCacheService.getNetMonitor(hostId, true);
        NodeNetMonitor nodeNetMonitor = new NodeNetMonitor();
        nodeNetMonitor.setFace(StrUtil.trim(netMonitor[0]));
        nodeNetMonitor.setReceive(netMonitor[1]);
        nodeNetMonitor.setTransmit(netMonitor[2]);
        res.add(nodeNetMonitor);
        return res;
    }

    private String memory(String hostId) {
        return hostMonitorCacheService.getMemoryUsing(hostId);
    }

    private String cpu(String hostId) {
        return hostMonitorCacheService.getCpuUsing(hostId);
    }
}
