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

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.CAN_BREAK;
import static com.nctigba.datastudio.constants.CommonConstants.FUNC_OID;
import static com.nctigba.datastudio.constants.CommonConstants.LINE_NO;
import static com.nctigba.datastudio.constants.CommonConstants.OID;
import static com.nctigba.datastudio.constants.CommonConstants.RESULT;
import static com.nctigba.datastudio.constants.CommonConstants.STATEMENT;
import static com.nctigba.datastudio.constants.CommonConstants.SUCCESS;
import static com.nctigba.datastudio.constants.CommonConstants.TYPE;
import static com.nctigba.datastudio.constants.CommonConstants.T_STR;
import static com.nctigba.datastudio.constants.SqlConstants.BACKTRACE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.BACKTRACE_SQL_PRE;
import static com.nctigba.datastudio.constants.SqlConstants.INFO_CODE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.NEXT_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.STEP_SQL;
import static com.nctigba.datastudio.enums.MessageEnum.CLOSE_WINDOW;
import static com.nctigba.datastudio.enums.MessageEnum.NEW_WINDOW;
import static com.nctigba.datastudio.enums.MessageEnum.SWITCH_WINDOW;

/**
 * StepInImpl
 *
 * @since 2023-6-26
 */
@Slf4j
@Service("stepIn")
public class StepInImpl implements OperationInterface {
    @Autowired
    private SingleStepImpl singleStep;

    @Override
    public void operate(WebSocketServer webSocketServer, Object obj) throws SQLException, IOException {
        PublicParamReq paramReq = DebugUtils.changeParamType(obj);
        log.info("stepIn paramReq: " + paramReq);
        String rootWindowName = paramReq.getRootWindowName();
        Statement stat = DebugUtils.changeParamType(webSocketServer, rootWindowName, STATEMENT);
        if (stat == null) {
            return;
        }

        String windowName = paramReq.getWindowName();
        String oid = DebugUtils.changeParamType(webSocketServer, windowName, OID);
        log.info("stepIn oid: " + oid);

        List<Integer> lineList = new ArrayList<>();
        ResultSet stackResult = stat.executeQuery(BACKTRACE_SQL_PRE + 0 + BACKTRACE_SQL);
        while (stackResult.next()) {
            lineList.add(stackResult.getInt(LINE_NO));
        }
        log.info("stepIn lineList: " + lineList);

        int lastLine = 0;
        try (
                ResultSet allLineResult = stat.executeQuery(String.format(INFO_CODE_SQL, oid))
        ) {
            while (allLineResult.next()) {
                if (T_STR.equals(allLineResult.getString(CAN_BREAK))) {
                    lastLine = allLineResult.getInt(LINE_NO);
                }
            }
        }
        log.info("stepIn lastLine: " + lastLine);
        List<String> oidList = DebugUtils.getOidList(webSocketServer, rootWindowName);

        String newOid = Strings.EMPTY;
        int lineNo = -1;
        try (
                ResultSet resultSet = stat.executeQuery(STEP_SQL)
        ) {
            while (resultSet.next()) {
                lineNo = resultSet.getInt(LINE_NO);
                newOid = resultSet.getString(FUNC_OID);
                log.info("stepIn newOid: " + newOid);
                log.info("stepIn lineNo: " + lineNo);
            }
        }

        if (lineList.get(0) != lastLine) {
            if (newOid.equals(oid)) {
                singleStep.showDebugInfo(webSocketServer, stat, paramReq);
            } else if (oidList.contains(newOid)) {
                Map<String, String> map = new HashMap<>();
                map.put(RESULT, newOid);
                webSocketServer.sendMessage(windowName, SWITCH_WINDOW, SUCCESS, map);
                String name = DebugUtils.getOldWindowName(webSocketServer, rootWindowName, newOid);
                DebugUtils.disableButton(webSocketServer, windowName);
                DebugUtils.enableButton(webSocketServer, name);
                paramReq.setCloseWindow(true);
                paramReq.setOldWindowName(name);
                singleStep.showDebugInfo(webSocketServer, stat, paramReq);
            } else {
                paramReq.setWindowName(rootWindowName);
                paramReq.setOid(newOid);
                Map<String, Map<String, String>> resultMap = DebugUtils.getResultMap(webSocketServer, paramReq);
                webSocketServer.sendMessage(windowName, NEW_WINDOW, SUCCESS, resultMap);
                DebugUtils.disableButton(webSocketServer, windowName);
            }
            return;
        }

        oidList = DebugUtils.getOidList(webSocketServer, rootWindowName);
        String oldWindowName = paramReq.getOldWindowName();
        String type = DebugUtils.changeParamType(webSocketServer, windowName, TYPE);
        if (!CollectionUtils.isEmpty(oidList)) {
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
                webSocketServer.sendMessage(windowName, SWITCH_WINDOW, SUCCESS, map);
                String name = DebugUtils.getOldWindowName(webSocketServer, rootWindowName, newOid);
                DebugUtils.disableButton(webSocketServer, windowName);
                DebugUtils.enableButton(webSocketServer, name);
                paramReq.setCloseWindow(true);
                paramReq.setOldWindowName(name);
            }
            if (!oidList.contains(oid)) {
                webSocketServer.sendMessage(windowName, CLOSE_WINDOW, SUCCESS, null);
                DebugUtils.enableButton(webSocketServer, oldWindowName);
                paramReq.setCloseWindow(true);
                Map<String, Object> paramMap = webSocketServer.getParamMap(rootWindowName);
                paramMap.keySet().removeIf(oid::equals);
            }
        }
        singleStep.showDebugInfo(webSocketServer, stat, paramReq);
    }

    @Override
    public Object formatJson(String str) {
        return JSON.parseObject(str, PublicParamReq.class);
    }
}
