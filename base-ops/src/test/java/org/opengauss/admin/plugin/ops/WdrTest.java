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
 * WdrTest.java
 *
 * IDENTIFICATION
 * base-ops/src/test/java/org/opengauss/admin/plugin/ops/WdrTest.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.ops;

import com.alibaba.druid.mock.MockConnection;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterNodeEntity;
import org.opengauss.admin.plugin.domain.entity.ops.OpsWdrEntity;
import org.opengauss.admin.plugin.domain.model.ops.JschResult;
import org.opengauss.admin.plugin.domain.model.ops.WdrGeneratorBody;
import org.opengauss.admin.plugin.enums.ops.*;
import org.opengauss.admin.plugin.service.ops.ClusterOpsProvider;
import org.opengauss.admin.plugin.service.ops.IOpsClusterNodeService;
import org.opengauss.admin.plugin.service.ops.IOpsClusterService;
import org.opengauss.admin.plugin.service.ops.IOpsWdrService;
import org.opengauss.admin.plugin.service.ops.impl.ClusterOpsProviderManager;
import org.opengauss.admin.plugin.service.ops.impl.OpsWdrServiceImpl;
import org.opengauss.admin.plugin.utils.DBUtil;
import org.opengauss.admin.plugin.utils.JschUtil;
import org.opengauss.admin.plugin.vo.ops.DwrSnapshotVO;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.plugin.facade.HostUserFacade;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;

/**
 * @author lhf
 * @date 2022/11/30 17:13
 **/
@RunWith(SpringRunner.class)
public class WdrTest {

    @Mock
    private IOpsClusterService clusterService;
    @Mock
    private IOpsClusterNodeService clusterNodeService;
    @Mock
    private HostFacade hostFacade;
    @Mock
    private HostUserFacade hostUserFacade;
    @Mock
    private JschUtil jschUtil;
    @Mock
    private EncryptionUtils encryptionUtils;
    @Mock
    private ClusterOpsProviderManager clusterOpsProviderManager;
    @Mock
    private ClusterOpsProvider clusterOpsProvider;
    @Mock
    private DBUtil DBUtil;
    @InjectMocks
    private IOpsWdrService wdrService = new OpsWdrServiceImpl(){
        @Override
        public List<OpsWdrEntity> list(Wrapper<OpsWdrEntity> queryWrapper) {
            return Arrays.asList(mockWdrEntity());
        }

        @Override
        public boolean save(OpsWdrEntity entity) {
            return true;
        }

        @Override
        public OpsWdrEntity getById(Serializable id) {
            return mockWdrEntity();
        }

        @Override
        public boolean removeById(Serializable id) {
            return true;
        }
    };

    @BeforeClass
    public static void before(){
        MockitoAnnotations.initMocks(WdrTest.class);
    }

    @Test
    public void testListWdr(){
        List<OpsWdrEntity> opsWdrEntities = wdrService.listWdr("1", WdrScopeEnum.CLUSTER, WdrTypeEnum.ALL, "1", null, null);
        Assertions.assertNotNull(opsWdrEntities);
        Assertions.assertEquals(1,opsWdrEntities.size());
    }

    @Test
    public void testGenerate() throws Exception {
        Mockito.doReturn(mockClusterEntity()).when(clusterService).getById(any());
        Mockito.doReturn(mockClusterNodeEntityList()).when(clusterNodeService).listClusterNodeByClusterId(any());
        Mockito.doReturn(mockHostEntity()).when(hostFacade).getById(any());
        Mockito.doReturn(mockHostOmmUserEntity()).when(hostUserFacade).getById(any());
        Mockito.doReturn("root").when(encryptionUtils).decrypt(any());
        Mockito.doReturn(Optional.ofNullable(mockJschSession())).when(jschUtil).getSession(any(),any(),any(),any());
        Mockito.doReturn(Optional.ofNullable(clusterOpsProvider)).when(clusterOpsProviderManager).provider(any(),any());
        Mockito.doNothing().when(clusterOpsProvider).enableWdrSnapshot(any(),any(),any(),any(),any());
        Mockito.doReturn(mockJschResult()).when(jschUtil).executeCommandWithSerialResponse(any(),any(),any(), any());

        WdrGeneratorBody wdrGeneratorBody = new WdrGeneratorBody();
        wdrGeneratorBody.setClusterId("1");
        wdrGeneratorBody.setHostId("1");
        wdrGeneratorBody.setScope(WdrScopeEnum.CLUSTER);
        wdrGeneratorBody.setType(WdrTypeEnum.ALL);
        wdrGeneratorBody.setStartId("1");
        wdrGeneratorBody.setEndId("3");
        wdrService.generate(wdrGeneratorBody);
    }

    @Test
    public void testListSnapshot() throws SQLException, ClassNotFoundException {
        Mockito.doReturn(mockClusterEntity()).when(clusterService).getById(any());
        Mockito.doReturn(mockHostEntity()).when(hostFacade).getById(any());
        Mockito.doReturn(Optional.of(mockConnection())).when(DBUtil).getSession(any(),any(),any(),any());

        Page page = new Page();
        Page<DwrSnapshotVO> dwrSnapshotVOS = wdrService.listSnapshot(page, "1", "1");
        Assertions.assertNotNull(dwrSnapshotVOS);
    }

    @Test
    public void testCreateSnapshot() throws Exception {
        Mockito.doReturn(mockClusterEntity()).when(clusterService).getById(any());
        Mockito.doReturn(mockHostEntity()).when(hostFacade).getById(any());
        Mockito.doReturn(mockClusterNodeEntityList()).when(clusterNodeService).listClusterNodeByClusterId(any());
        Mockito.doReturn(mockHostOmmUserEntity()).when(hostUserFacade).getById(any());
        Mockito.doReturn(Optional.ofNullable(mockJschSession())).when(jschUtil).getSession(any(),any(),any(),any());
        Mockito.doReturn(Optional.ofNullable(clusterOpsProvider)).when(clusterOpsProviderManager).provider(any(),any());
        Mockito.doNothing().when(clusterOpsProvider).enableWdrSnapshot(any(),any(),any(),any(),any());
        Mockito.doReturn(mockJschResult()).when(jschUtil).executeCommandWithSerialResponse(any(),any(),any(), any());
        wdrService.createSnapshot("1","1");
    }

    @Test
    public void testDel() throws JSchException {
        Mockito.doReturn(Arrays.asList(mockHostRootUserEntity(),mockHostOmmUserEntity())).when(hostUserFacade).listHostUserByHostId(any());
        Mockito.doReturn(mockHostEntity()).when(hostFacade).getById(any());
        Mockito.doReturn(mockHostRootUserEntity()).when(hostUserFacade).getRootUserByHostId(any());
        Mockito.doReturn(Optional.ofNullable(mockJschSession())).when(jschUtil).getSession(any(),any(),any(),any());
        wdrService.del("1");
    }

    private Connection mockConnection() throws SQLException, ClassNotFoundException {
        Connection connection = new MockConnection();
        return connection;
    }

    private JschResult mockJschResult() {
        JschResult jschResult = new JschResult();
        jschResult.setExitCode(0);
        return jschResult;
    }

    private Session mockJschSession() throws JSchException {
        return new JSch().getSession("192.168.0.31");
    }

    private OpsHostUserEntity mockHostRootUserEntity() {
        OpsHostUserEntity hostUserEntity = new OpsHostUserEntity();
        hostUserEntity.setHostUserId("1");
        hostUserEntity.setUsername("root");
        hostUserEntity.setPassword("123456");
        hostUserEntity.setHostId("1");
        return hostUserEntity;
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


    private OpsWdrEntity mockWdrEntity(){
        OpsWdrEntity opsWdrEntity = new OpsWdrEntity();
        opsWdrEntity.setWdrId("1");
        opsWdrEntity.setScope(WdrScopeEnum.CLUSTER);
        opsWdrEntity.setReportAt(new Date());
        opsWdrEntity.setReportType(WdrTypeEnum.ALL);
        opsWdrEntity.setReportName("test wdr");
        opsWdrEntity.setReportPath("/opt/wdr");
        opsWdrEntity.setNodeId("1");
        opsWdrEntity.setHostId("1");
        opsWdrEntity.setStartSnapshotId("1");
        opsWdrEntity.setEndSnapshotId("3");
        return opsWdrEntity;
    }

    private List<OpsClusterNodeEntity> mockClusterNodeEntityList() {
        List<OpsClusterNodeEntity> res = new ArrayList<>();
        OpsClusterNodeEntity opsClusterNodeEntity = new OpsClusterNodeEntity();
        opsClusterNodeEntity.setClusterNodeId("1");
        opsClusterNodeEntity.setClusterId("1");
        opsClusterNodeEntity.setClusterRole(ClusterRoleEnum.MASTER);
        opsClusterNodeEntity.setHostId("1");
        opsClusterNodeEntity.setRootPassword("123456");
        opsClusterNodeEntity.setInstallUserId("1");
        opsClusterNodeEntity.setInstallPath("/etc/openGauss");
        opsClusterNodeEntity.setDataPath("/etc/openGauss/datapath");
        opsClusterNodeEntity.setPkgPath("/etc/openGauss/pkgpath");
        opsClusterNodeEntity.setInstallDemoDatabase(Boolean.TRUE);
        res.add(opsClusterNodeEntity);
        return res;
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
        opsClusterEntity.setPort(5432);
        return opsClusterEntity;
    }
}
