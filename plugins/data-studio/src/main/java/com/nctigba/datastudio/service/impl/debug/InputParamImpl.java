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
import com.nctigba.datastudio.util.LocaleString;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.CONNECTION;
import static com.nctigba.datastudio.constants.CommonConstants.DIFFER;
import static com.nctigba.datastudio.constants.CommonConstants.FUNC_OID;
import static com.nctigba.datastudio.constants.CommonConstants.NODE_NAME;
import static com.nctigba.datastudio.constants.CommonConstants.OID;
import static com.nctigba.datastudio.constants.CommonConstants.PORT;
import static com.nctigba.datastudio.constants.CommonConstants.RESULT;
import static com.nctigba.datastudio.constants.CommonConstants.STATEMENT;
import static com.nctigba.datastudio.constants.CommonConstants.SUCCESS;
import static com.nctigba.datastudio.constants.SqlConstants.ATTACH_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.BACKTRACE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.BACKTRACE_SQL_PRE;
import static com.nctigba.datastudio.constants.SqlConstants.DEBUG_SERVER_INFO_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.INFO_BREAKPOINT_PRE;
import static com.nctigba.datastudio.constants.SqlConstants.INFO_BREAKPOINT_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.INFO_LOCALS_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.TURN_ON_SQL;
import static com.nctigba.datastudio.enums.MessageEnum.breakPoint;
import static com.nctigba.datastudio.enums.MessageEnum.stack;
import static com.nctigba.datastudio.enums.MessageEnum.table;
import static com.nctigba.datastudio.enums.MessageEnum.text;
import static com.nctigba.datastudio.enums.MessageEnum.variable;

/**
 * input param
 */
@Slf4j
@Service("inputParam")
public class InputParamImpl implements OperationInterface {
    @Autowired
    private AsyncHelper asyncHelper;

    @Autowired
    private AddBreakPointImpl addBreakPoint;

    @Override
    public void operate(WebSocketServer webSocketServer, Object obj) throws Exception {
        PublicParamReq paramReq = (PublicParamReq) obj;
        log.info("inputParam paramReq: " + paramReq);
        String windowName = paramReq.getWindowName();

        OperateStatusDO operateStatus = webSocketServer.getOperateStatus(windowName);
        if (!operateStatus.isDebug()) {
            Statement statement = webSocketServer.getStatement(windowName);
            ResultSet resultSet = statement.executeQuery(DebugUtils.prepareSql(paramReq));
            Map<String, Object> map = DebugUtils.parseResultSet(resultSet);
            log.info("inputParam result map: " + map);

            List<List<Object>> list = (List<List<Object>>) map.get(RESULT);
            if (list.size() == 1) {
                if (list.get(0).size() == 1) {
                    webSocketServer.sendMessage(windowName, text, SUCCESS, map);
                } else {
                    Map<String, String> messageMap = new HashMap<>();
                    messageMap.put(RESULT, LocaleString.transLanguageWs("1008", webSocketServer));
                    webSocketServer.sendMessage(windowName, text, SUCCESS, messageMap);
                    webSocketServer.sendMessage(windowName, table, SUCCESS, map);
                }
            } else {
                webSocketServer.sendMessage(windowName, text, SUCCESS, map);
            }
            statement.close();
            webSocketServer.setStatement(windowName, null);
        } else {
            debugOperate(webSocketServer, paramReq);
        }
    }

    private void debugOperate(WebSocketServer webSocketServer, PublicParamReq paramReq) throws Exception {
        String windowName = paramReq.getWindowName();
        Statement statement = webSocketServer.getStatement(windowName);

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
            } catch (Exception e) {
                log.info(e.toString());
                throw new RuntimeException(e);
            }
        }

        asyncHelper.task(webSocketServer, paramReq);
        Connection conn = webSocketServer.createConnection(paramReq.getUuid(), windowName);
        Statement statNew = conn.createStatement();
        webSocketServer.setParamMap(windowName, OID, oid);
        webSocketServer.setParamMap(windowName, CONNECTION, conn);
        webSocketServer.setParamMap(windowName, STATEMENT, statNew);

        statNew.execute(String.format(ATTACH_SQL, nodeName, port));
        List<Integer> list = DebugUtils.getAvailableBreakPoints(paramReq, webSocketServer);
        List<Integer> breakPoints = paramReq.getBreakPoints();
        int differ = (int) webSocketServer.getParamMap(windowName).get(DIFFER);
        if (!CollectionUtils.isEmpty(breakPoints)) {
            for (Integer i : breakPoints) {
                if (list.contains(i - differ)) {
                    paramReq.setLine(i);
                    addBreakPoint.operate(webSocketServer, paramReq);
                }
            }
        }

        ResultSet stackResult = statNew.executeQuery(BACKTRACE_SQL_PRE + differ + BACKTRACE_SQL);
        webSocketServer.sendMessage(windowName, stack, SUCCESS, DebugUtils.parseResultSet(stackResult));

        ResultSet bpResult = statNew.executeQuery(INFO_BREAKPOINT_PRE + differ + INFO_BREAKPOINT_SQL);
        webSocketServer.sendMessage(windowName, breakPoint, SUCCESS, DebugUtils.parseBreakPoint(bpResult, oid));

        Map<String, Object> variableMap = DebugUtils.parseVariable(statNew.executeQuery(INFO_LOCALS_SQL));
        Map<String, Object> paramMap = DebugUtils.addMapParam(variableMap, webSocketServer, paramReq);
        webSocketServer.sendMessage(windowName, variable, SUCCESS, paramMap);
    }

    @Override
    public Object formatJson(String str) {
        return JSON.parseObject(str, PublicParamReq.class);
    }
}
