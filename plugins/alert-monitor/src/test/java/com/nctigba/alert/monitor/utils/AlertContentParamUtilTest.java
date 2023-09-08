/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.utils;

import com.nctigba.alert.monitor.constant.CommonConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterNodeEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.enums.ops.ClusterRoleEnum;
import org.opengauss.admin.common.exception.ServiceException;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.service.ops.IOpsClusterNodeService;
import org.opengauss.admin.system.service.ops.IOpsClusterService;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * AlertContentParamUtilTest
 *
 * @since 2023/8/28 10:57
 */
@RunWith(SpringRunner.class)
public class AlertContentParamUtilTest {
    @Mock
    private IOpsClusterService clusterService;
    @Mock
    private IOpsClusterNodeService clusterNodeService;
    @Mock
    private HostFacade hostFacade;
    @InjectMocks
    private AlertContentParamUtil contentParamUtil;

    @Test(expected = ServiceException.class)
    public void testSetAndGetAlertContentParamDtoThrowEx1() {
        OpsClusterNodeEntity opsClusterNodeEntity = null;
        when(clusterNodeService.getById(anyString())).thenReturn(opsClusterNodeEntity);
        contentParamUtil.setAndGetAlertContentParamDto("node1", LocalDateTime.now(), CommonConstants.WARN,
            "centent");
    }

    @Test(expected = ServiceException.class)
    public void testSetAndGetAlertContentParamDtoThrowEx2() {
        OpsClusterNodeEntity opsClusterNodeEntity = new OpsClusterNodeEntity();
        opsClusterNodeEntity.setHostId("1");
        opsClusterNodeEntity.setClusterNodeId("node1");
        when(clusterNodeService.getById(anyString())).thenReturn(opsClusterNodeEntity);
        OpsHostEntity opsHost = null;
        when(hostFacade.getById(opsClusterNodeEntity.getHostId())).thenReturn(opsHost);
        contentParamUtil.setAndGetAlertContentParamDto("node1", LocalDateTime.now(), CommonConstants.WARN,
            "centent");
    }

    @Test(expected = ServiceException.class)
    public void testSetAndGetAlertContentParamDtoThrowEx3() {
        OpsClusterNodeEntity opsClusterNodeEntity = new OpsClusterNodeEntity();
        opsClusterNodeEntity.setHostId("1");
        opsClusterNodeEntity.setClusterNodeId("node1");
        opsClusterNodeEntity.setClusterId("test");
        when(clusterNodeService.getById(anyString())).thenReturn(opsClusterNodeEntity);
        OpsHostEntity opsHost = new OpsHostEntity();
        when(hostFacade.getById(opsClusterNodeEntity.getHostId())).thenReturn(opsHost);
        OpsClusterEntity opsClusterEntity = null;
        when(clusterService.getById(opsClusterNodeEntity.getClusterId())).thenReturn(opsClusterEntity);
        contentParamUtil.setAndGetAlertContentParamDto("node1", LocalDateTime.now(), CommonConstants.WARN,
            "centent");
    }

    @Test
    public void testSetAndGetAlertContentParamDto() {
        OpsClusterNodeEntity opsClusterNodeEntity = new OpsClusterNodeEntity();
        opsClusterNodeEntity.setHostId("1");
        opsClusterNodeEntity.setClusterNodeId("node1");
        opsClusterNodeEntity.setClusterId("test");
        opsClusterNodeEntity.setClusterRole(ClusterRoleEnum.MASTER);
        when(clusterNodeService.getById(anyString())).thenReturn(opsClusterNodeEntity);
        OpsHostEntity opsHost = new OpsHostEntity();
        opsHost.setPublicIp("127.0.0.1");
        when(hostFacade.getById(opsClusterNodeEntity.getHostId())).thenReturn(opsHost);
        OpsClusterEntity opsClusterEntity = new OpsClusterEntity();
        opsClusterEntity.setClusterId("test");
        opsClusterEntity.setPort(8080);
        when(clusterService.getById(opsClusterNodeEntity.getClusterId())).thenReturn(opsClusterEntity);
        contentParamUtil.setAndGetAlertContentParamDto("node1", LocalDateTime.now(), CommonConstants.WARN,
            "centent");
    }

    @Test
    public void testSetAndGetAlertContentParamDtoWithEmptyContent() {
        OpsClusterNodeEntity opsClusterNodeEntity = new OpsClusterNodeEntity();
        opsClusterNodeEntity.setHostId("1");
        opsClusterNodeEntity.setClusterNodeId("node1");
        opsClusterNodeEntity.setClusterId("test");
        opsClusterNodeEntity.setClusterRole(ClusterRoleEnum.MASTER);
        when(clusterNodeService.getById(anyString())).thenReturn(opsClusterNodeEntity);
        OpsHostEntity opsHost = new OpsHostEntity();
        opsHost.setPublicIp("127.0.0.1");
        when(hostFacade.getById(opsClusterNodeEntity.getHostId())).thenReturn(opsHost);
        OpsClusterEntity opsClusterEntity = new OpsClusterEntity();
        opsClusterEntity.setClusterId("test");
        opsClusterEntity.setPort(8080);
        when(clusterService.getById(opsClusterNodeEntity.getClusterId())).thenReturn(opsClusterEntity);
        contentParamUtil.setAndGetAlertContentParamDto("node1", LocalDateTime.now(), CommonConstants.WARN,
            "");
    }
}
