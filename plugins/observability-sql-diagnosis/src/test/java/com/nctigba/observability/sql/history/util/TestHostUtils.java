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
 *  TestHostUtils.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/test/java/com/nctigba/observability/sql/history/util/TestHostUtils.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.history.util;

import com.nctigba.observability.sql.service.impl.ClusterManager;
import com.nctigba.observability.sql.util.HostUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

/**
 * TestHostUtil
 *
 * @author luomeng
 * @since 2023/7/13
 */
@RunWith(MockitoJUnitRunner.class)
public class TestHostUtils {
    @Mock
    private ClusterManager clusterManager;

    @InjectMocks
    private HostUtils util;

    @Test
    public void testGetHostId() {
        String nodeId = "37e8a893-0b7e-49b2-a0b4-e6fdf7dc4345";
        List<OpsClusterVO> clusterVOList = new ArrayList<>();
        OpsClusterVO clusterVO = new OpsClusterVO();
        List<OpsClusterNodeVO> clusterNodes = new ArrayList<>();
        OpsClusterNodeVO nodeVO = new OpsClusterNodeVO();
        nodeVO.setNodeId(nodeId);
        clusterNodes.add(nodeVO);
        clusterVO.setClusterNodes(clusterNodes);
        clusterVOList.add(clusterVO);
        when(clusterManager.getAllOpsCluster()).thenReturn(clusterVOList);
        String result = util.getHostId(nodeId);
        assertNull(result);
    }
}
