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
 *  TriggerSqlServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/compatible/opengauss/TriggerSqlServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.compatible.opengauss;

import com.nctigba.datastudio.compatible.TriggerSqlService;
import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.model.query.TriggerQuery;
import com.nctigba.datastudio.utils.DebugUtils;
import com.nctigba.datastudio.utils.ExecuteUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.DESCRIPTION;
import static com.nctigba.datastudio.constants.CommonConstants.FUNCTION;
import static com.nctigba.datastudio.constants.CommonConstants.OPENGAUSS;
import static com.nctigba.datastudio.constants.CommonConstants.PRO_NAME;
import static com.nctigba.datastudio.constants.CommonConstants.RESULT;
import static com.nctigba.datastudio.constants.SqlConstants.COMMA;
import static com.nctigba.datastudio.constants.SqlConstants.COMMA_SPACE;
import static com.nctigba.datastudio.constants.SqlConstants.DISABLE_TRIGGER_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.DROP_TRIGGER_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.ENABLE_TRIGGER_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.LF;
import static com.nctigba.datastudio.constants.SqlConstants.POINT;
import static com.nctigba.datastudio.constants.SqlConstants.QUERY_COLUMN_NAME_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.QUERY_TRIGGER_FUNCTION_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.QUERY_TRIGGER_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.RENAME_TRIGGER_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.SEMICOLON;
import static com.nctigba.datastudio.constants.SqlConstants.SPACE;
import static com.nctigba.datastudio.constants.SqlConstants.TRIGGER_DDL_SQL;
import static com.nctigba.datastudio.utils.DebugUtils.needQuoteName;

/**
 * TriggerSqlServiceImpl
 *
 * @since 2023-10-19
 */
@Slf4j
@Service
public class TriggerSqlServiceImpl implements TriggerSqlService {
    @Autowired
    private ConnectionConfig connectionConfig;

    @Override
    public String type() {
        return OPENGAUSS;
    }

    @Override
    public List<String> queryFunction(TriggerQuery query) throws SQLException {
        log.info("TriggerSqlServiceImpl queryFunction query: " + query);
        try (
                Connection connection = connectionConfig.connectDatabase(query.getUuid());
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(
                        String.format(QUERY_TRIGGER_FUNCTION_SQL, needQuoteName(query.getUserName())))
        ) {
            List<String> list = new ArrayList<>();
            while (resultSet.next()) {
                list.add(resultSet.getString(PRO_NAME));
            }

            log.info("TriggerSqlServiceImpl queryFunction list: " + list);
            return list;
        }
    }

    @Override
    public void createFunction(TriggerQuery query) throws SQLException {
        log.info("TriggerSqlServiceImpl createFunction query: " + query);
        try (
                Connection connection = connectionConfig.connectDatabase(query.getUuid())
        ) {
            ExecuteUtils.execute(connection, query.getSql());
        }
    }

    @Override
    public String ddlPreview(TriggerQuery query) {
        log.info("TriggerSqlServiceImpl ddlPreview query: " + query);
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TRIGGER ").append(needQuoteName(query.getName())).append(LF)
                .append(query.getTime()).append(SPACE);

        StringBuilder eventSb = new StringBuilder();
        List<String> eventList = query.getEvent();
        for (int i = 0; i < eventList.size(); i++) {
            eventSb.append(eventList.get(i));
            if (eventList.get(i).equalsIgnoreCase("UPDATE")) {
                List<String> columnList = query.getColumnList();
                if (!CollectionUtils.isEmpty(columnList)) {
                    eventSb.append(" OF ").append(DebugUtils.listToString(columnList, COMMA_SPACE));
                }
            }
            if (i != eventList.size() - 1) {
                eventSb.append(" OR ");
            }
        }
        log.info("TriggerSqlServiceImpl ddlPreview eventSb: " + eventSb);
        sb.append(eventSb);

        sb.append(LF).append("ON ").append(needQuoteName(query.getSchema())).append(POINT)
                .append(needQuoteName(query.getTableName())).append(SPACE);

        sb.append("FOR EACH ").append(query.getFrequency());
        String condition = query.getCondition();
        if (StringUtils.isNotEmpty(condition)) {
            sb.append(" WHEN (").append(condition).append(")");
        }

        sb.append(LF).append("EXECUTE PROCEDURE ").append(query.getFunction()).append("();");
        log.info("TriggerSqlServiceImpl ddlPreview sb: " + sb);
        return sb.toString();
    }

    @Override
    public void create(TriggerQuery query) throws SQLException {
        log.info("TriggerSqlServiceImpl create query: " + query);
        try (
                Connection connection = connectionConfig.connectDatabase(query.getUuid());
                Statement statement = connection.createStatement();
        ) {
            String functionSql = query.getFunctionSql();
            if (StringUtils.isNotEmpty(functionSql)) {
                statement.execute(functionSql);
                log.info("TriggerSqlServiceImpl create function end: ");
            }
            statement.execute(ddlPreview(query));

            String description = query.getDescription();
            if (StringUtils.isNotEmpty(description)) {
                StringBuilder sb = new StringBuilder();
                sb.append(LF).append("COMMENT ON TRIGGER ").append(needQuoteName(query.getName())).append(" ON ")
                        .append(needQuoteName(query.getSchema())).append(POINT)
                        .append(needQuoteName(query.getTableName())).append(" IS '").append(description).append("';");
                log.info("TriggerSqlServiceImpl create sb: " + sb);
                statement.execute(sb.toString());
            }

            boolean isStatus = query.isStatus();
            if (!isStatus && "table".equals(query.getType())) {
                disable(query);
            }
        }
        log.info("TriggerSqlServiceImpl create end: ");
    }

    @Override
    public Map<String, Map<String, Object>> query(TriggerQuery query) throws SQLException {
        log.info("TriggerSqlServiceImpl query query: " + query);
        try (
                Connection connection = connectionConfig.connectDatabase(query.getUuid());
                Statement statement = connection.createStatement()
        ) {
            Map<String, Object> map = new HashMap<>();
            String oid = Strings.EMPTY;
            String attr = Strings.EMPTY;
            String sql = String.format(QUERY_TRIGGER_SQL, query.getName(), query.getTableName());
            log.info("TriggerSqlServiceImpl query sql: " + sql);
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                oid = resultSet.getString("oid");
                map.put("name", resultSet.getString("tgname"));
                map.put("status", resultSet.getString("tgenabled").equals("O"));
                map.put("type", resultSet.getString("relkind"));
                map.put("tableName", resultSet.getString("relname"));
                String hexString = Integer.toBinaryString(resultSet.getInt("tgtype"));
                parseHexString(hexString, map);
                map.put(FUNCTION, resultSet.getString("proname"));
                map.put("condition", "tgqual");
                map.put(DESCRIPTION, resultSet.getString(DESCRIPTION));
                attr = resultSet.getString("tgattr");
            }

            List<String> columnList = new ArrayList<>();
            List<String> eventList = (List<String>) map.get("event");
            if (eventList.contains("UPDATE") && StringUtils.isNotEmpty(attr)) {
                ResultSet result = statement.executeQuery(String.format(QUERY_COLUMN_NAME_SQL, oid,
                        attr.replace(SPACE, COMMA)));
                while (result.next()) {
                    columnList.add(result.getString("attname"));
                }
            }
            map.put("columnList", columnList);
            Map<String, Map<String, Object>> resultMap = new HashMap<>();
            resultMap.put(RESULT, map);
            log.info("TriggerSqlServiceImpl query map: " + resultMap);
            return resultMap;
        }
    }

    private void parseHexString(String hexString, Map<String, Object> map) {
        log.info("TriggerSqlServiceImpl parseHexString hexString: " + hexString);
        char ch = '1';
        StringBuilder hexStringBuilder = new StringBuilder(hexString);
        while (hexStringBuilder.length() < 8) {
            hexStringBuilder.insert(0, "0");
        }
        log.info("TriggerSqlServiceImpl parseHexString hexStringBuilder: " + hexStringBuilder);
        if (hexStringBuilder.charAt(7) == ch) {
            map.put("frequency", "ROW");
        } else {
            map.put("frequency", "STATEMENT");
        }
        if (hexStringBuilder.charAt(6) == ch) {
            map.put("time", "BEFORE");
        } else {
            map.put("time", "AFTER");
        }
        List<String> eventList = new ArrayList<>();
        if (hexStringBuilder.charAt(5) == ch) {
            eventList.add("INSERT");
        }
        if (hexStringBuilder.charAt(4) == ch) {
            eventList.add("DELETE");
        }
        if (hexStringBuilder.charAt(3) == ch) {
            eventList.add("UPDATE");
        }
        if (hexStringBuilder.charAt(2) == ch) {
            eventList.add("TRUNCATE");
        }
        map.put("event", eventList);
        if (hexStringBuilder.charAt(1) == ch) {
            map.put("time", "INSEAD OF");
        }
        log.info("TriggerSqlServiceImpl parseHexString map: " + map);
    }

    @Override
    public void rename(TriggerQuery query) throws SQLException {
        log.info("TriggerSqlServiceImpl rename query: " + query);
        try (
                Connection connection = connectionConfig.connectDatabase(query.getUuid());
                Statement statement = connection.createStatement()
        ) {
            String name = query.getName();
            String newName = query.getNewName();
            if (StringUtils.isNotEmpty(newName) && !newName.equals(name)) {
                statement.execute(String.format(RENAME_TRIGGER_SQL, needQuoteName(name),
                        needQuoteName(query.getSchema()), needQuoteName(query.getTableName()), needQuoteName(newName)));
            }
            log.info("TriggerSqlServiceImpl rename end: ");
        }
    }

    @Override
    public void edit(TriggerQuery query) throws SQLException {
        log.info("TriggerSqlServiceImpl edit query: " + query);
        String tableName = query.getTableName();
        String newTableName = query.getNewTableName();
        query.setTableName(newTableName);
        create(query);

        if (StringUtils.isNotEmpty(newTableName) && !newTableName.equals(tableName)) {
            query.setTableName(tableName);
            delete(query);
        }
        log.info("TriggerSqlServiceImpl edit end: ");
    }

    @Override
    public void delete(TriggerQuery query) throws SQLException {
        log.info("TriggerSqlServiceImpl delete query: " + query);
        try (
                Connection connection = connectionConfig.connectDatabase(query.getUuid())
        ) {
            String sql = String.format(DROP_TRIGGER_SQL, needQuoteName(query.getName()),
                    needQuoteName(query.getSchema()), needQuoteName(query.getTableName()));
            ExecuteUtils.execute(connection, sql);
        }
    }

    @Override
    public void enable(TriggerQuery query) throws SQLException {
        log.info("TriggerSqlServiceImpl enable query: " + query);
        try (
                Connection connection = connectionConfig.connectDatabase(query.getUuid())
        ) {
            String sql = String.format(ENABLE_TRIGGER_SQL, needQuoteName(query.getSchema()),
                    needQuoteName(query.getTableName()), needQuoteName(query.getName()));
            ExecuteUtils.execute(connection, sql);
        }
    }

    @Override
    public void disable(TriggerQuery query) throws SQLException {
        log.info("TriggerSqlServiceImpl disable query: " + query);
        try (
                Connection connection = connectionConfig.connectDatabase(query.getUuid())
        ) {
            String sql = String.format(DISABLE_TRIGGER_SQL, needQuoteName(query.getSchema()),
                    needQuoteName(query.getTableName()), needQuoteName(query.getName()));
            ExecuteUtils.execute(connection, sql);

        }
    }

    @Override
    public Map<String, String> showDdl(TriggerQuery query) throws SQLException {
        log.info("TriggerSqlServiceImpl showDdl query: " + query);
        try (
                Connection connection = connectionConfig.connectDatabase(query.getUuid())
        ) {
            String result = null;
            Object obj = ExecuteUtils.executeGetOne(connection, String.format(TRIGGER_DDL_SQL, query.getOid()));
            if (obj instanceof String) {
                result = (String) obj;
            }

            if (StringUtils.isNotEmpty(result)) {
                result = result.replace("((", "(").replace("))", ")");
            }
            Map<String, String> map = new HashMap<>();
            map.put(RESULT, result + SEMICOLON);
            log.info("TriggerSqlServiceImpl showDdl map: " + map);
            return map;
        }
    }
}
