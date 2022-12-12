package com.nctigba.observability.instance.handler.instance;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

import com.nctigba.observability.instance.constants.DatabaseType;
import com.nctigba.observability.instance.constants.DriverConstants;
import com.nctigba.observability.instance.constants.JdbcConnectConstants;
import com.nctigba.observability.instance.model.InstanceNodeInfo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class NormalInstanceHandler implements InstanceHandler {
    @Override
    public String getDatabaseType() {
        return DatabaseType.DEFAULT.getDbType();
    }

    @Override
    public boolean testConnectStatus(DataSource dataSource, String testSql) {
        if (ObjectUtils.isEmpty(dataSource) || StringUtils.isEmpty(testSql)) {
            return false;
        }
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = DataSourceUtils.getConnection(dataSource);
            stmt = conn.createStatement();
            stmt.execute(testSql);
            return true;
        } catch (Exception e) {
            log.error("execute sql [{}] failed!", testSql);
            return false;
        } finally {
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    @Override
    public DataSource getDataSource(InstanceNodeInfo nodeInfo) {
        String host = nodeInfo.getIp();
        int port = nodeInfo.getPort();
        String dbName = nodeInfo.getDbName();
        String userName = nodeInfo.getDbUser();
        String userPassword = nodeInfo.getDbUserPassword();
        String conUrl = String.format(JdbcConnectConstants.DEFAULT, host, port, dbName);
        return assembleDataSource(conUrl, userName, userPassword);
    }

    private DataSource assembleDataSource(String conUrl, String userName, String userPassword) {
        var dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName(DriverConstants.DEFAULT);
        dataSourceBuilder.url(conUrl);
        dataSourceBuilder.username(userName);
        dataSourceBuilder.password(userPassword);
        return dataSourceBuilder.build();
    }

    private static void closeStatement(Statement stmt) {
        if (ObjectUtils.isNotEmpty(stmt)) {
            try {
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static void closeConnection(Connection conn) {
        if (ObjectUtils.isNotEmpty(conn)) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
