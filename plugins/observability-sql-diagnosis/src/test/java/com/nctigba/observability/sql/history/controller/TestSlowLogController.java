/*
 *  Copyright (c) Huawei Technologies Co. 2025-2025.
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
 *  TestSlowLogController.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/test/java/com/nctigba/observability/sql/history/controller/
 *  TestSlowLogController.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.history.controller;

import com.nctigba.observability.sql.controller.SlowLogController;
import com.nctigba.observability.sql.model.query.SlowLogQuery;
import com.nctigba.observability.sql.model.vo.StatementHistoryAggVO;
import com.nctigba.observability.sql.model.vo.StatementHistoryDetailVO;
import com.nctigba.observability.sql.service.MyPage;
import com.nctigba.observability.sql.service.impl.HisSlowsqlServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.exception.CustomException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TestSlowLogController
 *
 * @author jianghongbo
 * @since 2025-09-15
 */
@ExtendWith(MockitoExtension.class)
class TestSlowLogController {
    @Mock
    private HisSlowsqlServiceImpl hisSlowsqlService;
    @InjectMocks
    private SlowLogController slowLogController;
    private SlowLogQuery slowLogQuery;
    @Mock
    private MyPage<StatementHistoryDetailVO> mockResultStmtDetail;
    @Mock
    private MyPage<StatementHistoryAggVO> mockResultStmtAgg;

    @BeforeEach
    void setUp() {
        Mockito.reset(hisSlowsqlService);
        slowLogQuery = new SlowLogQuery();
        slowLogQuery.setNodeId("test-node-id");
    }

    @Test
    void testListSlowSQLs() {
        // Arrange
        doNothing().when(hisSlowsqlService).collectNodeSlowsqls(anyString());
        when(hisSlowsqlService.listSlowSQLs(any(SlowLogQuery.class))).thenReturn(mockResultStmtDetail);

        // Act
        AjaxResult result = slowLogController.listSlowSQLs(slowLogQuery);

        // Assert
        assertNotNull(result);
        assertEquals("success", result.get("msg"));
        assertEquals(mockResultStmtDetail, result.get("data"));

        verify(hisSlowsqlService).collectNodeSlowsqls("test-node-id");
        verify(hisSlowsqlService).listSlowSQLs(slowLogQuery);
    }

    @Test
    void testSlowSqlChart() {
        // Arrange
        String nodeId = "test-node-id";
        Long start = 1000L;
        Long end = 2000L;
        Integer step = 60;
        String dbName = "test-db";
        Map<String, Object> sqlChartResult = new HashMap<>();
        doNothing().when(hisSlowsqlService).collectNodeSlowsqls(anyString());
        when(hisSlowsqlService.getSlowSqlChart(anyString(), anyLong(), anyLong(), anyInt(), anyString()))
                .thenReturn(sqlChartResult);
        // Act
        AjaxResult result = slowLogController.slowSqlChart(nodeId, start, end, step, dbName);
        // Assert
        assertNotNull(result);
        assertEquals("success", result.get("msg"));
        assertEquals(sqlChartResult, result.get("data"));

        verify(hisSlowsqlService).collectNodeSlowsqls(nodeId);
        verify(hisSlowsqlService).getSlowSqlChart(nodeId, start, end, step, dbName);
    }

    @Test
    void testSlowSqlChartWithException() {
        // Arrange
        String nodeId = "test-node-id";
        Long start = 1000L;
        Long end = 2000L;
        Integer step = 60;
        String dbName = "test-db";
        String errorMessage = "test slow sql chart occurred exception";
        doNothing().when(hisSlowsqlService).collectNodeSlowsqls(anyString());
        doThrow(new CustomException(errorMessage))
                .when(hisSlowsqlService)
                .getSlowSqlChart(anyString(), anyLong(), anyLong(), anyInt(), anyString());

        // Act
        AjaxResult result = slowLogController.slowSqlChart(nodeId, start, end, step, dbName);

        // Assert
        assertNotNull(result);
        assertEquals("success", result.get("msg"));
        assertEquals(errorMessage, result.get("data"));

        verify(hisSlowsqlService).collectNodeSlowsqls(nodeId);
        verify(hisSlowsqlService).getSlowSqlChart(nodeId, start, end, step, dbName);
    }

    @Test
    void testSlowSqlAggData() {
        // Arrange
        doNothing().when(hisSlowsqlService).collectNodeSlowsqls(anyString());
        when(hisSlowsqlService.selectSlowSqlAggData(any(SlowLogQuery.class))).thenReturn(mockResultStmtAgg);
        // Act
        AjaxResult result = slowLogController.slowSqlAggData(slowLogQuery);
        // Assert
        assertNotNull(result);
        assertEquals("success", result.get("msg"));
        assertEquals(mockResultStmtAgg, result.get("data"));
        verify(hisSlowsqlService).collectNodeSlowsqls("test-node-id");
        verify(hisSlowsqlService).selectSlowSqlAggData(slowLogQuery);
    }

    @Test
    void testSlowSqlChartWithNullParams() {
        // Arrange
        String nodeId = null;
        Long start = null;
        Long end = null;
        Integer step = null;
        String dbName = null;
        Map<String, Object> sqlChartResult = new HashMap<>();
        doNothing().when(hisSlowsqlService).collectNodeSlowsqls(any());
        when(hisSlowsqlService.getSlowSqlChart(any(), any(), any(), any(), any()))
                .thenReturn(sqlChartResult);
        // Act
        AjaxResult result = slowLogController.slowSqlChart(nodeId, start, end, step, dbName);
        // Assert
        assertNotNull(result);
        assertEquals("success", result.get("msg"));
        verify(hisSlowsqlService).collectNodeSlowsqls(null);
        verify(hisSlowsqlService).getSlowSqlChart(null, null, null, null, null);
    }
}
