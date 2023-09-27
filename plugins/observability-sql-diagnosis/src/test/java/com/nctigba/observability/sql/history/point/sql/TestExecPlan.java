/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.history.point.sql;

import com.alibaba.fastjson.JSONObject;
import com.nctigba.common.web.exception.HisDiagnosisException;
import com.nctigba.observability.sql.handler.TopSQLHandler;
import com.nctigba.observability.sql.mapper.history.HisDiagnosisTaskMapper;
import com.nctigba.observability.sql.model.history.HisDiagnosisResult;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.dto.AnalysisDTO;
import com.nctigba.observability.sql.service.ClusterManager;
import com.nctigba.observability.sql.service.history.DataStoreService;
import com.nctigba.observability.sql.service.history.collection.CollectionItem;
import com.nctigba.observability.sql.service.history.point.sql.ExecPlan;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TestExecPlan
 *
 * @author luomeng
 * @since 2023/9/19
 */
@RunWith(MockitoJUnitRunner.class)
public class TestExecPlan {
    @Mock
    private HisDiagnosisTaskMapper taskMapper;
    @Mock
    private ClusterManager clusterManager;
    @Mock
    private TopSQLHandler openGaussTopSQLHandler;
    @Mock
    private DataStoreService dataStoreService;
    @InjectMocks
    private ExecPlan pointService;

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
        AnalysisDTO result = pointService.analysis(mock(HisDiagnosisTask.class), dataStoreService);
        Assertions.assertEquals(HisDiagnosisResult.ResultState.NO_ADVICE, result.getIsHint());
        Assertions.assertEquals(HisDiagnosisResult.PointType.CENTER, result.getPointType());
        assertNotNull(result);
    }

    @Test
    public void testGetShowData() {
        try {
            Connection connection = mock(Connection.class);
            when(clusterManager.getConnectionByNodeId(any())).thenReturn(connection);
            PreparedStatement statement = mock(PreparedStatement.class);
            when(connection.prepareStatement(any())).thenReturn(statement);
            ResultSet resultSet = mock(ResultSet.class);
            when(statement.executeQuery()).thenReturn(resultSet);
            ResultSetMetaData metaData = mock(ResultSetMetaData.class);
            when(resultSet.getMetaData()).thenReturn(metaData);
            when(metaData.getColumnCount()).thenReturn(1);
            when(resultSet.next()).thenReturn(true, true, false);
            HisDiagnosisTask task = new HisDiagnosisTask();
            task.setNodeId("1");
            task.setDebugQueryId(1L);
            when(taskMapper.selectById(1)).thenReturn(task);
            when(openGaussTopSQLHandler.getExecutionPlan(any(), any(), any())).thenReturn(mock(JSONObject.class));
            Object data = pointService.getShowData(1);
            assertNotNull(data);
        } catch (SQLException | HisDiagnosisException e) {
            throw new HisDiagnosisException("error:", e);
        }
    }
}
