/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.history.util;

import com.nctigba.common.web.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constants.history.SqlCommon;
import com.nctigba.observability.sql.service.ClusterManager;
import com.nctigba.observability.sql.util.DbUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TestDbUtil
 *
 * @author luomeng
 * @since 2023/7/13
 */
@RunWith(MockitoJUnitRunner.class)
public class TestDbUtil {
    @Mock
    private ClusterManager clusterManager;

    @InjectMocks
    private DbUtil util;

    @Test
    public void testRangQuery_onNodeId() {
        String item = SqlCommon.POOR_SQL;
        Date startTime = new Date();
        Date endTime = new Date();
        String nodeId = "37e8a893-0b7e-49b2-a0b4-e6fdf7dc4345";
        Object object = util.rangQuery(item, startTime, endTime, nodeId);
        assertNotNull(object);
    }

    @Test
    public void testRangQuery_hasNodeId() {
        when(clusterManager.getOpsClusterIdByNodeId(any())).thenReturn("test");
        try {
            Connection connection = mock(Connection.class);
            when(clusterManager.getConnectionByNodeId(any())).thenReturn(connection);
            Statement statement = mock(Statement.class);
            when(connection.createStatement()).thenReturn(statement);
            ResultSet resultSet = mock(ResultSet.class);
            when(statement.executeQuery(any())).thenReturn(resultSet);
            ResultSetMetaData metaData = mock(ResultSetMetaData.class);
            when(resultSet.getMetaData()).thenReturn(metaData);
            when(resultSet.next()).thenReturn(true, true, false);
            String item = SqlCommon.POOR_SQL;
            Date startTime = new Date();
            Date endTime = new Date();
            String nodeId = "37e8a893-0b7e-49b2-a0b4-e6fdf7dc4345";
            Object object = util.rangQuery(item, startTime, endTime, nodeId);
            assertNotNull(object);
            connection.close();
        } catch (SQLException e) {
            throw new HisDiagnosisException("connect fail");
        }
    }
}
