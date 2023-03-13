package com.nctigba.datastudio.service.impl.debug;

import com.alibaba.fastjson.JSON;
import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.model.PublicParamReq;
import com.nctigba.datastudio.service.OperationInterface;
import com.nctigba.datastudio.util.DebugUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.DIFFER;
import static com.nctigba.datastudio.constants.CommonConstants.FUNC_OID;
import static com.nctigba.datastudio.constants.CommonConstants.LINE_NO;
import static com.nctigba.datastudio.constants.CommonConstants.OID;
import static com.nctigba.datastudio.constants.CommonConstants.STATEMENT;
import static com.nctigba.datastudio.constants.CommonConstants.SUCCESS;
import static com.nctigba.datastudio.constants.SqlConstants.BACKTRACE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.BACKTRACE_SQL_PRE;
import static com.nctigba.datastudio.constants.SqlConstants.CONTINUE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.INFO_LOCALS_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.NEXT_SQL;
import static com.nctigba.datastudio.enums.MessageEnum.closeWindow;
import static com.nctigba.datastudio.enums.MessageEnum.stack;
import static com.nctigba.datastudio.enums.MessageEnum.variable;

/**
 * single step
 */
@Slf4j
@Service("singleStep")
public class SingleStepImpl implements OperationInterface {
    @Override
    public void operate(WebSocketServer webSocketServer, Object obj) throws Exception {
        log.info("singleStep obj is: " + obj);
        PublicParamReq paramReq = (PublicParamReq) obj;
        String name = paramReq.getOldWindowName();
        String windowName = paramReq.getWindowName();
        String oldWindowName = paramReq.getOldWindowName();
        if (StringUtils.isEmpty(name)) {
            name = windowName;
        }

        Statement stat = (Statement) webSocketServer.getParamMap(name).get(STATEMENT);
        if (stat == null) {
            return;
        }
        ResultSet resultSet = stat.executeQuery(NEXT_SQL);
        int lineNo = -1;
        while (resultSet.next()) {
            lineNo = resultSet.getInt(LINE_NO);
            log.info("singleStep lineNo is: " + lineNo);
        }
        if (lineNo == 0 && StringUtils.isNotEmpty(oldWindowName)) {
            stat.execute(NEXT_SQL);
            webSocketServer.sendMessage(windowName, closeWindow, SUCCESS, null);
            paramReq.setCloseWindow(true);
        }
        showDebugInfo(webSocketServer, paramReq);
    }

    public void showDebugInfo(WebSocketServer webSocketServer, PublicParamReq paramReq) {
        log.info("singleStep showDebugInfo paramReq is: " + paramReq);
        String windowName = paramReq.getWindowName();
        String oldWindowName = paramReq.getOldWindowName();
        String name = oldWindowName;
        if (StringUtils.isEmpty(name)) {
            name = windowName;
        }
        int differ = (int) webSocketServer.getParamMap(name).get(DIFFER);
        log.info("singleStep showDebugInfo differ is: " + differ);
        Statement stat = (Statement) webSocketServer.getParamMap(name).get(STATEMENT);
        if (stat == null) {
            return;
        }

        try {
            if (paramReq.getOperation().equals("stepOut") || paramReq.isCloseWindow()) {
                name = oldWindowName;
            } else {
                name = windowName;
            }
            ResultSet stackResult = stat.executeQuery(BACKTRACE_SQL_PRE + differ + BACKTRACE_SQL);
            webSocketServer.sendMessage(name, stack, SUCCESS, DebugUtils.parseResultSet(stackResult));

            ResultSet variableResult = stat.executeQuery(INFO_LOCALS_SQL);
            Map<String, Object> variableMap = DebugUtils.parseResultSet(variableResult);
            webSocketServer.sendMessage(name, variable, SUCCESS, DebugUtils.addMapParam(variableMap, paramReq.getSql()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object formatJson(String str) {
        return JSON.parseObject(str, PublicParamReq.class);
    }
}
