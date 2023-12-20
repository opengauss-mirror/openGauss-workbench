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
 *  TestDbUtils.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/test/java/com/nctigba/observability/sql/history/util/TestDbUtils.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.history.util;

import com.nctigba.observability.sql.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constant.SqlConstants;
import com.nctigba.observability.sql.service.impl.ClusterManager;
import com.nctigba.observability.sql.util.DbUtils;
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
public class TestDbUtils {
    @Mock
    private ClusterManager clusterManager;

    @InjectMocks
    private DbUtils util;

    @Test
    public void testRangQuery_onNodeId() {
        String item = SqlConstants.POOR_SQL;
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
            String item = SqlConstants.POOR_SQL;
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
