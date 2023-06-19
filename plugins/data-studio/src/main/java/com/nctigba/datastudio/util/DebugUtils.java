/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.util;

import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.enums.ParamTypeEnum;
import com.nctigba.datastudio.model.PublicParamReq;
import com.nctigba.datastudio.model.entity.OperateStatusDO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
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
import static com.nctigba.datastudio.constants.CommonConstants.T;
import static com.nctigba.datastudio.constants.CommonConstants.TYPE;
import static com.nctigba.datastudio.constants.CommonConstants.TYP_NAME;
import static com.nctigba.datastudio.constants.CommonConstants.VARIADIC;
import static com.nctigba.datastudio.constants.SqlConstants.BACKTRACE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.BACKTRACE_SQL_PRE;
import static com.nctigba.datastudio.constants.SqlConstants.COMMA;
import static com.nctigba.datastudio.constants.SqlConstants.COMMA_SPACE;
import static com.nctigba.datastudio.constants.SqlConstants.FUNCTION_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.GET_OID_NAME_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.INFO_CODE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.LEFT_BRACKET;
import static com.nctigba.datastudio.constants.SqlConstants.POINT;
import static com.nctigba.datastudio.constants.SqlConstants.PROC_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.RIGHT_BRACKET;

@Slf4j
public class DebugUtils {
    public static final String TRANS_POINT = "\\.";

    public static String getSchemaBySql(String sql) {
        String substring = sql.substring(0, sql.indexOf(LEFT_BRACKET));
        log.info("DebugUtils getSchemaBySql substring: " + substring);
        String[] split = substring.trim().split(SPACE);

        String fullName = split[split.length - 1];
        log.info("DebugUtils getSchemaBySql fullName: " + fullName);
        if (!fullName.contains(POINT)) {
            return null;
        }
        return fullName.split(TRANS_POINT)[0];
    }

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

    public static List<Map<String, Object>> getParamMap(WebSocketServer webSocketServer, String windowName,
                                                        String oid) throws SQLException {
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
        } catch (Exception e) {
            log.info(e.toString());
            throw new RuntimeException(e);
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
        } catch (Exception e) {
            log.info(e.toString());
            throw new RuntimeException(e);
        }
    }

    public static List<String> getOidList(WebSocketServer webSocketServer, String rootWindowName) throws SQLException {
        log.info("DebugUtils getOidList rootWindowName: " + rootWindowName);
        List<String> list = new ArrayList<>();

        try {
            Statement statement = (Statement) webSocketServer.getParamMap(rootWindowName).get(STATEMENT);
            ResultSet stackResult = statement.executeQuery(BACKTRACE_SQL_PRE + 0 + BACKTRACE_SQL);
            while (stackResult.next()) {
                list.add(stackResult.getString(FUNC_OID));
            }
        } catch (Exception e) {
            log.info(e.toString());
            return new ArrayList<>();
        }

        log.info("DebugUtils getOidList list: " + list);
        return list;
    }

    public static Map<String, Object> getParamType(WebSocketServer webSocketServer, PublicParamReq paramReq) {
        log.info("DebugUtils getParamType paramReq: " + paramReq);
        Map<String, Object> map = new HashMap<>();
        try (
                Connection connection = webSocketServer.createConnection(paramReq.getUuid(), paramReq.getWindowName());
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(String.format(PROC_SQL, paramReq.getOid()))
        ) {
            while (resultSet.next()) {
                String argName = resultSet.getString(PRO_ARG_NAMES);
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
        } catch (Exception e) {
            return map;
        }
        log.info("DebugUtils getParamType map: " + map);
        return map;
    }

    public static String prepareSql(PublicParamReq paramReq) {
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

    public static String getFullName(String sql) {
        String[] split = sql.split("\\(");
        String[] s = split[0].trim().split(SPACE);
        log.info("DebugUtils getFullName s: " + Arrays.toString(s));
        return s[s.length - 1];
    }

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
        log.info("DebugUtils parseResultSet map: " + map);
        return map;
    }

    public static Map<String, Object> parseResultSet(ResultSet resultSet, Integer size,
                                                     Integer pageNum) throws SQLException {
        log.info("ResultSet map: " + resultSet);
        log.info("size map: " + size);
        log.info("pageNum map: " + pageNum);
        Map<String, Object> map = new HashMap<>();
        List<String> columnList = getColumnList(resultSet.getMetaData(), map);
        List<List<Object>> dataList = new ArrayList<>();
        Integer i = 0;
        if (pageNum != 1) {
            resultSet.absolute(size * pageNum - 1);
        } else {
            resultSet.absolute(0);
        }
        boolean a = i < size;
        while (resultSet.next() && i < size) {
            List<Object> list = new ArrayList<>();
            for (String column : columnList) {
                list.add(resultSet.getObject(column));
            }
            i++;
            dataList.add(list);
        }
        map.put(RESULT, dataList);
        log.info("DebugUtils parseResultSet map: " + map);
        return map;
    }

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

    private static List<String> getColumnList(ResultSetMetaData metaData, Map<String, Object> map) throws SQLException {
        List<String> columnList = new ArrayList<>();
        for (int i = 0; i < metaData.getColumnCount(); i++) {
            columnList.add(metaData.getColumnName(i + 1));
        }
        map.put(COLUMN, columnList);
        log.info("DebugUtils getColumnList: " + columnList);
        return columnList;
    }

    public static Map<String, Object> addMapParam(Map<String, Object> map, WebSocketServer webSocketServer,
                                                  PublicParamReq paramReq) {
        log.info("DebugUtils addMapParam map: " + map);
        List<String> columnList = (List<String>) map.get(COLUMN);
        List<List<Object>> dataList = (List<List<Object>>) map.get(RESULT);

        Map<String, Object> paramList = getParamType(webSocketServer, paramReq);
        log.info("DebugUtils addMapParam paramList: " + paramList);
        if (!CollectionUtils.isEmpty(map)) {
            columnList.add("paramType");
            for (int i = 0; i < dataList.size(); i++) {
                List<Object> data = dataList.get(i);
                if (!CollectionUtils.isEmpty(paramList)) {
                    if (i < paramList.size()) {
                        String value = (String) paramList.get(data.get(0));
                        data.add(value);
                    } else {
                        data.add("temp");
                    }
                } else {
                    data.add("temp");
                }
            }
        }
        log.info("DebugUtils addMapParam map: " + map);
        return map;
    }

    public static String sqlHandleAfter(String sql) {
        sql = sql.replace(FUNCTION_DOLLAR, DOLLAR);
        return sql + SLASH;
    }

    public static void disableButton(WebSocketServer webSocketServer, String windowName) {
        OperateStatusDO operateStatus = webSocketServer.getOperateStatus(windowName);
        operateStatus.subAllFalse();
        webSocketServer.setOperateStatus(windowName, operateStatus);
    }

    public static void enableButton(WebSocketServer webSocketServer, String windowName) {
        OperateStatusDO operateStatus = webSocketServer.getOperateStatus(windowName);
        operateStatus.subAllTrue();
        webSocketServer.setOperateStatus(windowName, operateStatus);
    }

    public static List<Integer> getAvailableBreakPoints(PublicParamReq paramReq, WebSocketServer webSocketServer) {
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
                if (T.equals(canBreak)) {
                    list.add(Integer.valueOf(lineNo));
                }
            }

            String windowName = paramReq.getWindowName();
            webSocketServer.setParamMap(windowName, CAN_BREAK, list);
            webSocketServer.setParamMap(windowName, DIFFER, differ);
            log.info("DebugUtils getAvailableBreakPoints differ: " + differ);
            log.info("DebugUtils getAvailableBreakPoints list: " + list);
        } catch (Exception e) {
            return list;
        }
        return list;
    }

    public static Map<String, Map<String, String>> getResultMap(WebSocketServer webSocketServer,
                                                                PublicParamReq paramReq) {
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
                } catch (Exception e) {
                    log.info(e.toString());
                    throw new RuntimeException(e);
                }
            }

            sb.append(RIGHT_BRACKET);
            log.info("DebugUtils sb: " + sb);
            paramMap.put(NAME, sb.toString());
        } catch (Exception e) {
            return null;
        }

        Map<String, Map<String, String>> map = new HashMap<>();
        map.put(RESULT, paramMap);
        log.info("DebugUtils map: " + map);
        return map;
    }

    public static void exportFile(String fileName, String content, HttpServletResponse response) throws Exception {
        response.reset();
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", URLEncoder.encode(fileName, StandardCharsets.UTF_8));
        response.addHeader("Response-Type", "blob");
        response.setCharacterEncoding("UTF-8");

        try (
                InputStream inputStream = new ByteArrayInputStream(content.getBytes());
                OutputStream outputStream = response.getOutputStream()
        ) {
            byte[] b = new byte[1024];
            int len;
            while ((len = inputStream.read(b)) > 0) {
                outputStream.write(b, 0, len);
            }
            outputStream.flush();
        }
    }

    public static String getOldWindowName(WebSocketServer webSocketServer, String rootWindowName, String oid) {
        String name = Strings.EMPTY;
        Map<String, Object> paramMap = webSocketServer.getParamMap(rootWindowName);
        log.info("DebugUtils getOldWindowName paramMap: " + paramMap);
        for (String key : paramMap.keySet()) {
            if (key.equals(oid)) {
                name = (String) paramMap.get(key);
            }
        }
        log.info("DebugUtils getOldWindowName name: " + name);
        return name;
    }

    public static String containsSqlInjection(String value) {
        Pattern pattern = Pattern.compile(
                "(?:')|(?:--)|(/\\*(?:.|[\\n\\r])*?\\*/)|(\\b(select|update|and|or|delete"
                        + "|insert|trancate|char|substr|ascii|declare|exec|count|master|into|drop|execute)\\b|(\\*|;|\\+|'|%))");
        Matcher matcher = pattern.matcher(value);
        if(matcher.find()){
            throw new CustomException(LocaleString.transLanguage("2018"));
        }else {
            return  value;
        }
    }
    public static String getMessage() {
        return LocaleString.transLanguage("2016");
    }
}
