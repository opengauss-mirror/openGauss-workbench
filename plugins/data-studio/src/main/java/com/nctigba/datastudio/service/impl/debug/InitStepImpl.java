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
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.DIFFER;
import static com.nctigba.datastudio.constants.CommonConstants.STATEMENT;
import static com.nctigba.datastudio.constants.CommonConstants.SUCCESS;
import static com.nctigba.datastudio.constants.SqlConstants.BACKTRACE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.BACKTRACE_SQL_PRE;
import static com.nctigba.datastudio.constants.SqlConstants.INFO_LOCALS_SQL;
import static com.nctigba.datastudio.enums.MessageEnum.stack;
import static com.nctigba.datastudio.enums.MessageEnum.variable;

/**
 * init step
 */
@Slf4j
@Service("initStep")
public class InitStepImpl implements OperationInterface {
    @Override
    public void operate(WebSocketServer webSocketServer, Object obj) throws Exception {
        PublicParamReq paramReq = (PublicParamReq) obj;
        log.info("initStep paramReq: " + paramReq);
        String windowName = paramReq.getWindowName();
        DebugUtils.enableButton(webSocketServer, windowName);

        String rootWindowName = paramReq.getRootWindowName();
        Statement stat = (Statement) webSocketServer.getParamMap(rootWindowName).get(STATEMENT);
        if (stat == null) {
            return;
        }

        DebugUtils.getAvailableBreakPoints(paramReq, webSocketServer);
        int differ = (int) webSocketServer.getParamMap(windowName).get(DIFFER);
        ResultSet stackResult = stat.executeQuery(BACKTRACE_SQL_PRE + differ + BACKTRACE_SQL);
        webSocketServer.sendMessage(windowName, stack, SUCCESS, DebugUtils.parseResultSet(stackResult));

        Map<String, Object> variableMap = DebugUtils.parseVariable(stat.executeQuery(INFO_LOCALS_SQL));
        Map<String, Object> paramMap = DebugUtils.addMapParam(variableMap, webSocketServer, paramReq);
        webSocketServer.sendMessage(windowName, variable, SUCCESS, paramMap);
    }

    @Override
    public Object formatJson(String str) {
        return JSON.parseObject(str, PublicParamReq.class);
    }
}
