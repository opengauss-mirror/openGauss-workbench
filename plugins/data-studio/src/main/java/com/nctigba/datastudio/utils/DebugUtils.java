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
 *  DebugUtils.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/utils/DebugUtils.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.utils;

import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.constants.SqlConstants;
import com.nctigba.datastudio.enums.ParamTypeEnum;
import com.nctigba.datastudio.model.entity.OperateStatusDO;
import com.nctigba.datastudio.model.query.PublicParamQuery;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.opengauss.admin.common.exception.CustomException;
import org.opengauss.util.PGobject;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.nctigba.datastudio.constants.CommonConstants.CAN_BREAK;
import static com.nctigba.datastudio.constants.CommonConstants.COLUMN;
import static com.nctigba.datastudio.constants.CommonConstants.DIFFER;
import static com.nctigba.datastudio.constants.CommonConstants.DOLLAR;
import static com.nctigba.datastudio.constants.CommonConstants.FUNCTION_DOLLAR;
import static com.nctigba.datastudio.constants.CommonConstants.FUNC_OID;
import static com.nctigba.datastudio.constants.CommonConstants.IN;
import static com.nctigba.datastudio.constants.CommonConstants.INOUT;
import static com.nctigba.datastudio.constants.CommonConstants.KEY;
import static com.nctigba.datastudio.constants.CommonConstants.LINE_NO;
import static com.nctigba.datastudio.constants.CommonConstants.NAME;
import static com.nctigba.datastudio.constants.CommonConstants.NSP_NAME;
import static com.nctigba.datastudio.constants.CommonConstants.NULL_STR;
import static com.nctigba.datastudio.constants.CommonConstants.OID;
import static com.nctigba.datastudio.constants.CommonConstants.OUT;
import static com.nctigba.datastudio.constants.CommonConstants.PRON_ARGS;
import static com.nctigba.datastudio.constants.CommonConstants.PRO_ARG_MODES;
import static com.nctigba.datastudio.constants.CommonConstants.PRO_ARG_NAMES;
import static com.nctigba.datastudio.constants.CommonConstants.PRO_ARG_TYPES;
import static com.nctigba.datastudio.constants.CommonConstants.PRO_NAME;
import static com.nctigba.datastudio.constants.CommonConstants.RESULT;
import static com.nctigba.datastudio.constants.CommonConstants.SLASH;
import static com.nctigba.datastudio.constants.CommonConstants.SPACE;
import static com.nctigba.datastudio.constants.CommonConstants.STATEMENT;
import static com.nctigba.datastudio.constants.CommonConstants.TRANS_POINT;
import static com.nctigba.datastudio.constants.CommonConstants.TYPE;
import static com.nctigba.datastudio.constants.CommonConstants.TYPE_NAME;
import static com.nctigba.datastudio.constants.CommonConstants.TYPE_NUM;
import static com.nctigba.datastudio.constants.CommonConstants.TYP_NAME;
import static com.nctigba.datastudio.constants.CommonConstants.T_STR;
import static com.nctigba.datastudio.constants.CommonConstants.VARIADIC;
import static com.nctigba.datastudio.constants.SqlConstants.BACKTRACE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.BACKTRACE_SQL_PRE;
import static com.nctigba.datastudio.constants.SqlConstants.COMMA;
import static com.nctigba.datastudio.constants.SqlConstants.COMMA_SPACE;
import static com.nctigba.datastudio.constants.SqlConstants.FUNCTION_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.GET_OID_NAME_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.INFO_CODE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.LEFT_BRACKET;
import static com.nctigba.datastudio.constants.SqlConstants.LF;
import static com.nctigba.datastudio.constants.SqlConstants.POINT;
import static com.nctigba.datastudio.constants.SqlConstants.PROC_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.QUOTES;
import static com.nctigba.datastudio.constants.SqlConstants.RIGHT_BRACKET;
import static com.nctigba.datastudio.dao.ConnectionMapDAO.conMap;

/**
 * DebugUtils
 *
 * @since 2023-6-26
 */
@Slf4j
public class DebugUtils {
    /**
     * get schema by sql
     *
     * @param sql sql
     * @return String
     */
    public static String getSchemaBySql(String sql) {
        String substring = sql.substring(0, sql.indexOf(LEFT_BRACKET));
        log.info("DebugUtils getSchemaBySql substring: " + substring);
        String[] split = substring.trim().split(SPACE);

        String fullName = split[split.length - 1];
        log.info("DebugUtils getSchemaBySql fullName: " + fullName);
        if (!fullName.contains(POINT)) {
            return "";
        }
        return fullName.split(TRANS_POINT)[0];
    }

    /**
     * get name by sql
     *
     * @param sql sql
     * @return String
     */
    public static String getNameBySql(String sql) {
        String substring = sql.substring(0, sql.indexOf(LEFT_BRACKET));
        log.info("DebugUtils getNameBySql substring: " + substring);
        String[] split = substring.trim().split(SPACE);

        String fullName = split[split.length - 1];
        log.info("DebugUtils getNameBySql fullName: " + fullName);
        if (!fullName.contains(POINT)) {
            return fullName;
        }
        return fullName.split(TRANS_POINT)[1];
    }

    /**
     * get param map
     *
     * @param webSocketServer webSocketServer
     * @param windowName      windowName
     * @param oid             oid
     * @return List
     * @throws SQLException SQLException
     */
    public static List<Map<String, Object>> getParamMap(
            WebSocketServer webSocketServer, String windowName, String oid) throws SQLException {
        log.info("DebugUtils getParamMap windowName--oid: " + windowName + "--" + oid);
        List<Map<String, Object>> list = new ArrayList<>();
        Statement statement = webSocketServer.getStatement(windowName);
        String[] proArgNames = new String[]{};
        String argTypes = Strings.EMPTY;

        try (
                ResultSet resultSet = statement.executeQuery(String.format(PROC_SQL, oid))
        ) {
            while (resultSet.next()) {
                int pronArgs = resultSet.getInt(PRON_ARGS);
                if (pronArgs == 0) {
                    return null;
                }
                argTypes = resultSet.getString(PRO_ARG_TYPES);
                String argNames = resultSet.getString(PRO_ARG_NAMES);
                if (StringUtils.isNotEmpty(argNames)) {
                    proArgNames = argNames.substring(1, argNames.length() - 1).split(COMMA);
                } else {
                    proArgNames = new String[argTypes.split(SPACE).length];
                }
            }
        }

        try (
                ResultSet nameResult = statement.executeQuery(
                        String.format(GET_OID_NAME_SQL, argTypes.replace(SPACE, COMMA)))
        ) {
            while (nameResult.next()) {
                for (int i = 0; i < argTypes.split(SPACE).length; i++) {
                    Map<String, Object> map = new HashMap<>();
                    map.put(KEY, proArgNames[i]);
                    map.put(TYPE, ParamTypeEnum.parseType(nameResult.getString(TYP_NAME)));
                    list.add(map);
                }
            }
            log.info("DebugUtils getParamMap list: " + list);
            return list;
        }
    }

    /**
     * get oid list
     *
     * @param webSocketServer webSocketServer
     * @param rootWindowName  rootWindowName
     * @return List
     * @throws SQLException SQLException
     */
    public static List<String> getOidList(WebSocketServer webSocketServer, String rootWindowName) throws SQLException {
        log.info("DebugUtils getOidList rootWindowName: " + rootWindowName);
        List<String> list = new ArrayList<>();
        Statement statement = DebugUtils.changeParamType(webSocketServer, rootWindowName, STATEMENT);
        ResultSet stackResult = statement.executeQuery(BACKTRACE_SQL_PRE + 0 + BACKTRACE_SQL);
        while (stackResult.next()) {
            list.add(stackResult.getString(FUNC_OID));
        }
        log.info("DebugUtils getOidList list: " + list);
        return list;
    }

    /**
     * get param type
     *
     * @param webSocketServer webSocketServer
     * @param paramReq        return
     * @return Map
     * @throws SQLException SQLException
     */
    public static Map<String, Object> getParamType(WebSocketServer webSocketServer, PublicParamQuery paramReq)
            throws SQLException {
        log.info("DebugUtils getParamType paramReq: " + paramReq);
        Map<String, Object> map = new HashMap<>();
        try (
                Connection connection = webSocketServer.createConnection(paramReq.getUuid(), paramReq.getWindowName());
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(String.format(PROC_SQL, paramReq.getOid()))
        ) {
            while (resultSet.next()) {
                String argName = resultSet.getString(PRO_ARG_NAMES);
                if (StringUtils.isEmpty(argName)) {
                    return map;
                }
                String[] proArgNames = argName.substring(1, argName.length() - 1).split(COMMA);
                for (int i = 0; i < proArgNames.length; i++) {
                    String argMode = resultSet.getString(PRO_ARG_MODES);
                    if (StringUtils.isEmpty(argMode)) {
                        map.put(proArgNames[i], IN);
                    } else {
                        String[] proArgModes = argMode.substring(1, argMode.length() - 1).split(COMMA);
                        switch (proArgModes[i]) {
                            case "i":
                                map.put(proArgNames[i], IN);
                                break;
                            case "o":
                                map.put(proArgNames[i], OUT);
                                break;
                            case "b":
                                map.put(proArgNames[i], INOUT);
                                break;
                            case "v":
                                map.put(proArgNames[i], VARIADIC);
                                break;
                        }
                    }
                }
            }
        }
        log.info("DebugUtils getParamType map: " + map);
        return map;
    }

    /**
     * prepare sql
     *
     * @param paramReq paramReq
     * @return String
     */
    public static String prepareSql(PublicParamQuery paramReq) {
        String sql = paramReq.getSql();
        StringBuilder sb = new StringBuilder();
        List<Map<String, Object>> inputParams = paramReq.getInputParams();
        log.info("DebugUtils prepareSql inputParams: " + inputParams);

        if (!CollectionUtils.isEmpty(inputParams)) {
            for (int i = 0; i < inputParams.size(); i++) {
                Map<String, Object> paramMap = inputParams.get(i);
                Set<String> keySet = paramMap.keySet();
                for (String key : keySet) {
                    if (key.contains("int") || key.contains("number") || key.contains("float") || key.contains("double")
                            || key.contains("real") || key.contains("serial")) {
                        sb.append(paramMap.get(key));
                    } else {
                        sb.append("'").append(paramMap.get(key)).append("'");
                    }
                }

                if (i != inputParams.size() - 1) {
                    sb.append(COMMA_SPACE);
                }
            }
        }

        log.info("DebugUtils prepareSql sql: " + sb);
        return String.format(FUNCTION_SQL, getFullName(sql), sb);
    }

    /**
     * get full name
     *
     * @param sql sql
     * @return String
     */
    public static String getFullName(String sql) {
        String[] split = sql.split("\\(");
        String[] str = split[0].trim().split(SPACE);
        log.info("DebugUtils getFullName str: " + Arrays.toString(str));
        return str[str.length - 1];
    }

    /**
     * parse resulte set
     *
     * @param resultSet resultSet
     * @return Map
     * @throws SQLException SQLException
     */
    public static Map<String, Object> parseResultSet(ResultSet resultSet) throws SQLException {
        Map<String, Object> map = new HashMap<>();
        List<String> columnList = getColumnList(resultSet.getMetaData(), map);

        List<List<Object>> dataList = new ArrayList<>();
        while (resultSet.next()) {
            List<Object> list = new ArrayList<>();
            for (String column : columnList) {
                list.add(resultSet.getObject(column));
            }
            dataList.add(list);
        }
        map.put(RESULT, dataList);
        log.info("DebugUtils parseResultSet map: {}", map);
        return map;
    }

    /**
     * parse resulte set
     *
     * @param resultSet resultSet
     * @return Map
     * @throws SQLException SQLException
     */
    public static Map<String, Object> parseResultSetType(ResultSet resultSet) throws SQLException {
        Map<String, Object> map = new HashMap<>();
        List<String> columnList = getColumnList(resultSet.getMetaData(), map);
        getColumnTypeList(resultSet.getMetaData(), map);
        getColumnTypeNamwList(resultSet.getMetaData(), map);
        List<List<Object>> dataList = new ArrayList<>();
        while (resultSet.next()) {
            List<Object> list = new ArrayList<>();
            for (String column : columnList) {
                Object columnValue = resultSet.getObject(column);
                if (columnValue instanceof PGobject) {
                    list.add(((PGobject) columnValue).getValue());
                } else {
                    list.add(objectToSting(columnValue));
                }
            }
            dataList.add(list);
        }
        map.put(RESULT, dataList);
        log.info("DebugUtils parseResultSetType map: {}", map);
        return map;
    }

    private static Object objectToSting(Object obj) {
        if (ObjectUtils.isNotEmpty(obj)) {
            return obj.toString();
        } else {
            return obj;
        }
    }

    /**
     * parse break point
     *
     * @param resultSet resultSet
     * @param oid       oid
     * @return Map
     * @throws SQLException SQLException
     */
    public static Map<String, Object> parseBreakPoint(ResultSet resultSet, String oid) throws SQLException {
        Map<String, Object> map = new HashMap<>();
        List<String> columnList = getColumnList(resultSet.getMetaData(), map);

        List<List<Object>> dataList = new ArrayList<>();
        while (resultSet.next()) {
            List<Object> list = new ArrayList<>();
            for (String column : columnList) {
                String str = resultSet.getString(column);
                if (str.equals("t")) {
                    list.add(true);
                } else if (str.equals("f")) {
                    list.add(false);
                } else {
                    list.add(str);
                }
            }
            if (list.contains(oid)) {
                dataList.add(list);
            }
        }
        map.put(RESULT, dataList);
        log.info("DebugUtils parseBeakPoint map: " + map);
        return map;
    }

    /**
     * parse variable
     *
     * @param resultSet resultSet
     * @return Map
     * @throws SQLException SQLException
     */
    public static Map<String, Object> parseVariable(ResultSet resultSet) throws SQLException {
        Map<String, Object> map = new HashMap<>();
        List<String> columnList = getColumnList(resultSet.getMetaData(), map);

        List<List<Object>> dataList = new ArrayList<>();
        while (resultSet.next()) {
            List<Object> list = new ArrayList<>();
            for (String column : columnList) {
                String str = resultSet.getString(column);
                if ("vartype".equals(column) || "typname".equals(column)) {
                    list.add(ParamTypeEnum.parseType(str));
                } else {
                    list.add(str);
                }
            }
            dataList.add(list);
        }
        map.put(RESULT, dataList);
        log.info("DebugUtils parseVariable map: " + map);
        return map;
    }

    /**
     * get column list
     *
     * @param metaData metaData
     * @param map      map
     * @return List
     * @throws SQLException SQLException
     */
    private static List<String> getColumnList(ResultSetMetaData metaData, Map<String, Object> map) throws SQLException {
        List<String> columnList = new ArrayList<>();
        for (int i = 0; i < metaData.getColumnCount(); i++) {
            columnList.add(metaData.getColumnName(i + 1));
        }
        map.put(COLUMN, columnList);
        log.info("DebugUtils getColumnList: " + columnList);
        return columnList;
    }

    /**
     * get column type list
     *
     * @param metaData metaData
     * @param map      map
     * @throws SQLException SQLException
     */
    private static void getColumnTypeList(ResultSetMetaData metaData, Map<String, Object> map) throws SQLException {
        List<Integer> columnTypeList = new ArrayList<>();
        for (int i = 0; i < metaData.getColumnCount(); i++) {
            columnTypeList.add(metaData.getColumnType(i + 1));
        }
        map.put(TYPE_NUM, columnTypeList);
        log.info("DebugUtils getColumnList: " + columnTypeList);
    }

    /**
     * get column type list
     *
     * @param metaData metaData
     * @param map      map
     * @throws SQLException SQLException
     */
    private static void getColumnTypeNamwList(ResultSetMetaData metaData, Map<String, Object> map) throws SQLException {
        List<String> columnTypeNameList = new ArrayList<>();
        for (int i = 0; i < metaData.getColumnCount(); i++) {
            columnTypeNameList.add(metaData.getColumnTypeName(i + 1));
        }
        map.put(TYPE_NAME, columnTypeNameList);
        log.info("DebugUtils getColumnList: " + columnTypeNameList);
    }

    /**
     * add param map
     *
     * @param map             map
     * @param webSocketServer webSocketServer
     * @param paramReq        paramReq
     * @return Map
     * @throws SQLException SQLException
     */
    public static Map<String, Object> addMapParam(
            Map<String, Object> map, WebSocketServer webSocketServer, PublicParamQuery paramReq) throws SQLException {
        log.info("DebugUtils addMapParam map: " + map);
        List<String> columnList = (List<String>) map.get(COLUMN);
        List<List<Object>> dataList = (List<List<Object>>) map.get(RESULT);

        Map<String, Object> paramList = getParamType(webSocketServer, paramReq);
        log.info("DebugUtils addMapParam paramList: " + paramList);
        columnList.add("paramType");
        for (int i = 0; i < dataList.size(); i++) {
            List<Object> data = dataList.get(i);
            if (!CollectionUtils.isEmpty(paramList)) {
                if (i < paramList.size()) {
                    data.add(paramList.get(data.get(0)));
                } else {
                    data.add("temp");
                }
            } else {
                data.add("temp");
            }
        }
        log.info("DebugUtils addMapParam map: " + map);
        return map;
    }

    /**
     * sql handle after
     *
     * @param sql sql
     * @return String
     */
    public static String sqlHandleAfter(String sql) {
        sql = sql.replace(FUNCTION_DOLLAR, DOLLAR);
        return sql + SLASH;
    }

    /**
     * replace line
     *
     * @param sql sql
     * @return String
     */
    public static String replaceLine(String sql) {
        return sql.replace(SqlConstants.SPACE, NULL_STR).replace(LF, NULL_STR).replace("\n", NULL_STR)
                .replace("\r", NULL_STR);
    }

    /**
     * disable button
     *
     * @param webSocketServer webSocketServer
     * @param windowName      windowName
     */
    public static void disableButton(WebSocketServer webSocketServer, String windowName) {
        OperateStatusDO operateStatus = webSocketServer.getOperateStatus(windowName);
        operateStatus.subAllFalse();
        webSocketServer.setOperateStatus(windowName, operateStatus);
    }

    /**
     * enable button
     *
     * @param webSocketServer webSocketServer
     * @param windowName      windowName
     */
    public static void enableButton(WebSocketServer webSocketServer, String windowName) {
        OperateStatusDO operateStatus = webSocketServer.getOperateStatus(windowName);
        operateStatus.subAllTrue();
        webSocketServer.setOperateStatus(windowName, operateStatus);
    }

    /**
     * get available break point
     *
     * @param paramReq        paramReq
     * @param webSocketServer webSocketServer
     * @return List
     */
    public static List<Integer> getAvailableBreakPoints(PublicParamQuery paramReq, WebSocketServer webSocketServer) {
        List<Integer> list = new ArrayList<>();
        int differ = 0;
        try (
                Connection connection = webSocketServer.createConnection(paramReq.getUuid(), paramReq.getWindowName());
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(String.format(INFO_CODE_SQL, paramReq.getOid()))
        ) {
            while (resultSet.next()) {
                String lineNo = resultSet.getString(LINE_NO);
                if (StringUtils.isEmpty(lineNo)) {
                    differ++;
                }
                String canBreak = resultSet.getString(CAN_BREAK);
                if (T_STR.equals(canBreak)) {
                    list.add(Integer.valueOf(lineNo));
                }
            }

            String windowName = paramReq.getWindowName();
            webSocketServer.setParamMap(windowName, CAN_BREAK, list);
            webSocketServer.setParamMap(windowName, DIFFER, differ);
            log.info("DebugUtils getAvailableBreakPoints differ: " + differ);
            log.info("DebugUtils getAvailableBreakPoints list: " + list);
        } catch (SQLException e) {
            log.info(e.getMessage());
        }
        return list;
    }

    /**
     * get result map
     *
     * @param webSocketServer webSocketServer
     * @param paramReq        paramReq
     * @return Map
     */
    public static Map<String, Map<String, String>> getResultMap(
            WebSocketServer webSocketServer, PublicParamQuery paramReq) {
        log.info("DebugUtils getResultMap paramReq: " + paramReq);
        Map<String, String> paramMap = new HashMap<>();
        String oid = paramReq.getOid();
        paramMap.put(OID, oid);
        StringBuilder sb = new StringBuilder();
        String proArgTypes = Strings.EMPTY;
        try (
                Connection connection = webSocketServer.createConnection(paramReq.getUuid(), paramReq.getWindowName());
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(String.format(PROC_SQL, oid))
        ) {
            while (resultSet.next()) {
                String schemaNew = resultSet.getString(NSP_NAME);
                String nameNew = resultSet.getString(PRO_NAME);
                proArgTypes = resultSet.getString(PRO_ARG_TYPES);
                log.info("DebugUtils getResultMap schemaNew: " + schemaNew);
                log.info("DebugUtils getResultMap nameNew: " + nameNew);
                log.info("DebugUtils getResultMap proArgTypes: " + proArgTypes);
                sb.append(schemaNew).append(POINT).append(nameNew).append(LEFT_BRACKET);
            }

            if (StringUtils.isNotEmpty(proArgTypes)) {
                try (
                        ResultSet nameResult = statement.executeQuery(
                                String.format(GET_OID_NAME_SQL, proArgTypes.replace(SPACE, COMMA)))
                ) {
                    while (nameResult.next()) {
                        int length = proArgTypes.split(SPACE).length;
                        for (int i = 0; i < length; i++) {
                            sb.append(ParamTypeEnum.parseType(nameResult.getString(TYP_NAME)));
                            if (i != length - 1) {
                                sb.append(COMMA);
                            }
                        }
                    }
                }
            }

            sb.append(RIGHT_BRACKET);
            log.info("DebugUtils sb: " + sb);
            paramMap.put(NAME, sb.toString());
        } catch (SQLException e) {
            log.info(e.getMessage());
        }

        Map<String, Map<String, String>> map = new HashMap<>();
        map.put(RESULT, paramMap);
        log.info("DebugUtils map: " + map);
        return map;
    }

    /**
     * export file
     *
     * @param fileName fileName
     * @param path  path
     * @param response response
     * @throws IOException IOException
     */
    public static void exportFile(String fileName, String path, HttpServletResponse response) throws IOException {
        log.info("ExportService exportFilePath fileName: " + fileName);
        response.reset();
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", URLEncoder.encode(fileName, StandardCharsets.UTF_8));
        response.addHeader("Response-Type", "blob");
        response.setCharacterEncoding("UTF-8");

        try (
                BufferedReader reader = new BufferedReader(new FileReader(path), 4096);
                OutputStream outputStream = response.getOutputStream()
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                outputStream.write(line.getBytes(StandardCharsets.UTF_8));
                outputStream.write(System.lineSeparator().getBytes());
            }
            outputStream.flush();
        }

        deleteFile(path);
    }

    /**
     * delete file
     *
     * @param path path
     */
    public static void deleteFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            boolean isDelete = file.delete();
            log.info("ExportService exportFilePath isDelete: " + isDelete);
        }
    }

    /**
     * get old window name
     *
     * @param webSocketServer webSocketServer
     * @param rootWindowName  rootWindowName
     * @param oid             oid
     * @return String
     */
    public static String getOldWindowName(WebSocketServer webSocketServer, String rootWindowName, String oid) {
        String name = Strings.EMPTY;
        Map<String, Object> paramMap = webSocketServer.getParamMap(rootWindowName);
        log.info("DebugUtils getOldWindowName paramMap: " + paramMap);
        for (String key : paramMap.keySet()) {
            if (key.equals(oid)) {
                if (paramMap.get(key) instanceof String) {
                    name = (String) paramMap.get(key);
                }
            }
        }
        log.info("DebugUtils getOldWindowName name: " + name);
        return name;
    }

    /**
     * change param type
     *
     * @param webSocketServer webSocketServer
     * @param windowName      windowName
     * @param key             key
     * @param <T>             <T>
     * @return T
     */
    public static <T> T changeParamType(WebSocketServer webSocketServer, String windowName, String key) {
        Map<String, Object> paramMap = webSocketServer.getParamMap(windowName);
        return (T) paramMap.get(key);
    }

    /**
     * change param type
     *
     * @param obj obj
     * @return T
     */
    public static <T> T changeParamType(Object obj) {
        PublicParamQuery paramReq = new PublicParamQuery();
        if (obj instanceof PublicParamQuery) {
            paramReq = (PublicParamQuery) obj;
        }
        return (T) paramReq;
    }

    /**
     * change type to integer
     *
     * @param obj obj
     * @return Integer
     */
    public static Integer changeTypeToInteger(Object obj) {
        int count = 0;
        if (obj instanceof Long) {
            count = Math.toIntExact((Long) obj);
        } else if (obj instanceof BigDecimal) {
            BigDecimal bigDecimal = (BigDecimal) obj;
            count = bigDecimal.intValue();
        }
        return count;
    }

    /**
     * contains sql injection
     *
     * @param value value
     * @return String
     */
    public static String containsSqlInjection(String value) {
        Pattern pattern = Pattern.compile(
                "(?:')|(?:--)|(/\\*(?:.|[\\n\\r])*?\\*/)|(\\b(select|update|and|or|delete|insert|trancate"
                        + "|char|substr|ascii|declare|exec|count|master|into|drop|execute)\\b|(\\*|;|\\+|'|%))");
        Matcher matcher = pattern.matcher(value);
        if (matcher.find()) {
            throw new CustomException(LocaleStringUtils.transLanguage("2018"));
        } else {
            return value;
        }
    }

    /**
     * get message
     *
     * @return String
     */
    public static String getMessage() {
        return LocaleStringUtils.transLanguage("2016");
    }

    /**
     * check name
     *
     * @param name name
     * @return String
     */
    public static String needQuoteName(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        String regex = "^([a-z_][a-z|0-9|_|$]*)|([a-z_][a-z|0-9|_|,| |.|$]*)$";
        if (name.matches(regex)) {
            return name;
        }

        StringBuilder sb = new StringBuilder(2 + name.length() * 11 / 10);
        sb.append('"');

        for (int index = 0; index < name.length(); ++index) {
            char ch = name.charAt(index);
            if (ch == '\0') {
                throw new CustomException(LocaleStringUtils.transLanguage("2020"));
            }
            if (ch == '"') {
                sb.append(ch);
            }
            sb.append(ch);
        }

        sb.append('"');
        return sb.toString();
    }

    /**
     * change param type
     *
     * @param value          value
     * @param columnType     columnType
     * @param columnTypeName columnTypeName
     * @return String
     */
    public static String typeChange(String value, int columnType, String columnTypeName) {
        String newValue;
        switch (columnType) {
            case Types.NULL: {
                newValue = null;
                break;
            }
            case Types.BIGINT:
            case Types.TINYINT:
            case Types.NUMERIC:
            case Types.DECIMAL:
            case Types.INTEGER:
            case Types.SMALLINT:
            case Types.FLOAT:
            case Types.REAL: {
                newValue = value;
                break;
            }
            case Types.DOUBLE: {
                if ("money".equalsIgnoreCase(columnTypeName)) {
                    newValue = QUOTES + value + QUOTES;
                } else {
                    newValue = value;
                }
                break;
            }
            case Types.BIT: {
                newValue = getBitString(value, columnTypeName);
                break;
            }
            default: {
                if (StringUtils.isEmpty(value)) {
                    newValue = value;
                } else {
                    newValue = QUOTES + value + QUOTES;
                }
                break;
            }
        }
        return newValue;
    }

    private static String getBitString(String value, String columnTypeName) {
        String newValue;
        if ("bool".equalsIgnoreCase(columnTypeName)) {
            Boolean isBoolValue = BooleanUtils.toBooleanObject(value);
            if (isBoolValue != null) {
                newValue = isBoolValue.toString();
            } else {
                newValue = value;
            }
        } else {
            if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                if ("true".equalsIgnoreCase(value)) {
                    newValue = QUOTES + "1" + QUOTES;
                } else {
                    newValue = QUOTES + "0" + QUOTES;
                }
            } else {
                newValue = QUOTES + value + QUOTES;
            }
        }
        return newValue;
    }

    /**
     * list to string
     *
     * @param list list
     * @param str str
     * @return String
     */
    public static String listToString(List list, String str) {
        log.info("DebugUtils listToString str: " + str);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i != list.size() - 1) {
                sb.append(str);
            }
        }

        log.info("DebugUtils listToString sb: " + sb);
        return sb.toString();
    }

    /**
     * get uuid
     *
     * @param uuid uuid
     * @return String
     */
    public static String comGetUuidType(String uuid) {
        log.info("DebugUtils listToString str: " + uuid);
        if (!conMap.containsKey(uuid)) {
            throw new CustomException(LocaleStringUtils.transLanguage("1004"));
        }
        return conMap.get(uuid).getType();
    }


    /**
     * get ddl
     *
     * @param request request
     * @return StringBuilder
     */
    public static List<String> cutBrace(String request) {
        List<String> strList = new ArrayList<>();
        if (org.opengauss.admin.common.utils.StringUtils.isNotEmpty(request)) {
            strList = List.of(request.replaceAll("\\{|\\}", "").split(","));
        }
        return strList;
    }
}
