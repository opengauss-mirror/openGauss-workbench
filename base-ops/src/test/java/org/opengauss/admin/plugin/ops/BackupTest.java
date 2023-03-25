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
 * BackupTest.java
 *
 * IDENTIFICATION
 * base-ops/src/test/java/org/opengauss/admin/plugin/ops/BackupTest.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.ops;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.plugin.domain.entity.ops.OpsBackupEntity;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterNodeEntity;
import org.opengauss.admin.plugin.domain.model.ops.WsSession;
import org.opengauss.admin.plugin.domain.model.ops.cache.WsConnectorManager;
import org.opengauss.admin.plugin.enums.ops.ClusterRoleEnum;
import org.opengauss.admin.plugin.enums.ops.DeployTypeEnum;
import org.opengauss.admin.plugin.enums.ops.InstallModeEnum;
import org.opengauss.admin.plugin.enums.ops.OpenGaussVersionEnum;
import org.opengauss.admin.plugin.mapper.ops.OpsBackupMapper;
import org.opengauss.admin.plugin.service.ops.IOpsBackupService;
import org.opengauss.admin.plugin.service.ops.IOpsClusterNodeService;
import org.opengauss.admin.plugin.service.ops.IOpsClusterService;
import org.opengauss.admin.plugin.service.ops.impl.OpsBackupService;
import org.opengauss.admin.plugin.vo.ops.BackupInputDto;
import org.opengauss.admin.plugin.vo.ops.BackupVO;
import org.opengauss.admin.plugin.vo.ops.RecoverInputDto;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.plugin.facade.HostUserFacade;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

/**
 * @author lhf
 * @date 2022/11/28 11:18
 **/
@RunWith(SpringRunner.class)
public class BackupTest {

    @Mock
    private IOpsClusterService clusterService;
    @Mock
    private IOpsClusterNodeService clusterNodeService;
    @Mock
    private HostFacade hostFacade;
    @Mock
    private HostUserFacade hostUserFacade;
    @Mock
    private WsConnectorManager wsConnectorManager;
    @Mock
    private OpsBackupMapper backupMapper;
    @Mock
    private EncryptionUtils encryptionUtils;
    @InjectMocks
    private IOpsBackupService opsBackupService = new OpsBackupService(){
        @Override
        public OpsBackupEntity getById(Serializable id) {
            return mockBackupEntity();
        }

        @Override
        public boolean removeById(OpsBackupEntity entity) {
            return true;
        }
    };

    @BeforeClass
    public static void before(){
        MockitoAnnotations.initMocks(BackupTest.class);
        System.out.println("start Backup test........");
    }
    @AfterClass
    public static void after(){
        System.out.println("end Backup test........");
    }

    @Test
    public void testBackup(){
        ThreadPoolTaskExecutor commonExecutor = new ThreadPoolTaskExecutor();
        commonExecutor.setCorePoolSize(5);
        commonExecutor.setMaxPoolSize(10);
        commonExecutor.setQueueCapacity(1000);
        commonExecutor.setKeepAliveSeconds(300);
        commonExecutor.afterPropertiesSet();
        ReflectionTestUtils.setField(opsBackupService, "threadPoolTaskExecutor", commonExecutor);

        Mockito.doReturn(mockClusterEntity()).when(clusterService).getById(any());
        Mockito.doReturn(mockClusterNodeEntityList()).when(clusterNodeService).listClusterNodeByClusterId(any());
        Mockito.doReturn(mockHostEntity()).when(hostFacade).getById(any());
        Mockito.doReturn(mockHostRootUserEntity()).when(hostUserFacade).getRootUserByHostId(any());
        Mockito.doReturn(mockHostOmmUserEntity()).when(hostUserFacade).getById(any());
        Mockito.doReturn(Optional.of(mockWsSession())).when(wsConnectorManager).getSession(any());
        Mockito.doReturn("root").when(encryptionUtils).decrypt(any());

        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        BackupInputDto backupInputDto = new BackupInputDto();
        backupInputDto.setBusinessId("1");
        opsBackupService.backup(backupInputDto);
    }

    @Test
    public void testRecover(){
        ThreadPoolTaskExecutor commonExecutor = new ThreadPoolTaskExecutor();
        commonExecutor.setCorePoolSize(5);
        commonExecutor.setMaxPoolSize(10);
        commonExecutor.setQueueCapacity(1000);
        commonExecutor.setKeepAliveSeconds(300);
        commonExecutor.afterPropertiesSet();
        ReflectionTestUtils.setField(opsBackupService, "threadPoolTaskExecutor", commonExecutor);

        Mockito.doReturn(mockClusterEntity()).when(clusterService).getById(any());
        Mockito.doReturn(mockHostEntity()).when(hostFacade).getById(any());
        Mockito.doReturn(mockClusterNodeEntityList().get(0)).when(clusterNodeService).getById(any());
        Mockito.doReturn(mockHostRootUserEntity()).when(hostUserFacade).getRootUserByHostId(any());
        Mockito.doReturn(mockHostOmmUserEntity()).when(hostUserFacade).getById(any());
        Mockito.doReturn(Optional.of(mockWsSession())).when(wsConnectorManager).getSession(any());

        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        RecoverInputDto recoverInputDto = new RecoverInputDto();
        recoverInputDto.setBusinessId("1");
        opsBackupService.recover("1",recoverInputDto);
    }

    @Test
    public void testDel(){
        ThreadPoolTaskExecutor commonExecutor = new ThreadPoolTaskExecutor();
        commonExecutor.setCorePoolSize(5);
        commonExecutor.setMaxPoolSize(10);
        commonExecutor.setQueueCapacity(1000);
        commonExecutor.setKeepAliveSeconds(300);
        commonExecutor.afterPropertiesSet();
        ReflectionTestUtils.setField(opsBackupService, "threadPoolTaskExecutor", commonExecutor);

        Mockito.doReturn(mockClusterEntity()).when(clusterService).getById(any());
        Mockito.doReturn(mockHostEntity()).when(hostFacade).getById(any());
        Mockito.doReturn(mockClusterNodeEntityList().get(0)).when(clusterNodeService).getById(any());
        Mockito.doReturn(mockHostRootUserEntity()).when(hostUserFacade).getRootUserByHostId(any());
        opsBackupService.del("1");
    }

    @Test
    public void testPageBackup(){
        Page page = new Page();
        page.setCurrent(1);
        page.setSize(10);
        page.setTotal(1);

        List<BackupVO> records = new ArrayList();
        records.add(BeanUtil.copyProperties(mockBackupEntity(),BackupVO.class));
        page.setRecords(records);
        Mockito.doReturn(page).when(backupMapper).pageBackup(any(),any());
        Page<BackupVO> backupVOPage = opsBackupService.pageBackup(page, "1");
        Assertions.assertNotNull(backupVOPage);
        Assertions.assertEquals(backupVOPage,page);
    }

    private OpsBackupEntity mockBackupEntity(){
        OpsBackupEntity opsBackupEntity = new OpsBackupEntity();
        opsBackupEntity.setBackupId("1");
        opsBackupEntity.setBackupPath("/etc/backup/1.sql");
        opsBackupEntity.setClusterId("1");
        opsBackupEntity.setHostId("1");
        opsBackupEntity.setClusterNodeId("1");
        return opsBackupEntity;
    }

    private OpsHostUserEntity mockHostOmmUserEntity() {
        OpsHostUserEntity hostUserEntity = new OpsHostUserEntity();
        hostUserEntity.setHostUserId("2");
        hostUserEntity.setUsername("omm");
        hostUserEntity.setPassword("123456");
        hostUserEntity.setHostId("1");
        return hostUserEntity;
    }

    private WsSession mockWsSession() {
        WsSession wsSession = new WsSession();
        return wsSession;
    }

    private OpsHostUserEntity mockHostRootUserEntity() {
        OpsHostUserEntity hostUserEntity = new OpsHostUserEntity();
        hostUserEntity.setHostUserId("1");
        hostUserEntity.setUsername("root");
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
        return opsClusterEntity;
    }

}
