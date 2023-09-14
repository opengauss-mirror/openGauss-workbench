/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service.impl.debug;

import com.alibaba.fastjson.JSON;
import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.model.PublicParamReq;
import com.nctigba.datastudio.model.entity.OperateStatusDO;
import com.nctigba.datastudio.service.OperationInterface;
import com.nctigba.datastudio.util.DebugUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.*;
import static com.nctigba.datastudio.constants.SqlConstants.*;
import static com.nctigba.datastudio.enums.MessageEnum.*;

/**
 * AnonymousStartDebugImpl
 *
 * @since 2023-8-17
 */
@Slf4j
@Service("anonymousStartDebug")
public class AnonymousStartDebugImpl implements OperationInterface {
    @Autowired
    private AsyncHelper asyncHelper;

    @Autowired
    private AddBreakPointImpl addBreakPoint;

    @Override
    public void operate(WebSocketServer webSocketServer, Object obj) throws SQLException, IOException {
        PublicParamReq paramReq = (PublicParamReq) obj;
        log.info("startDebug paramReq: " + paramReq);

        String rootWindowName = paramReq.getRootWindowName();
        String windowName = paramReq.getWindowName();
        Statement statement = webSocketServer.getStatement(rootWindowName);
        if (statement == null) {
            statement = webSocketServer.getConnection(rootWindowName).createStatement();
            webSocketServer.setStatement(rootWindowName, statement);
        }

        OperateStatusDO operateStatus = webSocketServer.getOperateStatus(windowName);
        operateStatus.setDebug(true);
        operateStatus.enableStopDebug();
        webSocketServer.setOperateStatus(windowName, operateStatus);

        List<String> oidList = new ArrayList<>();
        List<String> nodeNameList = new ArrayList<>();
        List<String> portList = new ArrayList<>();
        try (
                ResultSet resultSet = statement.executeQuery(DEBUG_SERVER_INFO_SQL)
        ) {
            while (resultSet.next()) {
                oidList.add(resultSet.getString(FUNC_OID));
                nodeNameList.add(resultSet.getString(NODE_NAME));
                portList.add(resultSet.getString(PORT));
            }
            log.info("inputParam debugOperate oidList: " + oidList);
        }

        String nodeName = Strings.EMPTY;
        String port = Strings.EMPTY;
        String oid = paramReq.getOid();
        if (oidList.contains(oid)) {
            int index = oidList.indexOf(oid);
            nodeName = nodeNameList.get(index);
            port = portList.get(index);
        } else {
            try (
                    ResultSet turnNoResult = statement.executeQuery(String.format(TURN_ON_SQL, oid))
            ) {
                while (turnNoResult.next()) {
                    nodeName = turnNoResult.getString(NODE_NAME);
                    port = turnNoResult.getString(PORT);
                    log.info("inputParam nodeName and port is: " + nodeName + "---" + port);
                }
            }
        }

        clientOperate(webSocketServer, paramReq, nodeName, port);
    }

    private void clientOperate(
            WebSocketServer webSocketServer, PublicParamReq paramReq, String nodeName, String port)
            throws SQLException, IOException {
        String windowName = paramReq.getWindowName();
        String oid = paramReq.getOid();
        asyncHelper.task(webSocketServer, paramReq);
        Connection conn = webSocketServer.createConnection(paramReq.getUuid(), windowName);
        Statement statNew = conn.createStatement();
        webSocketServer.setParamMap(windowName, OID, oid);
        webSocketServer.setParamMap(windowName, CONNECTION, conn);
        webSocketServer.setParamMap(windowName, STATEMENT, statNew);

        statNew.execute(String.format(ATTACH_SQL, nodeName, port));
        int diff = 0;
        List<Integer> list = new ArrayList<>();
        ResultSet resultSet = statNew.executeQuery(String.format(INFO_CODE_SQL, paramReq.getOid()));
        while (resultSet.next()) {
            String lineNo = resultSet.getString(LINE_NO);
            if (StringUtils.isEmpty(lineNo)) {
                diff++;
            }
            String canBreak = resultSet.getString(CAN_BREAK);
            if (T_STR.equals(canBreak)) {
                list.add(Integer.valueOf(lineNo));
            }
        }
        resultSet.close();
        webSocketServer.setParamMap(windowName, CAN_BREAK, list);
        webSocketServer.setParamMap(windowName, DIFFER, diff);
        log.info("DebugUtils getAvailableBreakPoints diff: " + diff);
        log.info("DebugUtils getAvailableBreakPoints list: " + list);

        List<Integer> breakPoints = paramReq.getBreakPoints();
        int differ = DebugUtils.changeParamType(webSocketServer, windowName, DIFFER);
        List<Integer> lineList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(breakPoints)) {
            for (Integer i : breakPoints) {
                if (list.contains(i - differ)) {
                    lineList.add(i);
                }
            }
            paramReq.setBreakPoints(lineList);
            addBreakPoint.operate(webSocketServer, paramReq);
        }

        ResultSet stackResult = statNew.executeQuery(BACKTRACE_SQL_PRE + differ + BACKTRACE_SQL);
        webSocketServer.sendMessage(windowName, STACK, SUCCESS, DebugUtils.parseResultSet(stackResult));
        stackResult.close();

        ResultSet bpResult = statNew.executeQuery(INFO_BREAKPOINT_PRE + differ + INFO_BREAKPOINT_SQL);
        webSocketServer.sendMessage(windowName, BREAKPOINT, SUCCESS, DebugUtils.parseBreakPoint(bpResult, oid));
        bpResult.close();

        Map<String, Object> variableMap = DebugUtils.parseVariable(statNew.executeQuery(INFO_LOCALS_SQL));
        Map<String, Object> paramMap = DebugUtils.addMapParam(variableMap, webSocketServer, paramReq);
        webSocketServer.sendMessage(windowName, VARIABLE, SUCCESS, paramMap);
    }

    @Override
    public Object formatJson(String str) {
        return JSON.parseObject(str, PublicParamReq.class);
    }
}
