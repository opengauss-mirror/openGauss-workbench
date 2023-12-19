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
 *  TestIndexAdvisor.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/test/java/com/nctigba/observability/sql/history/point/sql/TestIndexAdvisor.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.history.point.sql;

import com.nctigba.observability.sql.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constant.TestTxtConstants;
import com.nctigba.observability.sql.model.entity.DiagnosisResultDO;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.dto.point.AnalysisDTO;
import com.nctigba.observability.sql.service.impl.ClusterManager;
import com.nctigba.observability.sql.service.DataStoreService;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.service.impl.point.sql.IndexAdvisor;
import com.nctigba.observability.sql.util.PointUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TestIndexAdvisor
 *
 * @author luomeng
 * @since 2023/9/19
 */
@RunWith(MockitoJUnitRunner.class)
public class TestIndexAdvisor {
    @Mock
    private ClusterManager clusterManager;
    @Mock
    private PointUtils util;
    @Mock
    private DataStoreService dataStoreService;
    @InjectMocks
    private IndexAdvisor pointService;

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
    public void testAnalysis() {
        try {
            Connection conn = mock(Connection.class);
            when(clusterManager.getConnectionByNodeId(any())).thenReturn(conn);
            Statement statement = mock(Statement.class);
            when(conn.createStatement()).thenReturn(statement);
            ResultSet resultSet = mock(ResultSet.class);
            when(statement.executeQuery(any())).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true, false, true, false, true, false, true, false);
            String sql = TestTxtConstants.SQL_EXPLAIN;
            when(resultSet.getString(1)).thenReturn(sql, "tpch", sql);
            when(resultSet.getString(2)).thenReturn("lineitem");
            when(resultSet.getString(3)).thenReturn("l_returnflag, l_linestatus, count(*) AS count_order");
            when(resultSet.getString(4)).thenReturn("");
            DiagnosisTaskDO task = new DiagnosisTaskDO();
            task.setSql("SELECT l_returnflag, l_linestatus, count(*) AS count_order FROM tpch.lineitem"
                    + " GROUP BY l_returnflag, l_linestatus"
                    + " ORDER BY l_returnflag, l_linestatus");
            AnalysisDTO result = pointService.analysis(task, dataStoreService);
            assertEquals(DiagnosisResultDO.ResultState.NO_ADVICE, result.getIsHint());
            assertEquals(DiagnosisResultDO.PointType.DIAGNOSIS, result.getPointType());
            assertNotNull(result.getPointData());
        } catch (SQLException e) {
            throw new HisDiagnosisException("execute sql failed!");
        }
    }


    @Test
    public void testGetShowData() {
        Object result = pointService.getShowData(1);
        assertNull(result);
    }
}
