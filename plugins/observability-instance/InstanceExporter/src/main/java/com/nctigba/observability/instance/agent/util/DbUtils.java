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
import com.nctigba.observability.instance.agent.enums.DbTypeEnum;
import com.nctigba.observability.instance.agent.config.model.TargetConfig;
import com.nctigba.observability.instance.agent.exception.CollectException;
import com.nctigba.observability.instance.agent.service.TargetService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.var;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Util to query database
 *
 * @since 2023/12/1
 */
@Component
@Log4j2
@AllArgsConstructor
public class DbUtils {
    private static Map<String, Connection> conns = new HashMap<>();

    private final TargetService targetService;

    /**
     * Clear all connections
     */
    public void clear() {
        conns = new HashMap<>();
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

        if (!createAndCacheConnectionIfNotExisted(nodeId)) {
            return Collections.emptyList();
        }

        Connection conn = conns.get(nodeId);
        try {
            var stmt = conn.createStatement();
            var rs = stmt.executeQuery(sql);
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
            log.debug("[sql time: {}s]query SQL: {}", executionTime, sql);
            if (executionTime > 500) {
                log.warn("[sql time: {}s]query SQL: {}", executionTime, sql);
            }

            return list;
        } catch (SQLException e) {
            if (!test(conn)) {
                conns.remove(nodeId);
                return query(nodeId, sql);
            }
            log.error("sql error, cause :{} sql:{}", e.getMessage(), sql);
            return Collections.emptyList();
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
        if (createAndCacheConnectionIfNotExisted(nodeId)) {
            return test(conns.get(nodeId));
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

    private boolean createAndCacheConnectionIfNotExisted(String nodeId) {
        Optional<TargetConfig> opTarget = targetService.getTargetConfigs()
            .stream().filter(z -> z.getNodeId().equals(nodeId)).findFirst();
        if (!opTarget.isPresent()) {
            throw new CollectException("No match node id target config for node Id:" + nodeId);
        }

        if (!conns.containsKey(nodeId)) {
            synchronized (this) {
                try {
                    if (!conns.containsKey(nodeId)) {
                        Connection conn = createConnection(opTarget.get());
                        conns.put(nodeId, conn);
                    }
                } catch (SQLException | ClassNotFoundException | NullPointerException e) {
                    log.error("db connection fail", e);
                    return false;
                }
            }
        }
        return true;
    }
}