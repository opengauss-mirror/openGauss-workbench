package com.nctigba.datastudio.service.impl.debug;

import com.alibaba.fastjson.JSON;
import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.enums.ParamTypeEnum;
import com.nctigba.datastudio.model.PublicParamReq;
import com.nctigba.datastudio.model.entity.OperateStatusDO;
import com.nctigba.datastudio.service.OperationInterface;
import com.nctigba.datastudio.util.DebugUtils;
import com.nctigba.datastudio.util.LocaleString;
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
import java.util.HashMap;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.FEN_CED_MODE;
import static com.nctigba.datastudio.constants.CommonConstants.FIVE_HUNDRED;
import static com.nctigba.datastudio.constants.CommonConstants.LAN_NAME;
import static com.nctigba.datastudio.constants.CommonConstants.PRO_ALL_ARG_TYPES;
import static com.nctigba.datastudio.constants.CommonConstants.PRO_ARG_MODES;
import static com.nctigba.datastudio.constants.CommonConstants.PRO_ARG_NAMES;
import static com.nctigba.datastudio.constants.CommonConstants.PRO_ARG_SRC;
import static com.nctigba.datastudio.constants.CommonConstants.PRO_ARG_TYPES;
import static com.nctigba.datastudio.constants.CommonConstants.PRO_BIN;
import static com.nctigba.datastudio.constants.CommonConstants.PRO_COST;
import static com.nctigba.datastudio.constants.CommonConstants.PRO_IS_STRICT;
import static com.nctigba.datastudio.constants.CommonConstants.PRO_KIND;
import static com.nctigba.datastudio.constants.CommonConstants.PRO_NAME;
import static com.nctigba.datastudio.constants.CommonConstants.PRO_RET_SET;
import static com.nctigba.datastudio.constants.CommonConstants.PRO_ROWS;
import static com.nctigba.datastudio.constants.CommonConstants.PRO_SHIPPABLE;
import static com.nctigba.datastudio.constants.CommonConstants.PRO_SRC;
import static com.nctigba.datastudio.constants.CommonConstants.PRO_VOLATILE;
import static com.nctigba.datastudio.constants.CommonConstants.RESULT;
import static com.nctigba.datastudio.constants.CommonConstants.SPACE;
import static com.nctigba.datastudio.constants.CommonConstants.SUCCESS;
import static com.nctigba.datastudio.constants.CommonConstants.TYPE;
import static com.nctigba.datastudio.constants.CommonConstants.TYP_NAME;
import static com.nctigba.datastudio.constants.SqlConstants.COMMA;
import static com.nctigba.datastudio.constants.SqlConstants.COMMA_SPACE;
import static com.nctigba.datastudio.constants.SqlConstants.GET_OID_NAME_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.PARENTHESES_LEFT;
import static com.nctigba.datastudio.constants.SqlConstants.POINT;
import static com.nctigba.datastudio.constants.SqlConstants.SEMICOLON;
import static com.nctigba.datastudio.enums.MessageEnum.ignoreWindow;
import static com.nctigba.datastudio.enums.MessageEnum.view;

/**
 * show function/procedure
 */
@Slf4j
@Service("funcProcedure")
public class FuncProcedureImpl implements OperationInterface {
    @Autowired
    private StopDebugImpl stopDebug;

    @Override
    public void operate(WebSocketServer webSocketServer, Object obj) throws Exception {
        log.info("funcProcedure obj is: " + obj);
        PublicParamReq paramReq = (PublicParamReq) obj;
        String windowName = paramReq.getWindowName();
        String name = paramReq.getFullName();
        String schema = paramReq.getSchema();

        OperateStatusDO operateStatus = webSocketServer.getOperateStatus(windowName);
        operateStatus.enableStartDebug();
        webSocketServer.setOperateStatus(windowName, operateStatus);
        Connection connection = webSocketServer.getConnection(windowName);
        if (connection == null) {
            connection = webSocketServer.createConnection(paramReq.getUuid(), windowName);
            webSocketServer.setConnection(windowName, connection);
        }
        Statement statement = connection.createStatement();
        webSocketServer.setStatement(windowName, statement);
        webSocketServer.setStatement(windowName, statement);

        ResultSet funcResult = statement.executeQuery(DebugUtils.getFuncSql(windowName, schema, name, webSocketServer));
        Map<String, Object> paramMap = new HashMap<>();
        while (funcResult.next()) {
            paramMap.put(PRO_NAME, funcResult.getString(PRO_NAME));
            paramMap.put(LAN_NAME, funcResult.getString(LAN_NAME));
            paramMap.put(TYP_NAME, funcResult.getString(TYP_NAME));
            paramMap.put(PRO_ARG_TYPES, funcResult.getString(PRO_ARG_TYPES));
            paramMap.put(PRO_ALL_ARG_TYPES, funcResult.getString(PRO_ALL_ARG_TYPES));
            paramMap.put(PRO_ARG_MODES, funcResult.getString(PRO_ARG_MODES));
            paramMap.put(PRO_ARG_NAMES, funcResult.getString(PRO_ARG_NAMES));
            paramMap.put(PRO_SRC, funcResult.getString(PRO_SRC));
            paramMap.put(PRO_BIN, funcResult.getString(PRO_BIN));
            paramMap.put(PRO_ARG_SRC, funcResult.getString(PRO_ARG_SRC));
            paramMap.put(PRO_KIND, funcResult.getString(PRO_KIND));
            paramMap.put(PRO_VOLATILE, funcResult.getString(PRO_VOLATILE));
            paramMap.put(PRO_COST, funcResult.getInt(PRO_COST));
            paramMap.put(PRO_ROWS, funcResult.getInt(PRO_ROWS));
            paramMap.put(FEN_CED_MODE, funcResult.getBoolean(FEN_CED_MODE));
            paramMap.put(PRO_SHIPPABLE, funcResult.getBoolean(PRO_SHIPPABLE));
            paramMap.put(PRO_IS_STRICT, funcResult.getBoolean(PRO_IS_STRICT));
            paramMap.put(PRO_RET_SET, funcResult.getBoolean(PRO_RET_SET));
        }
        log.info("funcProcedure paramMap is: " + paramMap);
        if (CollectionUtils.isEmpty(paramMap)) {
            webSocketServer.sendMessage(windowName, ignoreWindow, FIVE_HUNDRED,
                    LocaleString.transLanguageWs("1006", webSocketServer), null);
            return;
        }

        String sql = parseResult(windowName, paramMap, webSocketServer, schema);
        log.info("funcProcedure sql is: " + sql);
        Map<String, Object> map = new HashMap<>();
        map.put(RESULT, sql);
        webSocketServer.sendMessage(windowName, view, SUCCESS, map);
    }

    public String parseResult(String windowName, Map<String, Object> map, WebSocketServer webSocketServer, String schema) throws SQLException {
        String proKind = (String) map.get(PRO_KIND);
        String proName = (String) map.get(PRO_NAME);
        String proSrc = (String) map.get(PRO_SRC);
        StringBuilder sb = new StringBuilder();
        webSocketServer.setParamMap(windowName, TYPE, proKind);
        if ("p".equals(proKind)) {
            sb.append("CREATE OR REPLACE PROCEDURE ").append(schema).append(POINT).append(proName).append(PARENTHESES_LEFT);
            sb.append(parseSql(windowName, map, webSocketServer));
            sb.append(")\nAS").append(proSrc).append(";\n/\n/");
        } else if ("f".equals(proKind)) {
            sb.append("CREATE OR REPLACE FUNCTION ").append(schema).append(".").append(map.get(PRO_NAME)).append(PARENTHESES_LEFT);
            sb.append(parseSql(windowName, map, webSocketServer));
            sb.append(")\n RETURNS ");
            if ((boolean) map.get(PRO_RET_SET)) {
                sb.append("SETOF ");
            }
            String lanName = (String) map.get(LAN_NAME);
            sb.append(ParamTypeEnum.parseType((String) map.get(TYP_NAME))).append("\n LANGUAGE ").append(lanName).append("\n ");
            String proVolatile = (String) map.get(PRO_VOLATILE);
            if ("i".equals(proVolatile)) {
                sb.append("IMMUTABLE ");
            } else if ("s".equals(proVolatile)) {
                sb.append("STABLE ");
            }
            if ((boolean) map.get(PRO_IS_STRICT)) {
                sb.append("STRICT ");
            }
            if (!(boolean) map.get(FEN_CED_MODE)) {
                sb.append("NOT ");
            }
            sb.append("FENCED ");
            if (!(boolean) map.get(PRO_SHIPPABLE)) {
                sb.append("NOT ");
            }
            sb.append("SHIPPABLE");

            int proCost = (int) map.get(PRO_COST);
            int proRows = (int) map.get(PRO_ROWS);
            if ("plpgsql".equals(lanName) && proCost != 100) {
                sb.append(" COST ").append(proCost);
            }
            if ("plpgsql".equals(lanName) && proRows != 1000 && proRows != 0) {
                sb.append(" ROWS ").append(proRows);
            }
            sb.append("\nAS ");
            String proBin = (String) map.get(PRO_BIN);
            if (!StringUtils.isEmpty(proBin)) {
                sb.append("'").append(proBin).append("', ");
            }
            sb.append("$$").append(map.get(PRO_SRC)).append("$$;\n/");
        }

        log.info("funcProcedure parseResult is: " + sb);
        return sb.toString();
    }

    public String parseSql(String windowName, Map<String, Object> map, WebSocketServer webSocketServer) throws SQLException {
        StringBuilder sb = new StringBuilder();
        String proArgModes = (String) map.get(PRO_ARG_MODES);
        String proArgNames = (String) map.get(PRO_ARG_NAMES);
        if (!StringUtils.isEmpty(proArgModes)) {
            String proAllArgTypes = (String) map.get(PRO_ALL_ARG_TYPES);
            String[] argModes = proArgModes.substring(1, proArgModes.length() - 1).split(COMMA);
            String[] argNames = proArgNames.substring(1, proArgNames.length() - 1).split(COMMA);
            String[] argTypes = proAllArgTypes.substring(1, proAllArgTypes.length() - 1).split(COMMA);
            for (int i = 0; i < argModes.length; i++) {
                ResultSet typeNameResult = webSocketServer.getStatement(windowName).executeQuery(GET_OID_NAME_SQL + argTypes[i] + SEMICOLON);
                while (typeNameResult.next()) {
                    switch (argModes[i].trim()) {
                        case "o":
                            sb.append("OUT ");
                            break;
                        case "b":
                            sb.append("INOUT ");
                            break;
                        case "v":
                            sb.append("VARIADIC ");
                            break;
                    }
                    sb.append(argNames[i]).append(SPACE).append(ParamTypeEnum.parseType(typeNameResult.getString(TYP_NAME)));
                }
                if (i != argModes.length - 1) {
                    sb.append(COMMA_SPACE);
                }
            }
        } else {
            String proArgTypes = (String) map.get(PRO_ARG_TYPES);
            if (!StringUtils.isEmpty(proArgTypes)) {
                String[] argTypes = proArgTypes.split(SPACE);
                if (StringUtils.isEmpty(proArgNames)) {
                    for (int i = 0; i < argTypes.length; i++) {
                        ResultSet typeNameResult = webSocketServer.getStatement(windowName).executeQuery(GET_OID_NAME_SQL + argTypes[i] + SEMICOLON);
                        while (typeNameResult.next()) {
                            sb.append(ParamTypeEnum.parseType(typeNameResult.getString(TYP_NAME)));
                        }

                        if (i != argTypes.length - 1) {
                            sb.append(COMMA_SPACE);
                        }
                    }
                } else {
                    String[] argNames = proArgNames.substring(1, proArgNames.length() - 1).split(COMMA);
                    for (int i = 0; i < argTypes.length; i++) {
                        ResultSet typeNameResult = webSocketServer.getStatement(windowName).executeQuery(GET_OID_NAME_SQL + argTypes[i] + SEMICOLON);
                        while (typeNameResult.next()) {
                            sb.append(argNames[i]).append(SPACE).append(ParamTypeEnum.parseType(typeNameResult.getString(TYP_NAME)));
                        }

                        if (i != argTypes.length - 1) {
                            sb.append(COMMA_SPACE);
                        }
                    }
                }
            }
        }

        log.info("funcProcedure parseSql is: " + sb);
        return sb.toString();
    }

    @Override
    public Object formatJson(String str) {
        return JSON.parseObject(str, PublicParamReq.class);
    }
}
