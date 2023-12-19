/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  IndexControllerTest.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/test/java/com/nctigba/observability/instance/controller/IndexControllerTest.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.plugin.facade.HostUserFacade;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;

import com.alibaba.fastjson.JSONObject;
import com.nctigba.observability.instance.mapper.DbConfigMapper;
import com.nctigba.observability.instance.service.ClusterManager;
import com.nctigba.observability.instance.service.ClusterManager.OpsClusterNodeVOSub;
import com.nctigba.observability.instance.service.MetricsService;
import com.nctigba.observability.instance.service.SessionService;
import com.nctigba.observability.instance.service.TopSQLService;
import com.nctigba.observability.instance.util.SshSessionUtils;

/**
 * IndexControllerTest.java
 *
 * @since 2023年7月17日
 */
@ExtendWith(MockitoExtension.class)
class IndexControllerTest {
    @InjectMocks
    private IndexController indexController;
    @Mock
    private MetricsService metricsService;
    @Mock
    private TopSQLService topSQLService;
    @Mock
    private SessionService sessionService;
    @Mock
    private SshSessionUtils sshSession;
    @Mock
    private DbConfigMapper configMapper;
    @Mock
    private ClusterManager clusterManager;
    @Mock
    private HostFacade hostFacade;
    @Mock
    private HostUserFacade hostUserFacade;
    @Mock
    private EncryptionUtils encryptionUtils;

    @BeforeEach
    void setup() {
        when(sessionService.simpleStatistic(anyString())).thenReturn(new JSONObject());
        OpsClusterNodeVOSub opsClusterNodeVOSub = new OpsClusterNodeVOSub();
        opsClusterNodeVOSub.setHostId("id");
        opsClusterNodeVOSub.setInstallUserName("name");
        when(clusterManager.getOpsNodeById(anyString())).thenReturn(opsClusterNodeVOSub);
        when(configMapper.env()).thenReturn(Map.of("datapath", "", "log_directory", ""));
        var host = new OpsHostEntity();
        host.setHostId("");
        host.setPublicIp("");
        host.setPort(11);
        when(hostFacade.getById(anyString())).thenReturn(host);
        OpsHostUserEntity e1 = new OpsHostUserEntity();
        e1.setUsername("name");
        e1.setPassword("");
        when(hostUserFacade.listHostUserByHostId(anyString())).thenReturn(List.of(e1));
        when(encryptionUtils.decrypt(anyString())).thenReturn("pwd");
    }

    @Test
    void test() {
        indexController.mainMetrics("id", null, null, null);
        indexController.topSQLNow("id");
        try (MockedStatic<SshSessionUtils> mockStatic = mockStatic(SshSessionUtils.class);) {
            mockStatic.when(() -> SshSessionUtils.connect(anyString(), anyInt(), anyString(), anyString()))
                    .thenReturn(sshSession);
            indexController.nodeInfo("id");
        }
    }
}