package com.nctigba.datastudio.service.impl.debug;

import com.alibaba.fastjson.JSON;
import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.enums.ParamTypeEnum;
import com.nctigba.datastudio.model.PublicParamReq;
import com.nctigba.datastudio.model.entity.OperateStatusDO;
import com.nctigba.datastudio.service.OperationInterface;
import com.nctigba.datastudio.util.DebugUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
        ResultSet funcResult = null;
        Map<String, Object> paramMap = new HashMap<>();
        try {
            if (connection == null) {
                connection = webSocketServer.createConnection(paramReq.getConnectionName(), paramReq.getWebUser());
            }
            Statement statement = connection.createStatement();
            webSocketServer.setStatement(windowName, statement);
            webSocketServer.setStatement(windowName, statement);

            funcResult = statement.executeQuery(DebugUtils.getFuncSql(windowName, name, webSocketServer));
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
        } catch (Exception e) {
            stopDebug.operate(webSocketServer, paramReq);
        }
        if (CollectionUtils.isEmpty(paramMap)) {
            webSocketServer.sendMessage(windowName, ignoreWindow, "500", "function/procedure doesn't exist!", null);
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
        String sql = "";
        if ("f".equals(proKind)) {
            sql = parseFunctionSql(windowName, map, webSocketServer, schema);
        } else if ("p".equals(proKind)) {
            sql = parseProcedureSql(map, schema);
        }

        return sql;
    }

    public String parseProcedureSql(Map<String, Object> map, String schema) throws SQLException {
        StringBuilder sb = new StringBuilder();
        String proName = (String) map.get(PRO_NAME);
        String proSrc = (String) map.get(PRO_SRC);
        String proArgSrc = (String) map.get(PRO_ARG_SRC);

        sb.append("CREATE OR REPLACE PROCEDURE " + schema + POINT + proName + PARENTHESES_LEFT);
        if (!StringUtils.isEmpty(proArgSrc)) {
            sb.append(proArgSrc);
        }
        sb.append(")\nAS" + proSrc + ";\n/\n/");

        log.info("funcProcedure parseProcedureSql is: " + sb);
        return sb.toString();
    }

    private String parseFunctionSql(String windowName, Map<String, Object> map, WebSocketServer webSocketServer, String schema) throws SQLException {
        StringBuilder sb = new StringBuilder();
        String proArgModes = (String) map.get(PRO_ARG_MODES);
        String proArgNames = (String) map.get(PRO_ARG_NAMES);

        sb.append("CREATE OR REPLACE FUNCTION " + schema + "." + map.get(PRO_NAME) + PARENTHESES_LEFT);
        if (!StringUtils.isEmpty(proArgModes)) {
            String proAllArgTypes = (String) map.get(PRO_ALL_ARG_TYPES);
            String[] argModes = proArgModes.substring(1, proArgModes.length() - 1).split(COMMA);
            String[] argNames = proArgNames.substring(1, proArgNames.length() - 1).split(COMMA);
            String[] argTypes = proAllArgTypes.substring(1, proAllArgTypes.length() - 1).split(COMMA);
            for (int i = 0; i < argModes.length; i++) {
                ResultSet typeNameResult = webSocketServer.getStatement(windowName).executeQuery(GET_OID_NAME_SQL + argTypes[i] + SEMICOLON);
                while (typeNameResult.next()) {
                    if ("o".equals(argModes[i].trim())) {
                        sb.append("OUT ");
                    } else if ("b".equals(argModes[i].trim())) {
                        sb.append("INOUT ");
                    } else if ("v".equals(argModes[i].trim())) {
                        sb.append("VARIADIC ");
                    }
                    sb.append(argNames[i] + SPACE + ParamTypeEnum.parseType(typeNameResult.getString(TYP_NAME)));
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
                            sb.append(argNames[i] + SPACE + ParamTypeEnum.parseType(typeNameResult.getString(TYP_NAME)));
                        }

                        if (i != argTypes.length - 1) {
                            sb.append(COMMA_SPACE);
                        }
                    }
                }
            }
        }

        sb.append(")\n RETURNS ");
        if ((boolean) map.get(PRO_RET_SET)) {
            sb.append("SETOF ");
        }
        String lanName = (String) map.get(LAN_NAME);
        sb.append(ParamTypeEnum.parseType((String) map.get(TYP_NAME)) + "\n LANGUAGE " + lanName + "\n ");
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
            sb.append(" COST " + proCost);
        }
        if ("plpgsql".equals(lanName) && proRows != 1000 && proRows != 0) {
            sb.append(" ROWS " + proRows);
        }
        sb.append("\nAS ");
        String proBin = (String) map.get(PRO_BIN);
        if (!StringUtils.isEmpty(proBin)) {
            sb.append("'" + proBin + "', ");
        }
        sb.append("$$" + map.get(PRO_SRC) + "$$;\n/");

        log.info("funcProcedure parseFunctionSql is: " + sb);
        return sb.toString();
    }

    @Override
    public Object formatJson(String str) {
        return JSON.parseObject(str, PublicParamReq.class);
    }
}
