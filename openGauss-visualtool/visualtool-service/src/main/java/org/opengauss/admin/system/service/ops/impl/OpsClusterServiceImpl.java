package org.opengauss.admin.system.service.ops.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.constant.ops.SshCommandConstants;
import org.opengauss.admin.common.core.domain.entity.ops.*;
import org.opengauss.admin.common.core.domain.model.ops.*;
import org.opengauss.admin.common.core.domain.model.ops.check.*;
import org.opengauss.admin.common.core.handler.ops.cache.TaskManager;
import org.opengauss.admin.common.core.handler.ops.cache.WsConnectorManager;
import org.opengauss.admin.common.enums.ops.ClusterRoleEnum;
import org.opengauss.admin.common.enums.ops.DeployTypeEnum;
import org.opengauss.admin.common.enums.ops.OpenGaussVersionEnum;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.common.utils.ops.JschUtil;
import org.opengauss.admin.common.utils.ops.WsUtil;
import org.opengauss.admin.system.mapper.ops.OpsClusterMapper;
import org.opengauss.admin.system.service.ops.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author lhf
 * @date 2022/8/6 17:38
 **/
@Slf4j
@Service
public class OpsClusterServiceImpl extends ServiceImpl<OpsClusterMapper, OpsClusterEntity> implements IOpsClusterService {
    @Autowired
    private IHostService hostService;
    @Autowired
    private IHostUserService hostUserService;
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Autowired
    private IOpsClusterNodeService opsClusterNodeService;
    @Autowired
    private IOpsAzService opsAzService;
    @Autowired
    private IOpsCheckService opsCheckService;
    @Autowired
    private JschUtil jschUtil;
    @Autowired
    private WsConnectorManager wsConnectorManager;
    @Autowired
    private WsUtil wsUtil;
    @Autowired
    private EncryptionUtils encryptionUtils;


    @Override
    public List<OpsClusterVO> listCluster() {
        List<OpsClusterVO> res = new ArrayList<>();
        List<OpsClusterEntity> opsClusterEntities = list(Wrappers.lambdaQuery(OpsClusterEntity.class).orderByDesc(OpsClusterEntity::getCreateTime));
        List<String> clusterIds = opsClusterEntities.stream().map(OpsClusterEntity::getClusterId).collect(Collectors.toList());
        Map<String, OpsCheckEntity> checkResMap = opsCheckService.mapLastResByClusterIds(clusterIds);
        Map<String, List<OpsClusterNodeEntity>> clusterNodeMap = opsClusterNodeService.listClusterNodeByClusterIds(clusterIds);
        Map<String, OpsHostEntity> hostEntityMap = new HashMap<>();
        List<String> hostIds = clusterNodeMap.values().stream().flatMap(nodeList -> nodeList.stream()).map(OpsClusterNodeEntity::getHostId).collect(Collectors.toList());
        Map<String, OpsAzEntity> azEntityMap = new HashMap<>();
        Map<String, OpsHostUserEntity> hostUserEntityMap = new HashMap<>();
        Map<String, OpsHostUserEntity> rootUserMap = new HashMap<>();
        if (CollUtil.isNotEmpty(hostIds)) {
            List<OpsHostEntity> opsHostEntities = hostService.listByIds(hostIds);
            if (CollUtil.isNotEmpty(opsHostEntities)) {
                hostEntityMap.putAll(opsHostEntities.stream().collect(Collectors.toMap(OpsHostEntity::getHostId, Function.identity())));
                List<String> azIds = opsHostEntities.stream().map(OpsHostEntity::getAzId).collect(Collectors.toList());
                if (CollUtil.isNotEmpty(azIds)) {
                    List<OpsAzEntity> opsAzEntities = opsAzService.listByIds(azIds);
                    azEntityMap.putAll(opsAzEntities.stream().collect(Collectors.toMap(OpsAzEntity::getAzId, Function.identity())));
                }

                List<OpsHostUserEntity> opsHostUserEntities = hostUserService.listHostUserByHostIdList(opsHostEntities.stream().map(OpsHostEntity::getHostId).collect(Collectors.toList()));
                hostUserEntityMap.putAll(opsHostUserEntities.stream().collect(Collectors.toMap(OpsHostUserEntity::getHostUserId, Function.identity())));
                hostUserEntityMap.putAll(opsHostUserEntities.stream().filter(hostUserEntity -> "root".equals(hostUserEntity.getUsername())).collect(Collectors.toMap(OpsHostUserEntity::getHostId, Function.identity())));
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
                    OpsHostUserEntity rootUser = hostUserEntityMap.get(hostEntity.getHostId());
                    OpsHostUserEntity installUser = hostUserEntityMap.get(opsClusterNodeEntity.getInstallUserId());
                    opsClusterNodeVO.setPublicIp(hostEntity.getPublicIp());
                    opsClusterNodeVO.setPrivateIp(hostEntity.getPrivateIp());
                    opsClusterNodeVO.setHostPort(hostEntity.getPort());
                    opsClusterNodeVO.setRootPassword(rootUser.getPassword());
                    opsClusterNodeVO.setHostname(hostEntity.getHostname());
                    opsClusterNodeVO.setHostId(hostEntity.getHostId());
                    opsClusterNodeVO.setDbPort(opsClusterEntity.getPort());
                    opsClusterNodeVO.setDbName("postgres");
                    opsClusterNodeVO.setDbUser(opsClusterEntity.getDatabaseUsername());
                    opsClusterNodeVO.setDbUserPassword(opsClusterEntity.getDatabasePassword());

                    if (Objects.nonNull(installUser)) {
                        opsClusterNodeVO.setInstallUserName(installUser.getUsername());
                    }

                    String azId = hostEntity.getAzId();
                    OpsAzEntity azEntity = azEntityMap.get(azId);
                    if (Objects.nonNull(azEntity)) {
                        opsClusterNodeVO.setAzName(azEntity.getName());
                        opsClusterNodeVO.setAzAddress(azEntity.getAddress());
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
        clusterSummaryVO.setNodeNum(opsClusterNodeService.count());
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

        OpsClusterNodeEntity nodeEntity = opsClusterNodeEntities.stream().filter(opsClusterNodeEntity -> opsClusterNodeEntity.getHostId().equals(hostId)).findFirst().orElseThrow(() -> new OpsException("Cluster node information does not exist"));

        String installUserId = nodeEntity.getInstallUserId();

        OpsHostEntity hostEntity = hostService.getById(hostId);
        if (Objects.isNull(hostEntity)) {
            throw new OpsException("Node host information does not exist");
        }
        OpsHostUserEntity hostUserEntity = hostUserService.getById(installUserId);
        if (Objects.isNull(hostUserEntity)) {
            throw new OpsException("Node installation user information does not exist");
        }

        OpsHostUserEntity rootUserEntity = hostUserService.getRootUserByHostId(hostId);
        if (Objects.isNull(rootUserEntity)) {
            throw new OpsException("Node root user information does not exist");
        }

        WsSession wsSession = wsConnectorManager.getSession(businessId).orElseThrow(() -> new OpsException("response session does not exist"));
        Session ommSession = jschUtil.getSession(hostEntity.getPublicIp(), hostEntity.getPort(), hostUserEntity.getUsername(), encryptionUtils.decrypt(hostUserEntity.getPassword())).orElseThrow(() -> new OpsException("Install user connection failed"));

        String dataPath = nodeEntity.getDataPath();
        if (OpenGaussVersionEnum.MINIMAL_LIST == clusterEntity.getVersion()) {
            if (clusterEntity.getDeployType() == DeployTypeEnum.CLUSTER) {
                if (nodeEntity.getClusterRole() == ClusterRoleEnum.MASTER) {
                    dataPath = dataPath + "/master";
                } else {
                    dataPath = dataPath + "/slave";
                }
            } else {
                dataPath = dataPath + "/single_node";
            }
        }


        final String realDataPath = dataPath;
        Future<?> future = threadPoolTaskExecutor.submit(() -> {
            Connection connection = null;
            try {
                String driver = "org.opengauss.Driver";
                String sourceURL = "jdbc:opengauss://" + hostEntity.getPublicIp() + ":" + clusterEntity.getPort() + "/postgres";
                Properties info = new Properties();
                info.setProperty("user", clusterEntity.getDatabaseUsername());
                info.setProperty("password", clusterEntity.getDatabasePassword());
                try {
                    Class.forName(driver);
                    connection = DriverManager.getConnection(sourceURL, info);
                } catch (Exception e) {
                    log.error("Failed to obtain a connection", e);
                    throw new OpsException("Failed to obtain a connection");
                }
                doMonitor(wsSession, ommSession, clusterEntity.getVersion(), connection, realDataPath);
            } finally {
                if (Objects.nonNull(connection)) {
                    try {
                        connection.close();
                    } catch (SQLException e) {

                    }
                }

                if (Objects.nonNull(ommSession) && ommSession.isConnected()) {
                    ommSession.disconnect();
                }

                if (wsSession!=null && Objects.nonNull(wsSession.getSession())){
                    try {
                        wsSession.getSession().close();
                    } catch (IOException ignore) {
                        log.error("close websocket session error ",ignore);
                    }
                }
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

    private void doMonitor(WsSession wsSession, Session ommSession, OpenGaussVersionEnum version, Connection connection, String dataPath) {
        while (wsSession.getSession().isOpen()) {
            NodeMonitorVO nodeMonitorVO = new NodeMonitorVO();
            CountDownLatch countDownLatch = new CountDownLatch(11);
            threadPoolTaskExecutor.submit(() -> {
                try {
                    nodeMonitorVO.setTime(System.currentTimeMillis());
                }catch (Exception e){
                    log.error("time error : ",e);
                }finally {
                    countDownLatch.countDown();
                }

            });

            threadPoolTaskExecutor.submit(() -> {
                try {
                    nodeMonitorVO.setCpu(cpu(ommSession));
                }catch (Exception e){
                    log.error("cpu monitor error : ",e);
                }finally {
                    countDownLatch.countDown();
                }
            });

            threadPoolTaskExecutor.submit(() -> {
                try {
                    nodeMonitorVO.setMemory(memory(ommSession));
                }catch (Exception e){
                    log.error("memory monitor error : ",e);
                }finally {
                    countDownLatch.countDown();
                }
            });

            threadPoolTaskExecutor.submit(() -> {
                try {
                    nodeMonitorVO.setNet(net(ommSession));
                }catch (Exception e){
                    log.error("net monitor error : ",e);
                }finally {
                    countDownLatch.countDown();
                }
            });

            threadPoolTaskExecutor.submit(() -> {
                try {
                    nodeMonitorVO.setState(state(ommSession, version, dataPath));
                }catch (Exception e){
                    log.error("state monitor error : ",e);
                }finally {
                    countDownLatch.countDown();
                }
            });

            threadPoolTaskExecutor.submit(() -> {
                try {
                    nodeMonitorVO.setLock(lock(connection));
                }catch (Exception e){
                    log.error("lock monitor error : ",e);
                }finally {
                    countDownLatch.countDown();
                }
            });

            threadPoolTaskExecutor.submit(() -> {
                try {
                    nodeMonitorVO.setSession(session(connection));
                }catch (Exception e){
                    log.error("session monitor error : ",e);
                }finally {
                    countDownLatch.countDown();
                }
            });

            threadPoolTaskExecutor.submit(() -> {
                try {
                    nodeMonitorVO.setConnectNum(connectNum(connection));
                }catch (Exception e){
                    log.error("connectNum monitor error : ",e);
                }finally {
                    countDownLatch.countDown();
                }
            });

            threadPoolTaskExecutor.submit(() -> {
                try {
                    nodeMonitorVO.setSessionMemoryTop10(sessionMemoryTop10(connection));
                }catch (Exception e){
                    log.error("sessionMemoryTop10 monitor error : ",e);
                }finally {
                    countDownLatch.countDown();
                }
            });

            threadPoolTaskExecutor.submit(() -> {
                try {
                    nodeMonitorVO.setKernel(kernel(ommSession));
                }catch (Exception e){
                    log.error("kernel monitor error : ",e);
                }finally {
                    countDownLatch.countDown();
                }
            });

            threadPoolTaskExecutor.submit(() -> {
                try {
                    nodeMonitorVO.setMemorySize(memorySize(ommSession));
                }catch (Exception e){
                    log.error("memorySize monitor error : ",e);
                }finally {
                    countDownLatch.countDown();
                }
            });

            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                log.error("waiting for thread to be interrupted", e);
            }

            wsUtil.sendText(wsSession, JSON.toJSONString(nodeMonitorVO));
            try {
                TimeUnit.SECONDS.sleep(1L);
            } catch (InterruptedException e) {
                throw new OpsException("thread is interrupted");
            }
        }

    }

    private List<Map<String, String>> sessionMemoryTop10(Connection connection) {
        String sql = "SELECT * FROM pv_session_memory_detail() ORDER BY usedsize desc limit 10";
        List<Map<String, String>> res = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                HashMap<String, String> model = new HashMap<>();
                model.put(resultSet.getString("sessid"), resultSet.getString("usedsize"));
                res.add(model);
            }
        } catch (Exception e) {
            log.error("Failed to query session memory top10", e);
        }

        return res;
    }

    private String connectNum(Connection connection) {
        String sql = "SELECT count(*) FROM (SELECT pg_stat_get_backend_idset() AS backendid) AS s";
        String res = null;
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            if (resultSet.next()) {
                res = resultSet.getString("count");
            }
        } catch (Exception e) {
            log.error("Failed to query the number of connections", e);
        }

        return res;
    }

    private String session(Connection connection) {
        String sql = "SELECT count(*) FROM pg_stat_activity";
        String res = null;
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            if (resultSet.next()) {
                res = resultSet.getString("count");
            }
        } catch (Exception e) {
            log.error("Failed to query the number of connections", e);
        }

        return res;
    }

    private String lock(Connection connection) {
        String sql = "SELECT count(*) FROM pg_locks";
        String res = null;
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            if (resultSet.next()) {
                res = resultSet.getString("count");
            }
        } catch (Exception e) {
            log.error("Failed to query the number of connections", e);
        }

        return res;
    }

    private String memorySize(Session rootSession) {
        try {
            JschResult jschResult = jschUtil.executeCommand(SshCommandConstants.MEMORY_TOTAL, rootSession);
            if (0 != jschResult.getExitCode()) {
                log.error("Query memory size failed, exit code: {}, message: {}", jschResult.getExitCode(), jschResult.getResult());
            } else {
                return jschResult.getResult();
            }
        } catch (Exception e) {
            log.error("Failed to query memory size", e);
        }
        return null;
    }

    private String kernel(Session rootSession) {
        try {
            JschResult jschResult = jschUtil.executeCommand(SshCommandConstants.CPU_CORE_NUM, rootSession);
            if (0 != jschResult.getExitCode()) {
                log.error("Failed to query core count, exit code: {}, message: {}", jschResult.getExitCode(), jschResult.getResult());
            } else {
                return jschResult.getResult();
            }
        } catch (Exception e) {
            log.error("Failed to query the number of cores", e);
        }
        return null;
    }

    private String state(Session ommSession, OpenGaussVersionEnum version, String dataPath) {
        if (OpenGaussVersionEnum.ENTERPRISE == version) {
            String command = "gs_om -t status --detail";
            JschResult jschResult = null;
            try {
                Map<String, Object> res = new HashMap<>();
                try {
                    jschResult = jschUtil.executeCommand(command, ommSession);
                } catch (InterruptedException e) {
                    throw new OpsException("thread is interrupted");
                }
                if (0 != jschResult.getExitCode()) {
                    log.error("Failed to get status info, exit code: {}, log: {}", jschResult.getExitCode(), jschResult.getResult());
                    throw new OpsException("Failed to get status information");
                }

                String result = jschResult.getResult();
                int clusterStateIndex = result.indexOf("cluster_state");
                String clusterState = null;
                if (clusterStateIndex < 1) {

                } else {
                    int splitIndex = result.indexOf(":", clusterStateIndex);
                    int lineEndIndex = result.indexOf("\n", clusterStateIndex);
                    clusterState = result.substring(splitIndex + 1, lineEndIndex).trim();
                    res.put("cluster_state", clusterState);
                }

                Map<String,String> nodeState = new HashMap<>(1);
                Map<String,String> nodeRole = new HashMap<>(1);
                res.put("nodeState",nodeState);
                res.put("nodeRole",nodeRole);


                int datanodeStateIndex = result.indexOf("Datanode State");
                if (datanodeStateIndex < 1) {

                } else {
                    int splitIndex = result.indexOf("------------------", datanodeStateIndex);
                    String dataNodeStateStr = result.substring(splitIndex);

                    String[] dataNode = dataNodeStateStr.split("\n");
                    for (String s : dataNode) {
                        String[] s1 = s.replaceAll(" +", " ").split(" ");
                        if (s1.length == 9) {
                            nodeState.put(s1[1], s1[8].trim());
                            nodeRole.put(s1[1],s1[6].trim());
                        }
                    }
                }
                return JSON.toJSONString(res);
            } catch (IOException e) {
                log.error("Failed to get status information", e);
            }
        } else {
            String command = "gs_ctl status -D " + dataPath;
            JschResult jschResult = null;
            try {
                try {
                    jschResult = jschUtil.executeCommand(command, ommSession);
                } catch (InterruptedException e) {
                    throw new OpsException("thread is interrupted");
                }
                if (0 != jschResult.getExitCode()) {
                    log.error("Failed to get status info, exit code: {}, log: {}", jschResult.getExitCode(), jschResult.getResult());
                    throw new OpsException("Failed to get status information");
                }

                String result = jschResult.getResult();
                if (result.contains("no server running")) {
                    return "false";
                } else {
                    return "true";
                }
            } catch (IOException e) {
                log.error("Failed to get status information", e);
            }
        }
        return null;
    }

    private List<NodeNetMonitor> net(Session rootSession) {
        List<NodeNetMonitor> res = new ArrayList<>();
        String command = SshCommandConstants.NET;
        JschResult jschResult = null;
        try {
            try {
                jschResult = jschUtil.executeCommand(command, rootSession);
            } catch (InterruptedException e) {
                throw new OpsException("thread is interrupted");
            }
            if (0 != jschResult.getExitCode()) {
                log.error("Failed to get network info, exit code: {}, log: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("Failed to get network information");
            }

            String result = jschResult.getResult();
            String[] split = result.split("\n");
            for (int i = 2; i < split.length; i++) {
                String s = split[i];
                String[] line = s.split(":");
                NodeNetMonitor nodeNetMonitor = new NodeNetMonitor();
                nodeNetMonitor.setFace(StrUtil.trim(line[0]));
                nodeNetMonitor.setReceive(line[1].trim().replaceAll(" +", " ").split(" ")[0]);
                nodeNetMonitor.setTransmit(line[1].trim().replaceAll(" +", " ").split(" ")[8]);
                res.add(nodeNetMonitor);
            }
        } catch (IOException e) {
            log.error("Failed to get network information", e);
        }

        return res;
    }

    private String memory(Session rootSession) {
        String command = SshCommandConstants.MEMORY_USING;
        JschResult jschResult = null;
        try {
            try {
                jschResult = jschUtil.executeCommand(command, rootSession);
            } catch (InterruptedException e) {
                throw new OpsException("thread is interrupted");
            }
            if (0 != jschResult.getExitCode()) {
                log.error("Getting memory usage failed, exit code: {}, log: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("Failed to get memory usage");
            }

            return jschResult.getResult();
        } catch (IOException e) {
            log.error("Failed to get memory usage", e);
        }

        return null;
    }

    private String cpu(Session rootSession) {
        String command = SshCommandConstants.CPU_USING;
        JschResult jschResult = null;
        try {
            try {
                jschResult = jschUtil.executeCommand(command, rootSession);
            } catch (InterruptedException e) {
                throw new OpsException("thread is interrupted");
            }
            if (0 != jschResult.getExitCode()) {
                log.error("Getting cpu usage failed, exit code: {}, log: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("Failed to get cpu usage");
            }

            return jschResult.getResult();
        } catch (IOException e) {
            log.error("Failed to get cpu usage", e);
        }

        return null;
    }
}
