/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service.impl.debug;

import com.alibaba.fastjson.JSON;
import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.model.PublicParamReq;
import com.nctigba.datastudio.service.OperationInterface;
import com.nctigba.datastudio.util.DebugUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.DIFFER;
import static com.nctigba.datastudio.constants.CommonConstants.FUNC_OID;
import static com.nctigba.datastudio.constants.CommonConstants.LINE_NO;
import static com.nctigba.datastudio.constants.CommonConstants.OID;
import static com.nctigba.datastudio.constants.CommonConstants.RESULT;
import static com.nctigba.datastudio.constants.CommonConstants.STATEMENT;
import static com.nctigba.datastudio.constants.CommonConstants.SUCCESS;
import static com.nctigba.datastudio.constants.CommonConstants.TYPE;
import static com.nctigba.datastudio.constants.SqlConstants.BACKTRACE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.BACKTRACE_SQL_PRE;
import static com.nctigba.datastudio.constants.SqlConstants.INFO_LOCALS_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.NEXT_SQL;
import static com.nctigba.datastudio.enums.MessageEnum.CLOSE_WINDOW;
import static com.nctigba.datastudio.enums.MessageEnum.STACK;
import static com.nctigba.datastudio.enums.MessageEnum.SWITCH_WINDOW;
import static com.nctigba.datastudio.enums.MessageEnum.VARIABLE;

/**
 * SingleStepImpl
 *
 * @since 2023-6-26
 */
@Slf4j
@Service("singleStep")
public class SingleStepImpl implements OperationInterface {
    @Override
    public void operate(WebSocketServer webSocketServer, Object obj) throws SQLException, IOException {
        PublicParamReq paramReq = DebugUtils.changeParamType(obj);
        log.info("singleStep paramReq: " + paramReq);

        String rootWindowName = paramReq.getRootWindowName();
        Statement stat = DebugUtils.changeParamType(webSocketServer, rootWindowName, STATEMENT);
        if (stat == null) {
            return;
        }

        int lineNo = -1;
        ResultSet resultSet = stat.executeQuery(NEXT_SQL);
        if (resultSet.next()) {
            lineNo = resultSet.getInt(LINE_NO);
        }

        String windowName = paramReq.getWindowName();
        String type = DebugUtils.changeParamType(webSocketServer, windowName, TYPE);
        String oid = DebugUtils.changeParamType(webSocketServer, windowName, OID);
        List<String> oidList = DebugUtils.getOidList(webSocketServer, rootWindowName);
        if (!CollectionUtils.isEmpty(oidList)) {
            String newOid = Strings.EMPTY;
            if ("f".equals(type) && lineNo == 0) {
                ResultSet result = stat.executeQuery(NEXT_SQL);
                if (result.next()) {
                    newOid = result.getString(FUNC_OID);
                }
                oidList = DebugUtils.getOidList(webSocketServer, rootWindowName);
            }
            if (oidList.contains(oid) && !oidList.get(0).equals(oid)) {
                Map<String, String> map = new HashMap<>();
                map.put(RESULT, oidList.get(0));
                webSocketServer.sendMessage(windowName, SWITCH_WINDOW, SUCCESS, map);
                String name = DebugUtils.getOldWindowName(webSocketServer, rootWindowName, newOid);
                DebugUtils.disableButton(webSocketServer, windowName);
                DebugUtils.enableButton(webSocketServer, name);
                paramReq.setCloseWindow(true);
                paramReq.setOldWindowName(name);
            }
            if (!oidList.contains(oid)) {
                webSocketServer.sendMessage(windowName, CLOSE_WINDOW, SUCCESS, null);
                DebugUtils.enableButton(webSocketServer, paramReq.getOldWindowName());
                paramReq.setCloseWindow(true);
                Map<String, Object> paramMap = webSocketServer.getParamMap(rootWindowName);
                paramMap.keySet().removeIf(oid::equals);
            }
        }
        showDebugInfo(webSocketServer, stat, paramReq);
    }

    /**
     * show debug info
     *
     * @param webSocketServer webSocketServer
     * @param statement statement
     * @param paramReq paramReq
     * @throws IOException IOException
     * @throws SQLException SQLException
     */
    public void showDebugInfo(
            WebSocketServer webSocketServer, Statement statement,
            PublicParamReq paramReq) throws IOException, SQLException {
        log.info("singleStep showDebugInfo paramReq is: " + paramReq);

        String name = paramReq.getWindowName();
        if (paramReq.isCloseWindow()) {
            name = paramReq.getOldWindowName();
        }
        int differ = DebugUtils.changeParamType(webSocketServer, name, DIFFER);
        log.info("singleStep showDebugInfo differ is: " + differ);
        ResultSet stackResult = statement.executeQuery(BACKTRACE_SQL_PRE + differ + BACKTRACE_SQL);
        webSocketServer.sendMessage(name, STACK, SUCCESS, DebugUtils.parseResultSet(stackResult));
        Map<String, Object> variableMap = DebugUtils.parseVariable(statement.executeQuery(INFO_LOCALS_SQL));
        Map<String, Object> paramMap = DebugUtils.addMapParam(variableMap, webSocketServer, paramReq);
        webSocketServer.sendMessage(name, VARIABLE, SUCCESS, paramMap);
    }

    @Override
    public Object formatJson(String str) {
        return JSON.parseObject(str, PublicParamReq.class);
    }
}
