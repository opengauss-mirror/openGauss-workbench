/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

import cn.hutool.core.collection.ListUtil;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterNodeEntity;
import org.opengauss.admin.plugin.domain.model.ops.DownloadBody;
import org.opengauss.admin.plugin.domain.model.ops.EnterpriseInstallConfig;
import org.opengauss.admin.plugin.domain.model.ops.HostFile;
import org.opengauss.admin.plugin.domain.model.ops.InstallBody;
import org.opengauss.admin.plugin.domain.model.ops.InstallContext;
import org.opengauss.admin.plugin.domain.model.ops.JschResult;
import org.opengauss.admin.plugin.domain.model.ops.LiteInstallConfig;
import org.opengauss.admin.plugin.domain.model.ops.MinimalistInstallConfig;
import org.opengauss.admin.plugin.domain.model.ops.OpsClusterBody;
import org.opengauss.admin.plugin.domain.model.ops.UnInstallBody;
import org.opengauss.admin.plugin.domain.model.ops.UpgradeBody;
import org.opengauss.admin.plugin.domain.model.ops.WsSession;
import org.opengauss.admin.plugin.domain.model.ops.cache.WsConnectorManager;
import org.opengauss.admin.plugin.domain.model.ops.node.EnterpriseInstallNodeConfig;
import org.opengauss.admin.plugin.domain.model.ops.node.LiteInstallNodeConfig;
import org.opengauss.admin.plugin.domain.model.ops.node.MinimalistInstallNodeConfig;
import org.opengauss.admin.plugin.enums.ops.ClusterRoleEnum;
import org.opengauss.admin.plugin.enums.ops.DatabaseKernelArch;
import org.opengauss.admin.plugin.enums.ops.DeployTypeEnum;
import org.opengauss.admin.plugin.enums.ops.InstallModeEnum;
import org.opengauss.admin.plugin.enums.ops.OpenGaussVersionEnum;
import org.opengauss.admin.plugin.enums.ops.UpgradeTypeEnum;
import org.opengauss.admin.plugin.service.ops.IOpsClusterNodeService;
import org.opengauss.admin.plugin.service.ops.IOpsClusterService;
import org.opengauss.admin.plugin.service.ops.impl.OpsClusterServiceImpl;
import org.opengauss.admin.plugin.utils.DownloadUtil;
import org.opengauss.admin.plugin.utils.JschUtil;
import org.opengauss.admin.plugin.vo.ops.AuditLogVO;
import org.opengauss.admin.plugin.vo.ops.OpsNodeLogVO;
import org.opengauss.admin.plugin.vo.ops.SessionVO;
import org.opengauss.admin.plugin.vo.ops.SlowSqlVO;
import org.opengauss.admin.plugin.vo.ops.UpgradeOsCheckVO;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.plugin.facade.HostUserFacade;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

/**
 * OpsClusterTest
 *
 * @author: wangchao
 * @Date: 2025/9/11 21:21
 * @since 7.0.0-RC2
 **/
@RunWith(MockitoJUnitRunner.class)
@ExtendWith(MockitoExtension.class)
public class OpsClusterTest {
    private static final String HOST_ID = "testHostId";
    private static final String NEW_INSTALL_CLUSTER_ID = "newInstallClusterId";
    private static final String OLD_CLUSTER_ID = "testClusterId";
    private static final String BUSINESS_ID = "testBusinessId";
    private static final String ROOT_PASSWORD = "testRootPassword";

    @InjectMocks
    @Spy
    private IOpsClusterService opsClusterService = new OpsClusterServiceImpl();
    @Mock
    private IOpsClusterNodeService opsClusterNodeService;
    @Mock
    private WsConnectorManager wsConnectorManager;
    @Mock
    private HostFacade hostFacade;
    @Mock
    private HostUserFacade hostUserFacade;
    @Mock
    private ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
    @Mock
    private JschUtil jschUtil;
    @Mock
    private HttpServletRequest request;
    @Mock
    private DownloadUtil downloadUtil;

    @Before
    public void setup() throws IOException, InterruptedException, JSchException {
        doReturn(getClusterEntity()).when(opsClusterService).getById(OLD_CLUSTER_ID);
        doReturn(null).when(opsClusterService).getById(NEW_INSTALL_CLUSTER_ID);
        doReturn(getClusterEntity()).when(opsClusterService).getById("testName");
        when(wsConnectorManager.getSession(any())).thenReturn(Optional.of(new WsSession()));
        when(threadPoolTaskExecutor.submit(any(Runnable.class))).thenReturn(new CompletableFuture<>());
        when(hostFacade.getById(any())).thenReturn(getHost());
        when(hostFacade.listByIds(any())).thenReturn(ListUtil.toList(getHost()));
        when(hostUserFacade.getById(any())).thenReturn(getHostUser());
        when(hostUserFacade.getRootUserByHostId(any())).thenReturn(getRootUser());
        when(hostUserFacade.listHostUserByHostId(any())).thenReturn(getHostUserList());
        when(hostUserFacade.listHostUserByHostIdList(any())).thenReturn(getHostUserList());
        when(opsClusterNodeService.listClusterNodeByClusterId(any())).thenReturn(getClusterNodeEntity());
        doNothing().when(downloadUtil).download(any(), any(), any(), any());
        Session session = null;
        JSch jSch = new JSch();
        session = jSch.getSession("");
        doReturn(Optional.of(session)).when(jschUtil).getSession(any(), any(), any(), any());
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    public void testHasName() throws IOException, InterruptedException {
        boolean hasNameResult = opsClusterService.hasName("testName");
        Assert.assertTrue(hasNameResult);
    }

    @Test
    public void testDownload() throws IOException, InterruptedException {
        DownloadBody body = new DownloadBody();
        body.setBusinessId(BUSINESS_ID);
        opsClusterService.download(body);
        HttpServletResponse response = null;
        opsClusterService.download("testHostId", "/mnt/d/", "testFileName", response);
    }

    @Test
    public void testInstallEnterpriseCluster() throws IOException, InterruptedException {
        doReturn(getCmdResult()).when(jschUtil).executeCommand(any(), any());
        InstallBody body = getEnterpriseInstallBody();
        opsClusterService.install(body);
    }

    @Test
    public void testInstallLiteCluster() throws IOException, InterruptedException {
        doReturn(getCmdResult()).when(jschUtil).executeCommand(any(), any());
        InstallBody body = getLiteInstallBody();
        opsClusterService.install(body);
    }

    @Test
    public void testInstallMiniCluster() throws IOException, InterruptedException {
        doReturn(getCmdResult()).when(jschUtil).executeCommand(any(), any());
        InstallBody body = getSimpleInstallBody();
        opsClusterService.install(body);
    }

    @Test
    public void testUpgradeOsCheck() throws IOException, InterruptedException {
        doReturn(getCmdDfResult()).when(jschUtil).executeCommand(any(), any());
        UpgradeOsCheckVO result = opsClusterService.upgradeOsCheck(OLD_CLUSTER_ID, ROOT_PASSWORD);
        Assert.assertEquals(result.getDiskUsed(), Integer.valueOf(9));
        Assert.assertNotNull(result.getOsInfo());
    }

    @Test
    public void testUpgrade() throws IOException, InterruptedException {
        doReturn(getCmdDfResult()).when(jschUtil).executeCommand(any(), any());
        UpgradeBody upgradeBody = getUpgradeBody();
        opsClusterService.upgrade(upgradeBody);
    }

    @Test
    public void testUpgradeCommit() {
        UpgradeBody upgradeBody = getUpgradeBody();
        opsClusterService.upgradeCommit(upgradeBody);
    }

    @Test
    public void testUpgradeRollback() {
        UpgradeBody upgradeBody = getUpgradeBody();
        opsClusterService.upgradeRollback(upgradeBody);
    }

    @Test
    public void testQuickInstall() throws IOException, InterruptedException {
        doReturn(getCmdResult()).when(jschUtil).executeCommand(any(), any());
        InstallBody installBody = getQuickInstallBody();
        opsClusterService.quickInstall(installBody);
    }

    @Test
    public void testUninstall() throws IOException, InterruptedException {
        doReturn(getCmdResult()).when(jschUtil).executeCommand(any(), any());
        UnInstallBody unInstallBody = new UnInstallBody();
        unInstallBody.setClusterId(OLD_CLUSTER_ID);
        unInstallBody.setForce(false);
        unInstallBody.setBusinessId(BUSINESS_ID);
        opsClusterService.uninstall(unInstallBody);
    }

    @Test
    public void testRestart() throws IOException, InterruptedException {
        doReturn(getCmdResult()).when(jschUtil).executeCommand(any(), any());
        OpsClusterBody opsClusterBody = new OpsClusterBody();
        opsClusterBody.setClusterId(OLD_CLUSTER_ID);
        opsClusterBody.setBusinessId(BUSINESS_ID);
        opsClusterService.restart(opsClusterBody);
    }

    @Test
    public void testStart() throws IOException, InterruptedException {
        doReturn(getCmdResult()).when(jschUtil).executeCommand(any(), any());
        OpsClusterBody opsClusterBody = new OpsClusterBody();
        opsClusterBody.setClusterId(OLD_CLUSTER_ID);
        opsClusterBody.setBusinessId(BUSINESS_ID);
        opsClusterService.start(opsClusterBody);
    }

    @Test
    public void testStop() throws IOException, InterruptedException {
        doReturn(getCmdResult()).when(jschUtil).executeCommand(any(), any());
        OpsClusterBody opsClusterBody = new OpsClusterBody();
        opsClusterBody.setClusterId(OLD_CLUSTER_ID);
        opsClusterBody.setBusinessId(BUSINESS_ID);
        opsClusterService.stop(opsClusterBody);
    }

    @Test
    public void testLs() throws IOException, InterruptedException {
        List<HostFile> hostFiles = opsClusterService.ls(HOST_ID, "/ops");
        Assert.assertNotNull(hostFiles);
    }

    @Test
    public void testLogPath() throws IOException, InterruptedException {
        doReturn(getCmdResult()).when(jschUtil).executeCommand(any(), any());
        OpsNodeLogVO nodeLogVO = opsClusterService.logPath(OLD_CLUSTER_ID, HOST_ID);
        Assert.assertEquals(nodeLogVO.getOpLogPath(), "testCmdResult");
    }

    @Test
    public void testAuditLog() {
        List<AuditLogVO> auditLogVOList = opsClusterService.auditLog(getPage(), OLD_CLUSTER_ID, HOST_ID,
            "2023-12-24+00:00:00", "2023-12-24+23:59:59");
        Assert.assertNotNull(auditLogVOList);
    }

    @Test
    public void testListSession() {
        List<SessionVO> sessionVOList = opsClusterService.listSession(OLD_CLUSTER_ID, HOST_ID);
        Assert.assertNotNull(sessionVOList);
    }

    @Test
    public void testSlowSql() {
        List<SlowSqlVO> slowSqlVOS = opsClusterService.slowSql(getPage(), OLD_CLUSTER_ID, HOST_ID,
            "2023-12-24+00:00:00", "2023-12-24+23:59:59");
        Assert.assertNotNull(slowSqlVOS);
    }

    private OpsClusterEntity getClusterEntity() {
        OpsClusterEntity clusterEntity = new OpsClusterEntity();
        clusterEntity.setClusterId(OLD_CLUSTER_ID);
        clusterEntity.setInstallPath("/ops/opengauss");
        clusterEntity.setVersion(OpenGaussVersionEnum.ENTERPRISE);
        return clusterEntity;
    }

    private List<org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO> getClusterList() {
        List<org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO> clusterList = new ArrayList<>();
        org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO clusterVO
            = new org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO();
        clusterVO.setClusterId(OLD_CLUSTER_ID);
        clusterVO.setVersionNum("5.1.1");
        clusterVO.setVersion(OpenGaussVersionEnum.ENTERPRISE.name());
        clusterList.add(clusterVO);
        return clusterList;
    }

    private Page getPage() {
        Page page = new Page<>();
        page.setCurrent(1);
        page.setSize(10);
        return page;
    }

    private List<OpsClusterNodeEntity> getClusterNodeEntity() {
        List<OpsClusterNodeEntity> nodeEntityList = new ArrayList<>();
        OpsClusterNodeEntity clusterNodeEntity = new OpsClusterNodeEntity();
        clusterNodeEntity.setClusterId(OLD_CLUSTER_ID);
        clusterNodeEntity.setHostId(HOST_ID);
        clusterNodeEntity.setClusterRole(ClusterRoleEnum.MASTER);
        nodeEntityList.add(clusterNodeEntity);
        return nodeEntityList;
    }

    private OpsHostEntity getHost() {
        OpsHostEntity hostEntity = new OpsHostEntity();
        hostEntity.setHostId(HOST_ID);
        return hostEntity;
    }

    private OpsHostUserEntity getHostUser() {
        OpsHostUserEntity userEntity1 = new OpsHostUserEntity();
        userEntity1.setUsername("lhf");
        userEntity1.setHostId(HOST_ID);
        userEntity1.setPassword("testPassword");
        return userEntity1;
    }

    private OpsHostUserEntity getRootUser() {
        OpsHostUserEntity userEntity1 = new OpsHostUserEntity();
        userEntity1.setUsername("root");
        userEntity1.setHostId(HOST_ID);
        userEntity1.setPassword(ROOT_PASSWORD);
        return userEntity1;
    }

    private List<OpsHostUserEntity> getHostUserList() {
        List<OpsHostUserEntity> userList = new ArrayList<>();
        OpsHostUserEntity userEntity1 = new OpsHostUserEntity();
        userEntity1.setUsername("lhf");
        userEntity1.setHostId(HOST_ID);
        userEntity1.setPassword("testPassword");
        userList.add(userEntity1);
        OpsHostUserEntity userEntity2 = new OpsHostUserEntity();
        userEntity2.setUsername("root");
        userEntity2.setHostId(HOST_ID);
        userEntity2.setPassword(ROOT_PASSWORD);
        userList.add(userEntity2);
        return userList;
    }

    private JschResult getCmdResult() {
        JschResult result = new JschResult();
        result.setExitCode(0);
        result.setResult("testCmdResult");
        return result;
    }

    private JschResult getCmdDfResult() {
        JschResult result = new JschResult();
        result.setExitCode(0);
        result.setResult(
            "Filesystem      Size  Used Avail Use% Mounted on\n" + "devtmpfs        3.8G     0  3.8G   0% /dev\n"
                + "tmpfs           3.8G   48K  3.8G   1% /dev/shm\n" + "tmpfs           3.8G  756K  3.8G   1% /run\n"
                + "tmpfs           3.8G     0  3.8G   0% /sys/fs/cgroup\n" + "/dev/vda1        99G  8.0G   87G   9% /\n"
                + "tmpfs           773M     0  773M   0% /run/user/0\n"
                + "tmpfs           773M     0  773M   0% /run/user/1002");
        return result;
    }

    private InstallBody getEnterpriseInstallBody() {
        InstallContext installContext = new InstallContext();
        installContext.setOpenGaussVersion(OpenGaussVersionEnum.ENTERPRISE);
        EnterpriseInstallConfig enterpriseInstallConfig = new EnterpriseInstallConfig();
        enterpriseInstallConfig.setInstallPath("/opt/openGauss/install/app");
        enterpriseInstallConfig.setIsInstallCM(false);
        enterpriseInstallConfig.setCorePath("/opt/openGauss/corefile");
        enterpriseInstallConfig.setPort(5432);
        enterpriseInstallConfig.setLogPath("/opt/openGauss/log/omm");
        enterpriseInstallConfig.setEnableDCF(false);
        enterpriseInstallConfig.setOmToolsPath("/opt/openGauss/install/om");
        enterpriseInstallConfig.setTmpPath("/opt/openGauss/tmp");
        enterpriseInstallConfig.setDatabaseKernelArch(DatabaseKernelArch.MASTER_SLAVE);
        enterpriseInstallConfig.setDatabaseUsername("");
        enterpriseInstallConfig.setDatabasePassword("1qaz2wsx#EDC");

        EnterpriseInstallNodeConfig enterpriseInstallNodeConfig = new EnterpriseInstallNodeConfig();
        enterpriseInstallNodeConfig.setClusterRole(ClusterRoleEnum.MASTER);
        enterpriseInstallNodeConfig.setHostId("1729406555136229377");
        enterpriseInstallNodeConfig.setIsCMMaster(false);
        enterpriseInstallNodeConfig.setPublicIp("124.222.71.6");
        enterpriseInstallNodeConfig.setPrivateIp("10.0.4.12");
        enterpriseInstallNodeConfig.setHostname("hostname");
        enterpriseInstallNodeConfig.setInstallUserId("1729407004216164353");
        enterpriseInstallNodeConfig.setInstallUsername("wangn");
        enterpriseInstallNodeConfig.setCmDataPath("/opt/openGauss/data/cmserver");
        enterpriseInstallNodeConfig.setCmPort(15300);
        enterpriseInstallNodeConfig.setDataPath("/opt/openGauss/install/data/dn");
        enterpriseInstallNodeConfig.setAzPriority("1");

        List<EnterpriseInstallNodeConfig> nodeConfigList = new ArrayList<>();
        nodeConfigList.add(enterpriseInstallNodeConfig);
        enterpriseInstallConfig.setNodeConfigList(nodeConfigList);
        installContext.setEnterpriseInstallConfig(enterpriseInstallConfig);
        installContext.setClusterId(NEW_INSTALL_CLUSTER_ID);
        installContext.setOpenGaussVersionNum("5.1.0");
        installContext.setInstallMode(InstallModeEnum.OFF_LINE);
        installContext.setDeployType(DeployTypeEnum.SINGLE_NODE);
        installContext.setInstallPackagePath("/ops");
        InstallBody installBody = new InstallBody();
        installBody.setInstallContext(installContext);
        installBody.setBusinessId(BUSINESS_ID);
        return installBody;
    }

    private InstallBody getLiteInstallBody() {
        InstallContext installContext = new InstallContext();
        installContext.setOpenGaussVersion(OpenGaussVersionEnum.LITE);
        LiteInstallConfig liteInstallConfig = new LiteInstallConfig();
        liteInstallConfig.setPort(5432);
        liteInstallConfig.setDatabaseUsername("");
        liteInstallConfig.setDatabasePassword("1qaz2wsx#EDC");
        liteInstallConfig.setInstallPackagePath("/opt/software/openGauss");
        List<LiteInstallNodeConfig> nodeConfigList = new ArrayList<>();
        LiteInstallNodeConfig liteInstallNodeConfig = new LiteInstallNodeConfig();
        liteInstallNodeConfig.setInstallPath("/opt/openGauss/install/app");
        liteInstallNodeConfig.setInstallUserId("testUserId");
        liteInstallNodeConfig.setClusterRole(ClusterRoleEnum.MASTER);
        liteInstallNodeConfig.setHostId(HOST_ID);
        liteInstallNodeConfig.setDataPath("/opt/openGauss/data");
        liteInstallNodeConfig.setRootPassword(ROOT_PASSWORD);
        nodeConfigList.add(liteInstallNodeConfig);
        liteInstallConfig.setNodeConfigList(nodeConfigList);
        installContext.setLiteInstallConfig(liteInstallConfig);
        installContext.setClusterId(NEW_INSTALL_CLUSTER_ID);
        installContext.setOpenGaussVersionNum("5.0.0");
        installContext.setInstallMode(InstallModeEnum.OFF_LINE);
        installContext.setDeployType(DeployTypeEnum.SINGLE_NODE);
        installContext.setInstallPackagePath("/ops");
        InstallBody installBody = new InstallBody();
        installBody.setInstallContext(installContext);
        installBody.setBusinessId(BUSINESS_ID);
        return installBody;
    }

    private InstallBody getSimpleInstallBody() {
        InstallContext installContext = new InstallContext();
        installContext.setOpenGaussVersion(OpenGaussVersionEnum.MINIMAL_LIST);
        MinimalistInstallConfig miniInstallConfig = new MinimalistInstallConfig();
        miniInstallConfig.setPort(5432);
        miniInstallConfig.setDatabaseUsername("");
        miniInstallConfig.setDatabasePassword("1qaz2wsx#EDC");
        miniInstallConfig.setInstallPackagePath("/opt/software/openGauss");
        List<MinimalistInstallNodeConfig> nodeConfigList = new ArrayList<>();
        MinimalistInstallNodeConfig miniInstallNodeConfig = new MinimalistInstallNodeConfig();
        miniInstallNodeConfig.setInstallPath("/opt/openGauss/install/app");
        miniInstallNodeConfig.setInstallUserId("testUserId");
        miniInstallNodeConfig.setClusterRole(ClusterRoleEnum.MASTER);
        miniInstallNodeConfig.setHostId(HOST_ID);
        miniInstallNodeConfig.setRootPassword(ROOT_PASSWORD);
        miniInstallNodeConfig.setIsInstallDemoDatabase(true);
        nodeConfigList.add(miniInstallNodeConfig);
        miniInstallConfig.setNodeConfigList(nodeConfigList);
        installContext.setMinimalistInstallConfig(miniInstallConfig);
        installContext.setClusterId(NEW_INSTALL_CLUSTER_ID);
        installContext.setOpenGaussVersionNum("5.0.0");
        installContext.setInstallMode(InstallModeEnum.OFF_LINE);
        installContext.setDeployType(DeployTypeEnum.SINGLE_NODE);
        installContext.setInstallPackagePath("/ops");
        InstallBody installBody = new InstallBody();
        installBody.setInstallContext(installContext);
        installBody.setBusinessId(BUSINESS_ID);
        return installBody;
    }

    private InstallBody getQuickInstallBody() {
        InstallBody installBody = getSimpleInstallBody();
        installBody.setQuickInstall(true);
        installBody.setQuickInstallResourceUrl(
            "https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.1.0/x86/openGauss-5.1.0-CentOS-64bit.tar.bz2");
        installBody.setBusinessId(BUSINESS_ID);
        return installBody;
    }

    private UpgradeBody getUpgradeBody() {
        UpgradeBody upgradeBody = new UpgradeBody();
        upgradeBody.setClusterId(OLD_CLUSTER_ID);
        upgradeBody.setUpgradeType(UpgradeTypeEnum.GRAY_UPGRADE);
        upgradeBody.setBusinessId(BUSINESS_ID);
        upgradeBody.setUpgradePackagePath("D:\\upload\\openGauss-5.1.0-CentOS-64bit-all.tar.gz");
        upgradeBody.setVersionNum("5.1.0");
        upgradeBody.setHostRootPassword(ROOT_PASSWORD);
        return upgradeBody;
    }
}
