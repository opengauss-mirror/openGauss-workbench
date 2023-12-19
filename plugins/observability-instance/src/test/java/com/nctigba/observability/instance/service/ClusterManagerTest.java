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
 *  ClusterManagerTest.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/test/java/com/nctigba/observability/instance/service/ClusterManagerTest.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.sql.DriverManager;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.plugin.facade.OpsFacade;
import org.opengauss.admin.system.service.ops.IOpsClusterService;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.creator.DefaultDataSourceCreator;

/**
 * ClusterManagerTest.java
 *
 * @since 2023年7月17日
 */
@ExtendWith(MockitoExtension.class)
class ClusterManagerTest {
    @InjectMocks
    private ClusterManager clusterManager;
    @Mock
    private DynamicRoutingDataSource dataSource;
    @Mock
    private DefaultDataSourceCreator dataSourceCreator;
    @Mock
    private OpsFacade opsFacade;
    @Mock
    private HostFacade hostFacade;
    @Mock
    private IOpsClusterService opsClusterService;

    @BeforeEach
    void setup() {
        OpsClusterNodeVO node = new OpsClusterNodeVO();
        node.setNodeId("id");
        OpsClusterVO cluster = new OpsClusterVO();
        cluster.setClusterId("id");
        cluster.setClusterNodes(List.of(node));
        when(opsFacade.listCluster()).thenReturn(List.of(cluster));

        when(hostFacade.getById(anyString())).thenReturn(new OpsHostEntity());
        OpsClusterEntity opsCluster = new OpsClusterEntity();
        opsCluster.setDatabaseUsername("");
        opsCluster.setDatabasePassword("");
        when(opsClusterService.getById(any())).thenReturn(opsCluster);
    }

    @Test
    void test() {
        clusterManager.setCurrentDatasource("id", null);
        clusterManager.pool();
        clusterManager.setCurrentDatasource(null, null);
        try (MockedStatic<DriverManager> mockStatic = mockStatic(DriverManager.class)) {
            mockStatic.when(() -> DriverManager.getConnection(anyString(), any())).thenReturn(null);
            clusterManager.getConnectionByClusterHost("id", "id");
        }
    }
}