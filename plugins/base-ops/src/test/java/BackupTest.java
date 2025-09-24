/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

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
import org.opengauss.admin.plugin.domain.entity.ops.OpsBackupEntity;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterNodeEntity;
import org.opengauss.admin.plugin.domain.model.ops.JschResult;
import org.opengauss.admin.plugin.domain.model.ops.WsSession;
import org.opengauss.admin.plugin.domain.model.ops.cache.WsConnectorManager;
import org.opengauss.admin.plugin.enums.ops.ClusterRoleEnum;
import org.opengauss.admin.plugin.service.ops.IOpsBackupService;
import org.opengauss.admin.plugin.service.ops.IOpsClusterNodeService;
import org.opengauss.admin.plugin.service.ops.IOpsClusterService;
import org.opengauss.admin.plugin.utils.JschUtil;
import org.opengauss.admin.plugin.vo.ops.BackupInputDto;
import org.opengauss.admin.plugin.vo.ops.RecoverInputDto;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.plugin.facade.HostUserFacade;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

/**
 * BackupTest
 *
 * @author: wangchao
 * @Date: 2025/9/11 21:21
 * @since 7.0.0-RC2
 **/
@RunWith(MockitoJUnitRunner.class)
@ExtendWith(MockitoExtension.class)
public class BackupTest {
    private static final String MASTER_NODE_HOST_ID = "testHostId";
    private static final String MASTER_NODE_INSTALL_USER_ID = "testUserId";

    @Mock
    private IOpsClusterService opsClusterService;
    @Mock
    private IOpsClusterNodeService clusterNodeService;
    @Mock
    private HostFacade hostFacade;
    @Mock
    private HostUserFacade hostUserFacade;
    @Mock
    private WsConnectorManager wsConnectorManager;
    @Mock
    private HttpServletRequest request;
    @Mock
    private JschUtil jschUtil;
    @InjectMocks
    @Spy
    private IOpsBackupService backupService;
    @Mock
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Before
    public void setup() throws IOException, InterruptedException, JSchException {
        List<OpsClusterEntity> clusterEntities = getClusterList();
        when(opsClusterService.getById("1")).thenReturn(getCluster());
        when(clusterNodeService.listClusterNodeByClusterId("1")).thenReturn(getClusterNodeList());
        when(clusterNodeService.getById("1")).thenReturn(getClusterNode());
        when(opsClusterService.list()).thenReturn(clusterEntities);
        when(hostFacade.getById(MASTER_NODE_HOST_ID)).thenReturn(getHost());
        when(hostUserFacade.getById(MASTER_NODE_INSTALL_USER_ID)).thenReturn(getHostUser());
        when(hostUserFacade.getRootUserByHostId(MASTER_NODE_HOST_ID)).thenReturn(getHostUser());
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        when(wsConnectorManager.getSession(any())).thenReturn(Optional.of(new WsSession()));
        when(threadPoolTaskExecutor.submit(any(Runnable.class))).thenReturn(new CompletableFuture<>());
        doReturn(getBackupEntity()).when(backupService).getById(any());
        doReturn(true).when(backupService).removeById(any());
        Session session = null;
        JSch jSch = new JSch();
        session = jSch.getSession("");
        doReturn(Optional.of(session)).when(jschUtil).getSession(any(), any(), any(), any());
        doReturn(getCmdResult()).when(jschUtil).executeCommand(any(), any());
    }

    @Test
    public void testBackup() {
        BackupInputDto inputDto = new BackupInputDto();
        inputDto.setBusinessId(null);
        List<OpsClusterEntity> voList = opsClusterService.list();
        inputDto.setClusterId(voList.get(0).getClusterId());
        inputDto.setBackupPath("/home/");
        inputDto.setBusinessId("testBusinessId");
        inputDto.setRemark("testRemark");
        backupService.backup(inputDto);
    }

    @Test
    public void testRecover() {
        RecoverInputDto recoverInputDto = new RecoverInputDto();
        recoverInputDto.setBusinessId("testBusinessId");
        backupService.recover("1", recoverInputDto);
    }

    @Test
    public void testDel() {
        backupService.del("1");
    }

    private List<OpsClusterEntity> getClusterList() {
        List<OpsClusterEntity> voList = new ArrayList<>();
        voList.add(getCluster());
        return voList;
    }

    private OpsClusterEntity getCluster() {
        OpsClusterEntity opsCluster = new OpsClusterEntity();
        opsCluster.setClusterId("1");
        return opsCluster;
    }

    private List<OpsClusterNodeEntity> getClusterNodeList() {
        List<OpsClusterNodeEntity> nodeList = new ArrayList<>();
        OpsClusterNodeEntity masterNode = new OpsClusterNodeEntity();
        masterNode.setClusterRole(ClusterRoleEnum.MASTER);
        masterNode.setHostId(MASTER_NODE_HOST_ID);
        masterNode.setInstallUserId(MASTER_NODE_INSTALL_USER_ID);
        nodeList.add(masterNode);
        OpsClusterNodeEntity slaveNode = new OpsClusterNodeEntity();
        slaveNode.setClusterRole(ClusterRoleEnum.SLAVE);
        nodeList.add(slaveNode);
        return nodeList;
    }

    private OpsClusterNodeEntity getClusterNode() {
        OpsClusterNodeEntity masterNode = new OpsClusterNodeEntity();
        masterNode.setClusterRole(ClusterRoleEnum.MASTER);
        masterNode.setHostId(MASTER_NODE_HOST_ID);
        masterNode.setInstallUserId(MASTER_NODE_INSTALL_USER_ID);
        return masterNode;
    }

    private OpsHostEntity getHost() {
        return new OpsHostEntity();
    }

    private OpsHostUserEntity getHostUser() {
        return new OpsHostUserEntity();
    }

    private OpsBackupEntity getBackupEntity() {
        OpsBackupEntity backupEntity = new OpsBackupEntity();
        backupEntity.setBackupId("1");
        backupEntity.setClusterId("1");
        backupEntity.setHostId(MASTER_NODE_HOST_ID);
        backupEntity.setClusterNodeId("1");
        return backupEntity;
    }

    private JschResult getCmdResult() {
        JschResult result = new JschResult();
        result.setExitCode(0);
        return result;
    }
}
