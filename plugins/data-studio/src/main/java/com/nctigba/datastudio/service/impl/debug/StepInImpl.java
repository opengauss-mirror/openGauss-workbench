/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service.impl.debug;

import com.alibaba.fastjson.JSON;
import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.model.query.PublicParamQuery;
import com.nctigba.datastudio.service.OperationInterface;
import com.nctigba.datastudio.utils.DebugUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.FUNC_OID;
import static com.nctigba.datastudio.constants.CommonConstants.F_STR;
import static com.nctigba.datastudio.constants.CommonConstants.LINE_NO;
import static com.nctigba.datastudio.constants.CommonConstants.OID;
import static com.nctigba.datastudio.constants.CommonConstants.RESULT;
import static com.nctigba.datastudio.constants.CommonConstants.STATEMENT;
import static com.nctigba.datastudio.constants.CommonConstants.SUCCESS;
import static com.nctigba.datastudio.constants.CommonConstants.TYPE;
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
        PublicParamQuery paramReq = DebugUtils.changeParamType(obj);
        log.info("stepIn paramReq: " + paramReq);
        String rootWindowName = paramReq.getRootWindowName();
        Statement stat = DebugUtils.changeParamType(webSocketServer, rootWindowName, STATEMENT);
        if (stat == null) {
            return;
        }

        String windowName = paramReq.getWindowName();
        String oid = DebugUtils.changeParamType(webSocketServer, windowName, OID);
        log.info("stepIn oid: " + oid);
        List<String> oidList = DebugUtils.getOidList(webSocketServer, rootWindowName);
        log.info("stepIn oidList: " + oidList);

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

        try {
            List<String> newOidList = DebugUtils.getOidList(webSocketServer, rootWindowName);
            log.info("stepIn newOidList: " + newOidList);

            String oldWindowName = paramReq.getOldWindowName();
            String type = DebugUtils.changeParamType(webSocketServer, windowName, TYPE);
            if (F_STR.equals(type) && lineNo == 0) {
                stat.executeQuery(NEXT_SQL);
                newOidList = DebugUtils.getOidList(webSocketServer, rootWindowName);
            }
            if (!oidList.contains(newOid) && newOidList.contains(newOid)) {
                paramReq.setWindowName(rootWindowName);
                paramReq.setOid(newOid);
                Map<String, Map<String, String>> resultMap = DebugUtils.getResultMap(webSocketServer, paramReq);
                webSocketServer.sendMessage(windowName, NEW_WINDOW, SUCCESS, resultMap);
                DebugUtils.disableButton(webSocketServer, windowName);
            } else if (oidList.contains(oid) && !newOidList.contains(oid)) {
                webSocketServer.sendMessage(windowName, CLOSE_WINDOW, SUCCESS, null);
                DebugUtils.enableButton(webSocketServer, oldWindowName);
                paramReq.setCloseWindow(true);
                Map<String, Object> paramMap = webSocketServer.getParamMap(rootWindowName);
                paramMap.keySet().removeIf(oid::equals);
            } else if (!oid.equals(newOid)) {
                Map<String, String> map = new HashMap<>();
                map.put(RESULT, newOid);
                webSocketServer.sendMessage(windowName, SWITCH_WINDOW, SUCCESS, map);
                String name = DebugUtils.getOldWindowName(webSocketServer, rootWindowName, newOid);
                DebugUtils.disableButton(webSocketServer, windowName);
                DebugUtils.enableButton(webSocketServer, name);
                paramReq.setCloseWindow(true);
                paramReq.setOldWindowName(name);
            } else {
                log.info("stepIn newOid -- oid: " + newOid + "--" + oid);
            }
            singleStep.showDebugInfo(webSocketServer, stat, paramReq);
        } catch (SQLException e) {
            log.info(e.getMessage());
        }
    }

    @Override
    public Object formatJson(String str) {
        return JSON.parseObject(str, PublicParamQuery.class);
    }
}
