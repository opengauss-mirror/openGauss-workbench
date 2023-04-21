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
 * ClusterTest.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-api/src/test/java/org/opengauss/admin/service/ops/ClusterTest.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.service.ops;

import com.alibaba.druid.mock.MockConnection;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opengauss.admin.common.core.domain.entity.ops.*;
import org.opengauss.admin.common.core.domain.model.ops.ClusterSummaryVO;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.opengauss.admin.common.core.domain.model.ops.WsSession;
import org.opengauss.admin.common.core.handler.ops.cache.WsConnectorManager;
import org.opengauss.admin.common.enums.ops.ClusterRoleEnum;
import org.opengauss.admin.common.enums.ops.DeployTypeEnum;
import org.opengauss.admin.common.enums.ops.InstallModeEnum;
import org.opengauss.admin.common.enums.ops.OpenGaussVersionEnum;
import org.opengauss.admin.common.utils.ops.JschUtil;
import org.opengauss.admin.system.service.ops.IHostService;
import org.opengauss.admin.system.service.ops.IHostUserService;
import org.opengauss.admin.system.service.ops.IOpsClusterNodeService;
import org.opengauss.admin.system.service.ops.IOpsClusterService;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.opengauss.admin.system.service.ops.impl.OpsCheckService;
import org.opengauss.admin.system.service.ops.impl.OpsClusterServiceImpl;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

/**
 * @author lhf
 * @date 2022/11/27 20:36
 **/
@RunWith(SpringRunner.class)
@Slf4j
public class ClusterTest {
    @InjectMocks
    private IOpsClusterService opsClusterService = new OpsClusterServiceImpl() {
        @Override
        public List<OpsClusterEntity> list(Wrapper<OpsClusterEntity> queryWrapper) {
            return listMockOpsCluster();
        }

        @Override
        public long count() {
            return 0;
        }

        @Override
        public OpsClusterEntity getById(Serializable id) {
            return listMockOpsCluster().get(0);
        }
    };

    @Mock
    private OpsCheckService opsCheckService;
    @Mock
    private IOpsClusterNodeService opsClusterNodeService;
    @Mock
    private IHostService hostService;
    @Mock
    private IHostUserService hostUserService;
    @Mock
    private WsConnectorManager wsConnectorManager;
    @Mock
    private JschUtil jschUtil;
    @Mock
    private EncryptionUtils encryptionUtils;


    @BeforeClass
    public static void before() {
        MockitoAnnotations.initMocks(HostTest.class);
        log.info("start cluster test........");
    }

    @AfterClass
    public static void after() {
        log.info("end cluster test........");
    }


    @Test
    public void testListCluster() {
        Mockito.doReturn(getMockOpsCheckEntity()).when(opsCheckService).getLastResByClusterId(any());
        Mockito.doReturn(listMockOpsCLusterNode()).when(opsClusterNodeService).listClusterNodeByClusterId("1");
        List<OpsClusterVO> opsClusterVOS = opsClusterService.listCluster();
        Assertions.assertNotNull(opsClusterVOS);
        Assertions.assertEquals(opsClusterVOS.size(), listMockOpsCluster().size());
    }

    @Test
    public void testSummary() {
        Mockito.doReturn(1L).when(hostService).count();
        ClusterSummaryVO summary = opsClusterService.summary();
        Assertions.assertNotNull(summary);
        Assertions.assertEquals(1, summary.getHostNum());
    }

    @Test
    public void testMonitor() throws JSchException, SQLException, ClassNotFoundException {
        String clusterId = "1";
        String hostId = "1";
        String businessId = "1";

        ThreadPoolTaskExecutor commonExecutor = new ThreadPoolTaskExecutor();
        commonExecutor.setCorePoolSize(5);
        commonExecutor.setMaxPoolSize(10);
        commonExecutor.setQueueCapacity(1000);
        commonExecutor.setKeepAliveSeconds(300);
        commonExecutor.afterPropertiesSet();
        ReflectionTestUtils.setField(opsClusterService, "threadPoolTaskExecutor", commonExecutor);

        Mockito.doReturn(Optional.of(mockJschSession())).when(jschUtil).getSession(any(), any(), any(), any());
        Mockito.doReturn(Optional.of(mockWsSession())).when(wsConnectorManager).getSession(businessId);
        Mockito.doReturn(getMockHostEntity()).when(hostService).getById(any());
        Mockito.doReturn(hostUserEntityList().get(0)).when(hostUserService).getById(any());
        Mockito.doReturn(hostUserEntityList().get(0)).when(hostUserService).getRootUserByHostId(hostId);
        Mockito.doReturn(listMockOpsCLusterNode()).when(opsClusterNodeService).listClusterNodeByClusterId(clusterId);
        Mockito.doReturn("root").when(encryptionUtils).decrypt(any());
        opsClusterService.monitor(clusterId, hostId, businessId);
    }

    private Connection mockConnection() throws SQLException, ClassNotFoundException {
        Connection connection = new MockConnection();
        return connection;
    }

    private Session mockJschSession() throws JSchException {
        return new JSch().getSession("192.168.0.31");
    }

    private WsSession mockWsSession() {
        WsSession wsSession = new WsSession();
        return wsSession;
    }

    private OpsHostEntity getMockHostEntity() {
        OpsHostEntity hostEntity = new OpsHostEntity();
        hostEntity.setHostId("1");
        hostEntity.setPublicIp("192.168.0.31");
        hostEntity.setPrivateIp("192.168.0.31");
        hostEntity.setPort(22);
        hostEntity.setHostname("node1");
        return hostEntity;
    }

    private List<OpsHostUserEntity> hostUserEntityList() {
        List<OpsHostUserEntity> hostUserEntityList = new ArrayList<>();

        OpsHostUserEntity root = new OpsHostUserEntity();
        root.setHostId("1");
        root.setHostUserId("1");
        root.setUsername("root");
        root.setPassword("Lxgiwyl#0847");
        hostUserEntityList.add(root);

        OpsHostUserEntity omm = new OpsHostUserEntity();
        omm.setHostId("1");
        omm.setHostUserId("2");
        omm.setUsername("omm");
        omm.setPassword("omm");
        hostUserEntityList.add(omm);

        return hostUserEntityList;
    }

    private List<OpsClusterNodeEntity> listMockOpsCLusterNode() {
        List<OpsClusterNodeEntity> mock = new ArrayList<>();
        OpsClusterNodeEntity nodeEntity = new OpsClusterNodeEntity();
        nodeEntity.setClusterNodeId("1");
        nodeEntity.setClusterRole(ClusterRoleEnum.MASTER);
        nodeEntity.setHostId("1");
        nodeEntity.setInstallUserId("1");
        nodeEntity.setDataPath("/opt/openGauss/data");
        mock.add(nodeEntity);
        return mock;
    }

    private OpsCheckEntity getMockOpsCheckEntity() {
        OpsCheckEntity opsCheckEntity = new OpsCheckEntity();
        opsCheckEntity.setCheckId("1");
        opsCheckEntity.setClusterId("1");
        opsCheckEntity.setCheckRes("" +
                "Parsing the check items config file successfully\r\n" +
                "The below items require root privileges to execute:[CheckBlockdev CheckIOrequestqueue CheckIOConfigure CheckMTU CheckRXTX CheckMultiQueue CheckFirewall CheckSshdService CheckSshdConfig CheckCrondService CheckMaxProcMemory CheckBootItems CheckFilehandle CheckNICModel CheckDropCache]\r\n" +
                "Please enter root privileges user[root]:root\r\n" +
                "Please enter password for user[root]:\r\n" +
                "Check root password connection successfully\r\n" +
                "root\r\n" +
                "Distribute the context file to remote hosts successfully\r\n" +
                "Start to health check for the cluster. Total Items:57 Nodes:3\r\n" +
                "\r\n" +
                "\r\n" +
                "Checking...               [                         ] 0/57\r\n" +
                "Checking...               [                         ] 1/57\r\n" +
                "Checking...               [                         ] 2/57\r\n" +
                "Checking...               [                         ] 4/57\r\n" +
                "Checking...               [                         ] 5/57\r\n" +
                "Checking...               [                         ] 6/57\r\n" +
                "Checking...               [                         ] 7/57\r\n" +
                "Checking...               [                         ] 14/57\r\n" +
                "Checking...               [                         ] 16/57\r\n" +
                "Checking...               [                         ] 17/57\r\n" +
                "Checking...               [                         ] 20/57\r\n" +
                "Checking...               [                         ] 21/57\r\n" +
                "Checking...               [                         ] 33/57\r\n" +
                "Checking...               [                         ] 34/57\r\n" +
                "Checking...               [                         ] 35/57\r\n" +
                "Checking...               [                         ] 37/57\r\n" +
                "Checking...               [                         ] 38/57\r\n" +
                "Checking...               [                         ] 44/57\r\n" +
                "Checking...               [                         ] 45/57\r\n" +
                "Checking...               [                         ] 46/57\r\n" +
                "Checking...               [                         ] 47/57\r\n" +
                "Checking...               [                         ] 52/57\r\n" +
                "Checking...               [                         ] 53/57\r\n" +
                "Checking...               [                         ] 54/57\r\n" +
                "Checking...               [                         ] 55/57\r\n" +
                "Checking...               [=========================] 57/57\r\n" +
                "Start to analysis the check result\r\n" +
                "CheckClusterState...........................OK\r\n" +
                "The item run on 3 nodes.  success: 3 \r\n" +
                "\r\n" +
                "CheckDBParams...............................OK\r\n" +
                "The item run on 1 nodes.  success: 1 \r\n" +
                "\r\n" +
                "CheckDebugSwitch............................OK\r\n" +
                "The item run on 3 nodes.  success: 3 \r\n" +
                "\r\n" +
                "CheckDirPermissions.........................NG\r\n" +
                "The item run on 3 nodes.  success: 2  ng: 1 \r\n" +
                "The ng[node1] value:\r\n" +
                "AppPath directory(/opt/huawei/install/app) permissions 755                     : Abnormal reason: Directory permission can not exceed 750.\r\n" +
                "Tmp directory(/opt/huawei/tmp) permissions 700                                 : Normal\r\n" +
                "Log directory(/opt/huawei/log/omm/omm) permissions 700                         : Normal\r\n" +
                "ToolPath directory(/opt/huawei/install/om) permissions 700                     : Normal\r\n" +
                "DN directory(/opt/huawei/install/data/dn) permissions 700                      : Normal\r\n" +
                "DN Xlog directory(/opt/huawei/install/data/dn/pg_xlog) permissions 700         : Normal\r\n" +
                "\r\n" +
                "\r\n" +
                "CheckReadonlyMode...........................OK\r\n" +
                "The item run on 1 nodes.  success: 1 \r\n" +
                "\r\n" +
                "CheckEnvProfile.............................OK\r\n" +
                "The item run on 3 nodes.  success: 3  (consistent) \r\n" +
                "The success on all nodes value:\r\n" +
                "GAUSSHOME        /opt/huawei/install/app\r\n" +
                "LD_LIBRARY_PATH  /opt/huawei/install/app/lib\r\n" +
                "PATH             /opt/huawei/install/app/bin\r\n" +
                "\r\n" +
                "\r\n" +
                "CheckBlockdev...............................OK\r\n" +
                "The item run on 3 nodes.  success: 3 \r\n" +
                "\r\n" +
                "CheckCurConnCount...........................OK\r\n" +
                "The item run on 1 nodes.  success: 1 \r\n" +
                "\r\n" +
                "CheckCursorNum..............................OK\r\n" +
                "The item run on 1 nodes.  success: 1 \r\n" +
                "\r\n" +
                "CheckPgxcgroup..............................OK\r\n" +
                "The item run on 1 nodes.  success: 1 \r\n" +
                "\r\n" +
                "CheckDiskFormat.............................OK\r\n" +
                "The item run on 3 nodes.  success: 3 \r\n" +
                "\r\n" +
                "CheckSpaceUsage.............................OK\r\n" +
                "The item run on 3 nodes.  success: 3 \r\n" +
                "\r\n" +
                "CheckInodeUsage.............................OK\r\n" +
                "The item run on 3 nodes.  success: 3 \r\n" +
                "\r\n" +
                "CheckSwapMemory.............................NG\r\n" +
                "The item run on 3 nodes.  ng: 3 \r\n" +
                "The ng[node1,node2,node3] value:\r\n" +
                "SwapMemory(2147479552) must be 0.\r\n" +
                "MemTotal: 1907970048.\r\n" +
                "\r\n" +
                "CheckLogicalBlock...........................OK\r\n" +
                "The item run on 3 nodes.  success: 3 \r\n" +
                "\r\n" +
                "CheckIOrequestqueue.........................OK\r\n" +
                "The item run on 3 nodes.  success: 3 \r\n" +
                "\r\n" +
                "CheckMaxAsyIOrequests.......................OK\r\n" +
                "The item run on 3 nodes.  success: 3 \r\n" +
                "\r\n" +
                "CheckIOConfigure............................OK\r\n" +
                "The item run on 3 nodes.  success: 3 \r\n" +
                "\r\n" +
                "CheckMTU....................................OK\r\n" +
                "The item run on 3 nodes.  success: 3  (consistent) \r\n" +
                "The success on all nodes value:\r\n" +
                "1500\r\n" +
                "\r\n" +
                "CheckPing...................................OK\r\n" +
                "The item run on 3 nodes.  success: 3 \r\n" +
                "\r\n" +
                "CheckRXTX...................................NG\r\n" +
                "The item run on 3 nodes.  ng: 3 \r\n" +
                "The ng[node1,node2,node3] value:\r\n" +
                "NetWork[ens33]\r\n" +
                "RX: 256\r\n" +
                "TX: 256\r\n" +
                "\r\n" +
                "\r\n" +
                "CheckNetWorkDrop............................OK\r\n" +
                "The item run on 3 nodes.  success: 3 \r\n" +
                "\r\n" +
                "CheckMultiQueue.............................OK\r\n" +
                "The item run on 3 nodes.  success: 3 \r\n" +
                "\r\n" +
                "CheckEncoding...............................OK\r\n" +
                "The item run on 3 nodes.  success: 3  (consistent) \r\n" +
                "The success on all nodes value:\r\n" +
                "LANG=en_US.UTF-8\r\n" +
                "\r\n" +
                "CheckFirewall...............................OK\r\n" +
                "The item run on 3 nodes.  success: 3 \r\n" +
                "\r\n" +
                "CheckKernelVer..............................OK\r\n" +
                "The item run on 3 nodes.  success: 3  (consistent) \r\n" +
                "The success on all nodes value:\r\n" +
                "3.10.0-957.el7.x86_64\r\n" +
                "\r\n" +
                "CheckMaxHandle..............................OK\r\n" +
                "The item run on 3 nodes.  success: 3 \r\n" +
                "\r\n" +
                "CheckNTPD...................................NG\r\n" +
                "node1: NTPD service is not running, 2022-11-10 06:42:02\r\n" +
                "node2: NTPD service is not running, 2022-11-10 06:41:34\r\n" +
                "node3: NTPD service is not running, 2022-11-10 06:41:35\r\n" +
                "\r\n" +
                "CheckOSVer..................................OK\r\n" +
                "node1: The current OS is centos 7.6 64bit.\r\n" +
                "node2: The current OS is centos 7.6 64bit.\r\n" +
                "node3: The current OS is centos 7.6 64bit.\r\n" +
                "\r\n" +
                "\r\n" +
                "CheckSysParams..............................NG\r\n" +
                "The item run on 3 nodes.  ng: 3 \r\n" +
                "The ng[node1,node2,node3] value:\r\n" +
                "Abnormal reason: variable 'net.ipv4.tcp_retries2' RealValue '12' ExpectedValue '80'.\r\n" +
                "Warning reason: variable 'net.ipv4.tcp_retries1' RealValue '3' ExpectedValue '5'.\r\n" +
                "Warning reason: variable 'net.ipv4.tcp_syn_retries' RealValue '6' ExpectedValue '5'.\r\n" +
                "\r\n" +
                "\r\n" +
                "CheckTHP....................................OK\r\n" +
                "The item run on 3 nodes.  success: 3 \r\n" +
                "\r\n" +
                "CheckTimeZone...............................OK\r\n" +
                "The item run on 3 nodes.  success: 3  (consistent) \r\n" +
                "The success on all nodes value:\r\n" +
                "+0800\r\n" +
                "\r\n" +
                "CheckCPU.................................ERROR\r\n" +
                "The item run on 3 nodes.  error: 3 \r\n" +
                "The error[node1,node2,node3] value:\r\n" +
                "[GAUSS-53025]: ERROR: Execute Shell command faild: export LC_ALL=C; sar 1 5 2>&1 , the exception is: /bin/sh: sar: command not found\r\n" +
                "\r\n" +
                "CheckSshdService............................OK\r\n" +
                "The item run on 3 nodes.  success: 3 \r\n" +
                "\r\n" +
                "CheckSshdConfig.........................WARNING\r\n" +
                "The item run on 3 nodes.  warning: 3 \r\n" +
                "The warning[node1,node2,node3] value:\r\n" +
                "\r\n" +
                "Warning reason: UseDNS parameter is not set; expected: no\r\n" +
                "\r\n" +
                "CheckCrondService...........................OK\r\n" +
                "The item run on 3 nodes.  success: 3 \r\n" +
                "\r\n" +
                "CheckStack..................................OK\r\n" +
                "The item run on 3 nodes.  success: 3  (consistent) \r\n" +
                "The success on all nodes value:\r\n" +
                "8192\r\n" +
                "\r\n" +
                "CheckSysPortRange...........................OK\r\n" +
                "The item run on 3 nodes.  success: 3 \r\n" +
                "\r\n" +
                "CheckMemInfo................................OK\r\n" +
                "The item run on 3 nodes.  success: 3  (consistent) \r\n" +
                "The success on all nodes value:\r\n" +
                "totalMem: 1.7769355773925781G\r\n" +
                "\r\n" +
                "CheckHyperThread............................NG\r\n" +
                "The item run on 3 nodes.  ng: 3 \r\n" +
                "The ng[node1,node2,node3] value:\r\n" +
                "Hyper-threading is down.\r\n" +
                "\r\n" +
                "CheckTableSpace.............................OK\r\n" +
                "The item run on 1 nodes.  success: 1 \r\n" +
                "\r\n" +
                "CheckSysadminUser...........................OK\r\n" +
                "The item run on 1 nodes.  success: 1 \r\n" +
                "\r\n" +
                "CheckGUCConsistent..........................OK\r\n" +
                "All DN instance guc value is consistent.\r\n" +
                "\r\n" +
                "CheckMaxProcMemory..........................NG\r\n" +
                "The item run on 1 nodes.  ng: 1 \r\n" +
                "The ng[node1] value:\r\n" +
                "parameter max_process_memory setting should not be bigger than recommended(kb):745300:\r\n" +
                "RecommendedMaxMem is 745300\r\n" +
                "DN_6001 : 2097152\r\n" +
                "\r\n" +
                "\r\n" +
                "CheckBootItems..............................OK\r\n" +
                "The item run on 3 nodes.  success: 3 \r\n" +
                "\r\n" +
                "CheckHashIndex..............................OK\r\n" +
                "The item run on 1 nodes.  success: 1 \r\n" +
                "\r\n" +
                "CheckPgxcRedistb............................OK\r\n" +
                "The item run on 1 nodes.  success: 1 \r\n" +
                "\r\n" +
                "CheckNodeGroupName..........................OK\r\n" +
                "The item run on 1 nodes.  success: 1 \r\n" +
                "\r\n" +
                "CheckTDDate.................................OK\r\n" +
                "The item run on 1 nodes.  success: 1 \r\n" +
                "\r\n" +
                "CheckDilateSysTab...........................OK\r\n" +
                "The item run on 1 nodes.  success: 1 \r\n" +
                "\r\n" +
                "CheckKeyProAdj..............................OK\r\n" +
                "The item run on 3 nodes.  success: 3 \r\n" +
                "\r\n" +
                "CheckProStartTime.......................WARNING\r\n" +
                "node1:\r\n" +
                "STARTED COMMAND\r\n" +
                "Thu Nov 10 06:24:45 2022 gaussdb fenced UDF master process\r\n" +
                "Thu Nov 10 06:33:43 2022 /opt/huawei/install/app/bin/gaussdb -D /opt/huawei/install/data/dn -M primary\r\n" +
                "node2:\r\n" +
                "STARTED COMMAND\r\n" +
                "Thu Nov 10 06:24:45 2022 gaussdb fenced UDF master process\r\n" +
                "Thu Nov 10 06:33:57 2022 /opt/huawei/install/app/bin/gaussdb -D /opt/huawei/install/data/dn -M standby\r\n" +
                "node3:\r\n" +
                "STARTED COMMAND\r\n" +
                "Thu Nov 10 06:24:44 2022 gaussdb fenced UDF master process\r\n" +
                "Thu Nov 10 06:34:15 2022 /opt/huawei/install/app/bin/gaussdb -D /opt/huawei/install/data/dn -M standby\r\n" +
                "\r\n" +
                "\r\n" +
                "CheckFilehandle.............................OK\r\n" +
                "The item run on 3 nodes.  success: 3 \r\n" +
                "\r\n" +
                "CheckRouting................................OK\r\n" +
                "The item run on 3 nodes.  success: 3 \r\n" +
                "\r\n" +
                "CheckNICModel...............................OK\r\n" +
                "The item run on 3 nodes.  success: 3  (consistent) \r\n" +
                "The success on all nodes value:\r\n" +
                "version: 7.3.21-k8-NAPI\r\n" +
                "model: Intel Corporation 82545EM Gigabit Ethernet Controller\r\n" +
                "\r\n" +
                "\r\n" +
                "CheckDropCache..........................WARNING\r\n" +
                "The item run on 3 nodes.  warning: 3 \r\n" +
                "The warning[node1,node2,node3] value:\r\n" +
                "No DropCache process is running\r\n" +
                "\r\n" +
                "CheckMpprcFile..............................NG\r\n" +
                "The item run on 3 nodes.  ng: 3 \r\n" +
                "The ng[node1,node2,node3] value:\r\n" +
                "There is no mpprc file\r\n" +
                "\r\n" +
                "Analysis the check result successfully\r\n" +
                "Failed.\tAll check items run completed. Total:57   Success:45   Warning:3   NG:8   Error:1 \r\n" +
                "For more information please refer to /opt/huawei/install/om/script/gspylib/inspection/output/CheckReport_inspect_202211102390755619.tar.gz\r\n");
        return opsCheckEntity;
    }


    private List<OpsClusterEntity> listMockOpsCluster() {
        List<OpsClusterEntity> mockClusterList = new ArrayList<>();

        OpsClusterEntity opsClusterEntity = new OpsClusterEntity();
        opsClusterEntity.setClusterId("1");
        opsClusterEntity.setVersion(OpenGaussVersionEnum.LITE);
        opsClusterEntity.setVersionNum("3.0.0");
        opsClusterEntity.setInstallMode(InstallModeEnum.OFF_LINE);
        opsClusterEntity.setDeployType(DeployTypeEnum.SINGLE_NODE);
        opsClusterEntity.setClusterName("test_cluster");
        opsClusterEntity.setDatabaseUsername("gaussdb");
        opsClusterEntity.setDatabasePassword("1qaz2wsx#EDC");
        mockClusterList.add(opsClusterEntity);
        return mockClusterList;
    }
}
