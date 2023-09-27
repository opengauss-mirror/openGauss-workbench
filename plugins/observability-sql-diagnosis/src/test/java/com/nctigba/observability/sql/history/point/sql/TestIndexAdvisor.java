/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.history.point.sql;

import com.nctigba.common.web.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constants.TestTxtCommon;
import com.nctigba.observability.sql.model.history.HisDiagnosisResult;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.dto.AnalysisDTO;
import com.nctigba.observability.sql.service.ClusterManager;
import com.nctigba.observability.sql.service.history.DataStoreService;
import com.nctigba.observability.sql.service.history.collection.CollectionItem;
import com.nctigba.observability.sql.service.history.point.sql.IndexAdvisor;
import com.nctigba.observability.sql.util.PointUtil;
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
    private PointUtil util;
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
            String sql = TestTxtCommon.SQL_EXPLAIN;
            when(resultSet.getString(1)).thenReturn(sql, "tpch", sql);
            when(resultSet.getString(2)).thenReturn("lineitem");
            when(resultSet.getString(3)).thenReturn("l_returnflag, l_linestatus, count(*) AS count_order");
            when(resultSet.getString(4)).thenReturn("");
            HisDiagnosisTask task = new HisDiagnosisTask();
            task.setSql("SELECT l_returnflag, l_linestatus, count(*) AS count_order FROM tpch.lineitem"
                    + " GROUP BY l_returnflag, l_linestatus"
                    + " ORDER BY l_returnflag, l_linestatus");
            AnalysisDTO result = pointService.analysis(task, dataStoreService);
            assertEquals(HisDiagnosisResult.ResultState.NO_ADVICE, result.getIsHint());
            assertEquals(HisDiagnosisResult.PointType.DIAGNOSIS, result.getPointType());
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
