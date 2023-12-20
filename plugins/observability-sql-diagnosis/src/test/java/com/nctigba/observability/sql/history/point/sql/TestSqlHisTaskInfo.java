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
 *  TestSqlHisTaskInfo.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/test/java/com/nctigba/observability/sql/history/point/sql/TestSqlHisTaskInfo.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.history.point.sql;

import com.nctigba.observability.sql.exception.HisDiagnosisException;
import com.nctigba.observability.sql.mapper.DiagnosisTaskMapper;
import com.nctigba.observability.sql.model.entity.DiagnosisResultDO;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.dto.point.AnalysisDTO;
import com.nctigba.observability.sql.service.impl.ClusterManager;
import com.nctigba.observability.sql.service.DataStoreService;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.service.impl.point.sql.SqlTaskInfo;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TestSqlTaskInfo
 *
 * @author luomeng
 * @since 2023/9/17
 */
@RunWith(MockitoJUnitRunner.class)
public class TestSqlHisTaskInfo {
    @Mock
    private DataStoreService dataStoreService;
    @Mock
    private DiagnosisTaskMapper taskMapper;
    @Mock
    private ClusterManager clusterManager;
    @InjectMocks
    private SqlTaskInfo pointService;

    @Test
    public void testGetOption() {
        List<String> list = pointService.getOption();
        assertNull(list);
    }

    @Test
    public void testGetSourceDataKeys() {
        List<CollectionItem<?>> result = pointService.getSourceDataKeys();
        assertNull(result);
    }

    @Test
    public void testAnalysisData() {
        AnalysisDTO result = pointService.analysis(mock(DiagnosisTaskDO.class), dataStoreService);
        Assertions.assertEquals(DiagnosisResultDO.ResultState.SUGGESTIONS, result.getIsHint());
        Assertions.assertEquals(DiagnosisResultDO.PointType.ROOT, result.getPointType());
        assertNotNull(result);
    }

    @Test
    public void testGetShowData() {
        DiagnosisTaskDO task = new DiagnosisTaskDO();
        task.setNodeId("1");
        when(taskMapper.selectById(1)).thenReturn(task);
        OpsClusterNodeVO nodeVO = new OpsClusterNodeVO();
        nodeVO.setNodeId("1");
        nodeVO.setDataPath("test");
        nodeVO.setAzAddress("test");
        nodeVO.setAzName("test");
        nodeVO.setClusterRole("test");
        nodeVO.setDbName("test");
        nodeVO.setDbPort(8080);
        nodeVO.setDbUser("test");
        nodeVO.setDbUserPassword("test");
        nodeVO.setHostId("test");
        nodeVO.setHostname("test");
        nodeVO.setPublicIp("test");
        nodeVO.setPrivateIp("test");
        nodeVO.setIsRemember(true);
        nodeVO.setInstallUserName("test");
        nodeVO.setHostPort(8080);
        List<OpsClusterNodeVO> nodeVOList = new ArrayList<>();
        nodeVOList.add(nodeVO);
        OpsClusterVO opsClusterVO = new OpsClusterVO();
        opsClusterVO.setClusterNodes(nodeVOList);
        List<OpsClusterVO> list = new ArrayList<>();
        list.add(opsClusterVO);
        when(clusterManager.getAllOpsCluster()).thenReturn(list);
        DiagnosisTaskDO data = pointService.getShowData(1);
        assertEquals(opsClusterVO, data.getNodeVOSub());
        when(taskMapper.selectById(1)).thenReturn(null);
        try {
            pointService.getShowData(1);
        } catch (HisDiagnosisException e) {
            assertEquals("taskId is not exists!", e.getMessage());
        }
    }
}
