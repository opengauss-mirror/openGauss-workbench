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

import static com.nctigba.datastudio.constants.CommonConstants.BEGIN;
import static com.nctigba.datastudio.constants.CommonConstants.CONNECTION;
import static com.nctigba.datastudio.constants.CommonConstants.DIFFER;
import static com.nctigba.datastudio.constants.CommonConstants.END;
import static com.nctigba.datastudio.constants.CommonConstants.FUNC_OID;
import static com.nctigba.datastudio.constants.CommonConstants.LINE_FEED;
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
import static com.nctigba.datastudio.constants.SqlConstants.INFO_LOCALS_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.PARENTHESES_SEMICOLON;
import static com.nctigba.datastudio.constants.SqlConstants.QUOTES_COMMA;
import static com.nctigba.datastudio.constants.SqlConstants.TURN_ON_SQL;
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
        log.info("inputParam obj is: " + obj);
        PublicParamReq paramReq = (PublicParamReq) obj;
        String windowName = paramReq.getWindowName();
        String schema = DebugUtils.prepareFuncName(paramReq.getSql()).split("\\.")[0];
        OperateStatusDO operateStatusDO = webSocketServer.getOperateStatus(windowName);
        if (!operateStatusDO.isDebug()) {
            Statement statement = webSocketServer.getStatement(windowName);
            ResultSet resultSet = statement.executeQuery(DebugUtils.prepareSql(paramReq));
            Map<String, Object> map = DebugUtils.parseResultSet(resultSet);
            log.info("inputParam result map is: " + map);
            List<List<Object>> list = (List<List<Object>>) map.get(RESULT);
            if (list.size() == 1) {
                if (list.get(0).size() == 1) {
                    webSocketServer.sendMessage(windowName, text, SUCCESS, map);
                } else {
                    Map<String, String> messageMap = new HashMap<>();
                    messageMap.put(RESULT, LocaleString.transLanguageWs("1007", webSocketServer));
                    webSocketServer.sendMessage(windowName, text, SUCCESS, messageMap);
                    webSocketServer.sendMessage(windowName, table, SUCCESS, map);
                }
            } else {
                webSocketServer.sendMessage(windowName, text, SUCCESS, map);
            }
            statement.close();
            webSocketServer.setStatement(windowName, null);
        } else {
            debugOperate(windowName, schema, webSocketServer, paramReq);
        }
    }

    private void debugOperate(String windowName, String schema, WebSocketServer webSocketServer, PublicParamReq paramReq) throws Exception {
        Statement statement = webSocketServer.getStatement(windowName);
        String name = DebugUtils.prepareName(paramReq.getSql());
        ResultSet oidResult = statement.executeQuery(DebugUtils.getFuncSql(windowName, schema, name, webSocketServer));
        String oid = Strings.EMPTY;
        while (oidResult.next()) {
            oid = oidResult.getString(OID);
        }
        log.info("inputParam oid is: " + oid);

        ResultSet resultSet = statement.executeQuery(DEBUG_SERVER_INFO_SQL);
        List<String> oidList = new ArrayList<>();
        List<String> nodeNameList = new ArrayList<>();
        List<String> portList = new ArrayList<>();
        while (resultSet.next()) {
            oidList.add(resultSet.getString(FUNC_OID));
            nodeNameList.add(resultSet.getString(NODE_NAME));
            portList.add(resultSet.getString(PORT));
        }

        String nodeName = Strings.EMPTY;
        String port = Strings.EMPTY;
        if (oidList.contains(oid)) {
            int index = oidList.indexOf(oid);
            nodeName = nodeNameList.get(index);
            port = portList.get(index);
        } else {
            ResultSet turnNoResult = statement.executeQuery(TURN_ON_SQL + oid + PARENTHESES_SEMICOLON);
            while (turnNoResult.next()) {
                nodeName = turnNoResult.getString(NODE_NAME);
                port = turnNoResult.getString(PORT);
                log.info("inputParam nodeName and port is: " + nodeName + "---" + port);
            }
        }

        asyncHelper.task(webSocketServer, paramReq);
        Connection conn = webSocketServer.createConnection(paramReq.getUuid(), windowName);
        Statement statNew = conn.createStatement();
        webSocketServer.setParamMap(windowName, OID, oid);
        webSocketServer.setParamMap(windowName, CONNECTION, conn);
        webSocketServer.setParamMap(windowName, STATEMENT, statNew);

        statNew.execute(ATTACH_SQL + nodeName + QUOTES_COMMA + port + PARENTHESES_SEMICOLON);
        List<Integer> breakPoints = getAvailableBreakPoints(windowName, paramReq, webSocketServer);
        log.info("inputParam breakPoints is: " + breakPoints);
        for (Integer i : breakPoints) {
            paramReq.setLine(i);
            addBreakPoint.operate(webSocketServer, paramReq);
        }

        int differ = (int) webSocketServer.getParamMap(windowName).get(DIFFER);
        ResultSet stackResult = statNew.executeQuery(BACKTRACE_SQL_PRE + differ + BACKTRACE_SQL);
        webSocketServer.sendMessage(windowName, stack, SUCCESS, DebugUtils.parseResultSet(stackResult));

        ResultSet variableResult = statNew.executeQuery(INFO_LOCALS_SQL);
        Map<String, Object> variableMap = DebugUtils.parseResultSet(variableResult);
        webSocketServer.sendMessage(windowName, variable, SUCCESS, DebugUtils.addMapParam(variableMap, paramReq.getSql()));
    }

    public List<Integer> getAvailableBreakPoints(String windowName, PublicParamReq paramReq, WebSocketServer webSocketServer) {
        List<Integer> list = new ArrayList<>();
        String sql = paramReq.getSql();
        List<Integer> breakPoints = paramReq.getBreakPoints();

        int begin = 0;
        int end = 0;
        int differ = 0;
        String[] split = sql.split(LINE_FEED);
        for (int i = 0; i < split.length; i++) {
            if (split[i].trim().equalsIgnoreCase("as") || split[i].trim().equalsIgnoreCase("as $$")
                    || split[i].trim().equalsIgnoreCase("as declare")) {
                differ = i;
            }

            if (split[i].toLowerCase().startsWith(BEGIN)) {
                begin = i + 1;
            }
            if (split[i].toLowerCase().startsWith(END)) {
                end = i + 1;
            }
        }

        if (!CollectionUtils.isEmpty(breakPoints)) {
            for (Integer i : breakPoints) {
                if (i > begin && i < end) {
                    list.add(i);
                }
            }
        }

        log.info("break point is: " + list);
        log.info("break point begin is: " + begin);
        log.info("break point end is: " + end);
        log.info("break point differ is: " + differ);
        webSocketServer.setParamMap(windowName, BEGIN, begin);
        webSocketServer.setParamMap(windowName, END, end);
        webSocketServer.setParamMap(windowName, DIFFER, differ);
        return list;
    }

    @Override
    public Object formatJson(String str) {
        return JSON.parseObject(str, PublicParamReq.class);
    }
}
