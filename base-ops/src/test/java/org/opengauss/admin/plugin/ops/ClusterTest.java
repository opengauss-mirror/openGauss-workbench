package org.opengauss.admin.plugin.ops;

import com.jcraft.jsch.*;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.plugin.domain.model.ops.*;
import org.opengauss.admin.plugin.domain.model.ops.*;
import org.opengauss.admin.plugin.domain.model.ops.cache.WsConnectorManager;
import org.opengauss.admin.plugin.domain.model.ops.node.MinimalistInstallNodeConfig;
import org.opengauss.admin.plugin.enums.ops.ClusterRoleEnum;
import org.opengauss.admin.plugin.enums.ops.DeployTypeEnum;
import org.opengauss.admin.plugin.enums.ops.InstallModeEnum;
import org.opengauss.admin.plugin.enums.ops.OpenGaussVersionEnum;
import org.opengauss.admin.plugin.service.ops.ClusterOpsProvider;
import org.opengauss.admin.plugin.service.ops.IOpsClusterService;
import org.opengauss.admin.plugin.service.ops.impl.ClusterOpsProviderManager;
import org.opengauss.admin.plugin.service.ops.impl.OpsClusterServiceImpl;
import org.opengauss.admin.plugin.service.ops.impl.provider.MinimaListOpsProvider;
import org.opengauss.admin.plugin.utils.JschUtil;
import org.opengauss.admin.plugin.utils.WsUtil;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.plugin.facade.HostUserFacade;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

/**
 * @author lhf
 * @date 2022/11/30 18:09
 **/
@RunWith(SpringRunner.class)
public class ClusterTest {
    @Mock
    private WsConnectorManager wsConnectorManager;
    @Mock
    private HostFacade hostFacade;
    @Mock
    private HostUserFacade hostUserFacade;
    @Mock
    private ClusterOpsProviderManager clusterOpsProviderManager;
    @Mock
    private WsUtil wsUtil;
    @Mock
    private JschUtil jschUtil;
    @Mock
    private EncryptionUtils encryptionUtils;
    @InjectMocks
    private IOpsClusterService opsClusterService = new OpsClusterServiceImpl() {
        @Override
        public OpsClusterEntity getById(Serializable id) {
            return null;
        }
    };

    @BeforeClass
    public static void before() {
        MockitoAnnotations.initMocks(WdrTest.class);
        System.out.println("start Cluster test........");
    }

    @AfterClass
    public static void after() {
        System.out.println("end Cluster test........");
    }


    @Test
    public void testDownload() {
        ThreadPoolTaskExecutor commonExecutor = new ThreadPoolTaskExecutor();
        commonExecutor.setCorePoolSize(5);
        commonExecutor.setMaxPoolSize(10);
        commonExecutor.setQueueCapacity(1000);
        commonExecutor.setKeepAliveSeconds(300);
        commonExecutor.afterPropertiesSet();
        ReflectionTestUtils.setField(opsClusterService, "threadPoolTaskExecutor", commonExecutor);

        Mockito.doReturn(Optional.of(mockWsSession())).when(wsConnectorManager).getSession(any());
        DownloadBody downloadBody = new DownloadBody();
        downloadBody.setBusinessId("1");
        opsClusterService.download(downloadBody);
    }

    @Test
    public void testInstall() throws Exception {
        ThreadPoolTaskExecutor commonExecutor = new ThreadPoolTaskExecutor();
        commonExecutor.setCorePoolSize(5);
        commonExecutor.setMaxPoolSize(10);
        commonExecutor.setQueueCapacity(1000);
        commonExecutor.setKeepAliveSeconds(300);
        commonExecutor.afterPropertiesSet();
        ReflectionTestUtils.setField(opsClusterService, "threadPoolTaskExecutor", commonExecutor);

        Mockito.doReturn("root").when(encryptionUtils).decrypt(any());
        Mockito.doReturn(mockJschResult()).when(jschUtil).executeCommand(any(), any());
        Mockito.doReturn(Optional.ofNullable(mockJschSession())).when(jschUtil).getSession(any(), any(), any(), any());
        Mockito.doReturn(Optional.of(mockWsSession())).when(wsConnectorManager).getSession(any());
        Mockito.doReturn(Arrays.asList(mockHostEntity())).when(hostFacade).listByIds(any());
        Mockito.doReturn(Arrays.asList(mockHostRootUserEntity(), mockHostOmmUserEntity())).when(hostUserFacade).listHostUserByHostIdList(any());

        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        ClusterOpsProvider clusterOpsProvider = new MinimaListOpsProvider();
        Mockito.doReturn(Optional.ofNullable(clusterOpsProvider)).when(clusterOpsProviderManager).provider(any(), any());
        Mockito.doNothing().when(wsUtil).sendText(any(), any());

        InstallBody installBody = mockSimpleInstallBody();
        opsClusterService.install(installBody);
    }

    private OpsHostUserEntity mockHostRootUserEntity() {
        OpsHostUserEntity hostUserEntity = new OpsHostUserEntity();
        hostUserEntity.setHostUserId("1");
        hostUserEntity.setUsername("root");
        hostUserEntity.setPassword("123456");
        hostUserEntity.setHostId("1");
        return hostUserEntity;
    }

    private Session mockJschSession() throws JSchException {
        return new JSch().getSession("192.168.0.31");
    }

    private JschResult mockJschResult() {
        JschResult jschResult = new JschResult();
        jschResult.setExitCode(0);
        jschResult.setResult("success");
        return jschResult;
    }

    private OpsHostUserEntity mockHostOmmUserEntity() {
        OpsHostUserEntity hostUserEntity = new OpsHostUserEntity();
        hostUserEntity.setHostUserId("2");
        hostUserEntity.setUsername("omm");
        hostUserEntity.setPassword("123456");
        hostUserEntity.setHostId("1");
        return hostUserEntity;
    }

    private OpsHostEntity mockHostEntity() {
        OpsHostEntity hostEntity = new OpsHostEntity();
        hostEntity.setHostId("1");
        hostEntity.setHostname("node1");
        hostEntity.setPublicIp("192.168.0.31");
        hostEntity.setPrivateIp("192.168.0.31");
        hostEntity.setPort(22);
        return hostEntity;
    }

    private InstallBody mockSimpleInstallBody() {
        InstallBody installBody = new InstallBody();
        installBody.setBusinessId("1");
        InstallContext installContext = new InstallContext();
        installContext.setOpenGaussVersion(OpenGaussVersionEnum.MINIMAL_LIST);
        installContext.setOpenGaussVersionNum("3.0.0");
        installContext.setInstallMode(InstallModeEnum.OFF_LINE);
        installContext.setDeployType(DeployTypeEnum.SINGLE_NODE);
        installContext.setInstallPackagePath("/etc/openGauss-3.0.0-CentOS-64bit.tar.bz2");
        MinimalistInstallConfig minimalistInstallConfig = new MinimalistInstallConfig();
        minimalistInstallConfig.setPort(5432);
        minimalistInstallConfig.setDatabaseUsername("gaussdb");
        minimalistInstallConfig.setDatabasePassword("1qaz2wsx#EDC");

        List<MinimalistInstallNodeConfig> minimalistInstallNodeConfigs = new ArrayList<>();
        MinimalistInstallNodeConfig minimalistInstallNodeConfig = new MinimalistInstallNodeConfig();
        minimalistInstallNodeConfig.setClusterRole(ClusterRoleEnum.MASTER);
        minimalistInstallNodeConfig.setHostId("1");
        minimalistInstallNodeConfig.setRootPassword("123456");
        minimalistInstallNodeConfig.setInstallUserId("1");
        minimalistInstallNodeConfig.setInstallPath("/opt/install");
        minimalistInstallNodeConfig.setIsInstallDemoDatabase(Boolean.TRUE);
        minimalistInstallNodeConfigs.add(minimalistInstallNodeConfig);

        minimalistInstallConfig.setNodeConfigList(minimalistInstallNodeConfigs);
        installContext.setMinimalistInstallConfig(minimalistInstallConfig);
        installContext.setClusterId("1");
        installBody.setInstallContext(installContext);
        return installBody;
    }

    private WsSession mockWsSession() {
        WsSession wsSession = new WsSession();
        return wsSession;
    }

    private OpsClusterEntity mockClusterEntity() {
        OpsClusterEntity opsClusterEntity = new OpsClusterEntity();
        opsClusterEntity.setClusterId("1");
        opsClusterEntity.setVersion(OpenGaussVersionEnum.LITE);
        opsClusterEntity.setVersionNum("3.0.0");
        opsClusterEntity.setInstallMode(InstallModeEnum.OFF_LINE);
        opsClusterEntity.setDeployType(DeployTypeEnum.SINGLE_NODE);
        opsClusterEntity.setDatabaseUsername("gaussdb");
        opsClusterEntity.setDatabasePassword("1qaz2wsx#EDC");
        return opsClusterEntity;
    }

    @Test
    public void testDirPermission() {
        String host = "192.168.1.8";
        String username = "wang";
        String password = "1qaz2wsx#EDC";
        String directoryPath = "/root/data";
        Session session = null;
        ChannelSftp channelSftp = null;
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(username, host, 22);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(password);
            session.connect();
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            SftpATTRS attrs;
            try {
                attrs = channelSftp.stat(directoryPath);
            } catch (SftpException e) {
                if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                    // directory does not exist, check if user has create permission
                    try {
                        channelSftp.mkdir(directoryPath);
                        attrs = channelSftp.stat(directoryPath);
                        channelSftp.rmdir(directoryPath);
                    } catch (SftpException ex) {
                        System.out.println(">>>>>>>>>>>>>>>>>>>>> User does not have create permission for directory " + directoryPath);
                        throw e;
                    }
                } else {
                    System.out.println(">>>>>>>>>>>>>>>>>>>>> User does not have create permission for directory " + directoryPath);
                    throw e;
                }
            }

            // check if user has read and write permission for directory
            if (attrs.getPermissionsString().charAt(1) == 'r' && attrs.getPermissionsString().charAt(2) == 'w') {
                System.out.println("User has read and write permission for directory " + directoryPath);
            } else {
                System.out.println("User does not have read and write permission for directory " + directoryPath);
            }

        } catch (JSchException | SftpException e) {
            e.printStackTrace();
        } finally {
            if (channelSftp != null && channelSftp.isConnected()) {
                channelSftp.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }
}
