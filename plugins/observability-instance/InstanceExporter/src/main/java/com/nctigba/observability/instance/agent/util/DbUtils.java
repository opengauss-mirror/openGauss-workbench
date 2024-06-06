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
 *  DbUtils.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/InstanceExporter/
 *  src/main/java/com/nctigba/observability/instance/agent/util/DbUtils.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.agent.util;

import cn.hutool.core.text.StrFormatter;
import cn.hutool.core.thread.ThreadUtil;
import com.alibaba.druid.pool.DruidDataSource;
import com.nctigba.observability.instance.agent.constant.CollectConstants;
import com.nctigba.observability.instance.agent.enums.DbTypeEnum;
import com.nctigba.observability.instance.agent.config.model.TargetConfig;
import com.nctigba.observability.instance.agent.exception.CollectException;
import com.nctigba.observability.instance.agent.service.TargetService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.var;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

/**
 * Util to query database
 *
 * @since 2023/12/1
 */
@Component
@Log4j2
@AllArgsConstructor
public class DbUtils {

    private static Map<String, DataSource> dataSourceMap = new HashMap<>();

    private final TargetService targetService;

    /**
     * Clear all connections
     */
    public void clear() {
        dataSourceMap = new HashMap<>();
    }

    /**
     * Query a SQL
     *
     * @param nodeId Node Id for database
     * @param sql    SQL text
     * @return Query Result
     * @since 2023/12/1
     */
    public final List<Map<String, Object>> query(String nodeId, String sql) {
        long startTime = System.currentTimeMillis();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            DataSource dataSource = dataSourceMap.get(nodeId);
            conn = dataSource.getConnection();
            long midTime = System.currentTimeMillis();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            var rsmeta = rs.getMetaData();
            List<Map<String, Object>> list = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                for (int i = 0; i < rsmeta.getColumnCount(); i++) {
                    var obj = rs.getObject(i + 1);
                    if (obj == null) {
                        obj = "";
                    }
                    map.put(rsmeta.getColumnName(i + 1), obj);
                }
                list.add(map);
            }

            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            if (executionTime > CollectConstants.COLLECT_TIMEOUT * 1000L) {
                log.error("[sql time: {}ms]query SQL: {}", executionTime, sql);
                log.error("real cost {}ms", endTime - midTime);
                log.error("get connection cost {}ms", midTime - startTime);
            } else {
                log.debug("[sql time: {}ms]query SQL: {}", executionTime, sql);
            }
            rs.close();
            stmt.close();
            conn.close();
            return list;
        } catch (Exception e) {
            log.error("sql error, cause :{} sql:{}", e.getMessage(), sql);
            return Collections.emptyList();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                log.error("closed exception: {}", e.getMessage());
            }
        }
    }

    private static boolean test(Connection connection) {
        try {
            connection.createStatement().execute("SELECT 1");
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Test whether the database is available by nodeId.
     *
     * @param nodeId String
     * @return boolean
     */
    public boolean test(String nodeId) {
        DataSource dataSource = dataSourceMap.get(nodeId);
        Connection conn = null;
        try {
            if (dataSource != null) {
                conn = dataSource.getConnection();
                return test(conn);
            }
        } catch (SQLException e) {
            log.error("{}\n{}", e.getMessage(), e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                log.error("connection closed failed: {}", e.getMessage());
            }
        }
        return false;
    }

    private Connection createConnection(TargetConfig targetConfig) throws SQLException, ClassNotFoundException,
        NullPointerException {
        String dbType = targetConfig.getDbType();
        DbTypeEnum dbTypeEnum = DbTypeEnum.valueOf(dbType);
        Class.forName(dbTypeEnum.getDriverClass());
        String url = StrFormatter.format(dbTypeEnum.getUrlPattern(),
            targetConfig.getDbIp(), targetConfig.getDbPort());
        return DriverManager.getConnection(url, targetConfig.getDbUserName(),
            targetConfig.getDbUserPassword());
    }

    public void createDataSource(String nodeId) {
        Optional<TargetConfig> opTarget = targetService.getTargetConfigs()
            .stream().filter(z -> z.getNodeId().equals(nodeId)).findFirst();
        if (!opTarget.isPresent()) {
            throw new CollectException("No match node id target config for node Id:" + nodeId);
        }
        try {
            TargetConfig targetConfig = opTarget.get();
            DruidDataSource dataSource = new DruidDataSource();
            dataSource.setInitialSize(25);
            dataSource.setMaxActive(25);
            dataSource.setMaxWait(2000L);
            dataSource.setLoginTimeout(2);
            dataSource.setName(nodeId);
            String dbType = targetConfig.getDbType();
            DbTypeEnum dbTypeEnum = DbTypeEnum.valueOf(dbType);
            dataSource.setDriverClassName(dbTypeEnum.getDriverClass());
            dataSource.setUrl(StrFormatter.format(dbTypeEnum.getUrlPattern(), targetConfig.getDbIp(),
                targetConfig.getDbPort()));
            dataSource.setUsername(targetConfig.getDbUserName());
            dataSource.setPassword(targetConfig.getDbUserPassword());
            dataSource.setAsyncInit(true);
            dataSourceMap.put(nodeId, dataSource);
            dataSource.init();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}