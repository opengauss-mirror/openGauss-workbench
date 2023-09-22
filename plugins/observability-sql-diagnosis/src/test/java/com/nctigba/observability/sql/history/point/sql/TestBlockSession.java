/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.history.point.sql;

import com.nctigba.common.web.exception.HisDiagnosisException;
import com.nctigba.observability.sql.mapper.history.HisDiagnosisTaskMapper;
import com.nctigba.observability.sql.model.history.HisDiagnosisResult;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.dto.AnalysisDTO;
import com.nctigba.observability.sql.service.ClusterManager;
import com.nctigba.observability.sql.service.history.DataStoreService;
import com.nctigba.observability.sql.service.history.collection.CollectionItem;
import com.nctigba.observability.sql.service.history.point.sql.BlockSession;
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
 * TestBlockSession
 *
 * @author luomeng
 * @since 2023/9/19
 */
@RunWith(MockitoJUnitRunner.class)
public class TestBlockSession {
    @Mock
    private ClusterManager clusterManager;
    @Mock
    private HisDiagnosisTaskMapper taskMapper;
    @Mock
    private DataStoreService dataStoreService;
    @InjectMocks
    private BlockSession pointService;

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
            when(resultSet.next()).thenReturn(true, true, false);
            AnalysisDTO result = pointService.analysis(mock(HisDiagnosisTask.class), dataStoreService);
            assertEquals(HisDiagnosisResult.ResultState.SUGGESTIONS, result.getIsHint());
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