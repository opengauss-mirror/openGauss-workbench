/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.agent.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opengauss.plugin.agent.server.HostMetric;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
@AllArgsConstructor
public class DbUtil {
    private static Connection conn;

    private final HostMetric hostMetric;

    public final List<Map<String, Object>> query(String sql) {
        synchronized (conn) {
            try {
                conn = DriverManager.getConnection("jdbc:opengauss://" + "localhost" + ":" + hostMetric.getDbport()
                        + "/" + "postgres" + "?TimeZone=UTC", hostMetric.getDbUsername(), hostMetric.getDbPassword());
            } catch (SQLException e) {
                conn = null;
            }
        }
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
            return list;
        } catch (SQLException e) {
            log.error("sql error, cause :{} sql:{}", e.getMessage(), sql);
            return Collections.emptyList();
        }
    }
}