/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.history.point.sql;

import com.nctigba.common.web.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constants.history.OptionCommon;
import com.nctigba.observability.sql.mapper.history.HisDiagnosisResultMapper;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.dto.AnalysisDTO;
import com.nctigba.observability.sql.service.ClusterManager;
import com.nctigba.observability.sql.service.history.DataStoreService;
import com.nctigba.observability.sql.service.history.collection.CollectionItem;
import com.nctigba.observability.sql.service.history.collection.table.DatabaseItem;
import com.nctigba.observability.sql.service.history.point.sql.DatabaseParam;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TestDatabaseParam
 *
 * @author luomeng
 * @since 2023/9/19
 */
@RunWith(MockitoJUnitRunner.class)
public class TestDatabaseParam {
    @Mock
    private DatabaseItem item;
    @Mock
    private ClusterManager clusterManager;
    @Mock
    private DataStoreService dataStoreService;
    @Mock
    private HisDiagnosisResultMapper resultMapper;
    @InjectMocks
    private DatabaseParam pointService;

    @Test
    public void testGetOption() {
        String actual = String.valueOf(OptionCommon.IS_PARAM);
        List<String> list = pointService.getOption();
        assertEquals(actual, list.get(0));
    }

    @Test
    public void testGetSourceDataKeys() {
        List<CollectionItem<?>> result = pointService.getSourceDataKeys();
        assertEquals(1, result.size());
        assertEquals(item, result.get(0));
    }

    @Test
    public void testAnalysis_noData() {
        try {
            Connection conn = mock(Connection.class);
            when(clusterManager.getConnectionByNodeId(any())).thenReturn(conn);
            Statement statement = mock(Statement.class);
            when(conn.createStatement()).thenReturn(statement);
            ResultSet resultSet = mock(ResultSet.class);
            when(statement.executeQuery(any())).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true, true, false);
            when(resultMapper.update(any(), any())).thenReturn(1);
            AnalysisDTO result = pointService.analysis(mock(HisDiagnosisTask.class), dataStoreService);
            assertNull(result);
        } catch (SQLException e) {
            throw new HisDiagnosisException("execute sql failed!");
        }
    }

    @Test
    public void testAnalysis_hasData() {
        try {
            Connection conn = mock(Connection.class);
            when(clusterManager.getConnectionByNodeId(any())).thenReturn(conn);
            Statement statement = mock(Statement.class);
            when(conn.createStatement()).thenReturn(statement);
            ResultSet resultSet = mock(ResultSet.class);
            when(statement.executeQuery(any())).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true, true, false);
            when(resultMapper.update(any(), any())).thenReturn(1);
            when(resultSet.getString(1)).thenReturn("max_process_memory");
            AnalysisDTO result = pointService.analysis(mock(HisDiagnosisTask.class), dataStoreService);
            assertNull(result);
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