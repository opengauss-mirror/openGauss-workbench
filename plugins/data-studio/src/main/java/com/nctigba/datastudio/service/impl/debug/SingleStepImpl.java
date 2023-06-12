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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.sql.ResultSet;
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
import static com.nctigba.datastudio.enums.MessageEnum.closeWindow;
import static com.nctigba.datastudio.enums.MessageEnum.stack;
import static com.nctigba.datastudio.enums.MessageEnum.switchWindow;
import static com.nctigba.datastudio.enums.MessageEnum.variable;

/**
 * single step
 */
@Slf4j
@Service("singleStep")
public class SingleStepImpl implements OperationInterface {
    @Autowired
    private StepOutImpl stepOut;

    @Override
    public void operate(WebSocketServer webSocketServer, Object obj) throws Exception {
        PublicParamReq paramReq = (PublicParamReq) obj;
        log.info("singleStep paramReq: " + paramReq);
        String rootWindowName = paramReq.getRootWindowName();
        String oldWindowName = paramReq.getOldWindowName();
        String windowName = paramReq.getWindowName();

        Statement stat = (Statement) webSocketServer.getParamMap(rootWindowName).get(STATEMENT);
        if (stat == null) {
            return;
        }
        int lineNo = -1;
        try (
                ResultSet resultSet = stat.executeQuery(NEXT_SQL)
        ) {
            if (resultSet.next()) {
                lineNo = resultSet.getInt(LINE_NO);
            }
        }

        String type = (String) webSocketServer.getParamMap(windowName).get(TYPE);
        String oid = (String) webSocketServer.getParamMap(windowName).get(OID);
        List<String> oidList = DebugUtils.getOidList(webSocketServer, rootWindowName);
        if (!CollectionUtils.isEmpty(oidList)) {
            String newOid = Strings.EMPTY;
            if ("f".equals(type) && lineNo == 0) {
                ResultSet resultSet = stat.executeQuery(NEXT_SQL);
                if (resultSet.next()) {
                    newOid = resultSet.getString(FUNC_OID);
                }
                oidList = DebugUtils.getOidList(webSocketServer, rootWindowName);
            }
            if (oidList.contains(oid) && !oidList.get(0).equals(oid)) {
                Map<String, String> map = new HashMap<>();
                map.put(RESULT, oidList.get(0));
                webSocketServer.sendMessage(windowName, switchWindow, SUCCESS, map);
                String name = DebugUtils.getOldWindowName(webSocketServer, rootWindowName, newOid);
                DebugUtils.disableButton(webSocketServer, windowName);
                DebugUtils.enableButton(webSocketServer, name);
                paramReq.setCloseWindow(true);
                paramReq.setOldWindowName(name);
            }
            if (!oidList.contains(oid)) {
                stepOut.deleteBreakPoint(webSocketServer, paramReq);
                webSocketServer.sendMessage(windowName, closeWindow, SUCCESS, null);
                DebugUtils.enableButton(webSocketServer, oldWindowName);
                paramReq.setCloseWindow(true);
                Map<String, Object> paramMap = webSocketServer.getParamMap(rootWindowName);
                paramMap.keySet().removeIf(oid::equals);
            }
        }
        showDebugInfo(webSocketServer, paramReq);
    }

    public void showDebugInfo(WebSocketServer webSocketServer, PublicParamReq paramReq) {
        log.info("singleStep showDebugInfo paramReq is: " + paramReq);
        String rootWindowName = paramReq.getRootWindowName();
        Statement stat = (Statement) webSocketServer.getParamMap(rootWindowName).get(STATEMENT);
        if (stat == null) {
            return;
        }

        String name = paramReq.getWindowName();
        if (paramReq.isCloseWindow()) {
            name = paramReq.getOldWindowName();
        }
        int differ = (int) webSocketServer.getParamMap(name).get(DIFFER);
        log.info("singleStep showDebugInfo differ is: " + differ);

        try {
            ResultSet stackResult = stat.executeQuery(BACKTRACE_SQL_PRE + differ + BACKTRACE_SQL);
            webSocketServer.sendMessage(name, stack, SUCCESS, DebugUtils.parseResultSet(stackResult));

            Map<String, Object> variableMap = DebugUtils.parseVariable(stat.executeQuery(INFO_LOCALS_SQL));
            Map<String, Object> paramMap = DebugUtils.addMapParam(variableMap, webSocketServer, paramReq);
            webSocketServer.sendMessage(name, variable, SUCCESS, paramMap);
        } catch (Exception e) {
            log.info(e.toString());
        }
    }

    @Override
    public Object formatJson(String str) {
        return JSON.parseObject(str, PublicParamReq.class);
    }
}
