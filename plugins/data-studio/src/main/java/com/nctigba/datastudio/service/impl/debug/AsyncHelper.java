/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service.impl.debug;

import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.model.PublicParamReq;
import com.nctigba.datastudio.model.entity.OperateStatusDO;
import com.nctigba.datastudio.model.entity.SqlHistoryDO;
import com.nctigba.datastudio.service.impl.sql.SqlHistoryManagerServiceImpl;
import com.nctigba.datastudio.util.DebugUtils;
import com.nctigba.datastudio.util.LocaleString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.nctigba.datastudio.constants.CommonConstants.CONNECTION;
import static com.nctigba.datastudio.constants.CommonConstants.FIVE_HUNDRED;
import static com.nctigba.datastudio.constants.CommonConstants.OID;
import static com.nctigba.datastudio.constants.CommonConstants.RESULT;
import static com.nctigba.datastudio.constants.CommonConstants.STATEMENT;
import static com.nctigba.datastudio.constants.CommonConstants.SUCCESS;
import static com.nctigba.datastudio.constants.SqlConstants.TURN_OFF_SQL;
import static com.nctigba.datastudio.enums.MessageEnum.CLOSE_WINDOW;
import static com.nctigba.datastudio.enums.MessageEnum.CREATE_COVERAGE_RATE;
import static com.nctigba.datastudio.enums.MessageEnum.OPERATE_STATUS;
import static com.nctigba.datastudio.enums.MessageEnum.TABLE;
import static com.nctigba.datastudio.enums.MessageEnum.TEXT;
import static com.nctigba.datastudio.enums.MessageEnum.WINDOW;

/**
 * AsyncHelper
 *
 * @since 2023-6-26
 */
@Service
@Slf4j
public class AsyncHelper {
    @Autowired
    private SqlHistoryManagerServiceImpl managerService;

    private final Pattern pattern = Pattern.compile("[0-9]*");

    @Async
    public void task(WebSocketServer webSocketServer, PublicParamReq paramReq) throws SQLException, IOException {
        log.info("AsyncHelper paramReq: " + paramReq);
        String windowName = paramReq.getWindowName();
        Statement statement = webSocketServer.getStatement(windowName);
        if (isAnonymousOid(paramReq)) {
            try {
                statement.executeUpdate(paramReq.getSql());
            } catch (SQLException e) {
                webSocketServer.sendMessage(windowName, WINDOW, FIVE_HUNDRED, e.getMessage(), e.getStackTrace());
            }
        } else {
            try (
                    ResultSet resultSet = statement.executeQuery(DebugUtils.prepareSql(paramReq))
            ) {
                funcTask(webSocketServer, paramReq, resultSet);
            } catch (SQLException | IOException e) {
                webSocketServer.sendMessage(windowName, WINDOW, FIVE_HUNDRED, e.getMessage(), e.getStackTrace());
            }
        }

        OperateStatusDO operateStatusDO = webSocketServer.getOperateStatus(windowName);
        if (isAnonymousOid(paramReq) || paramReq.isInPackage()) {
            operateStatusDO.enableStartAnonymous();
        } else {
            operateStatusDO.enableStartDebug();
        }
        webSocketServer.setOperateStatus(windowName, operateStatusDO);
        Map<String, Object> operateStatusMap = new HashMap<>();
        operateStatusMap.put(RESULT, operateStatusDO);
        webSocketServer.sendMessage(windowName, OPERATE_STATUS, SUCCESS, operateStatusMap);

        closeWindow(webSocketServer, windowName);
        String oid = DebugUtils.changeParamType(webSocketServer, windowName, OID);
        statement.execute(String.format(TURN_OFF_SQL, oid));
        log.info("AsyncHelper oid: " + oid);

        if (!paramReq.isCoverage() && !isAnonymousOid(paramReq)) {
            closeConnection(webSocketServer, windowName, statement);
            Map<String, String> map = new HashMap<>();
            map.put(RESULT, LocaleString.transLanguageWs("1010", webSocketServer));
            webSocketServer.sendMessage(windowName, CREATE_COVERAGE_RATE, SUCCESS, map);
            return;
        }
        closeConnection(webSocketServer, windowName, statement);
    }

    private void closeWindow(WebSocketServer webSocketServer, String windowName) throws IOException {
        Map<String, Object> paramMap = webSocketServer.getParamMap(windowName);
        log.info("AsyncHelper paramMap: " + paramMap);
        Set<String> keySet = paramMap.keySet();
        for (String key : keySet) {
            Matcher isNum = pattern.matcher(key);
            if (isNum.matches()) {
                webSocketServer.sendMessage(String.valueOf(paramMap.get(key)), CLOSE_WINDOW, SUCCESS, null);
            }
        }
    }

    private void funcTask(WebSocketServer webSocketServer, PublicParamReq paramReq, ResultSet resultSet)
            throws SQLException, IOException {
        String windowName = paramReq.getWindowName();
        Map<String, Object> map = DebugUtils.parseResultSet(resultSet);
        log.info("AsyncHelper result map: " + map);

        List<List<Object>> list = (List<List<Object>>) map.get(RESULT);
        if (list.size() == 1) {
            if (list.get(0).size() == 1) {
                webSocketServer.sendMessage(windowName, TEXT, SUCCESS, map);
            } else {
                Map<String, String> messageMap = new HashMap<>();
                messageMap.put(RESULT, LocaleString.transLanguageWs("1008", webSocketServer));
                webSocketServer.sendMessage(windowName, TEXT, SUCCESS, messageMap);
                webSocketServer.sendMessage(windowName, TABLE, SUCCESS, map);
            }
        } else {
            webSocketServer.sendMessage(windowName, TEXT, SUCCESS, map);
        }
    }

    private boolean isAnonymousOid(PublicParamReq paramReq) {
        String funcOid = paramReq.getOid();
        return funcOid.equals("0");
    }

    private void closeConnection(
            WebSocketServer webSocketServer, String windowName, Statement statement) throws SQLException {
        Connection conn = DebugUtils.changeParamType(webSocketServer, windowName, CONNECTION);
        Statement stat = DebugUtils.changeParamType(webSocketServer, windowName, STATEMENT);
        if (stat != null) {
            stat.close();
            stat.cancel();
            webSocketServer.setParamMap(windowName, STATEMENT, null);
        }
        if (conn != null) {
            conn.close();
            webSocketServer.setParamMap(windowName, CONNECTION, null);
        }
        statement.close();
        webSocketServer.setStatement(windowName, null);
    }

    /**
     * insert sql execute history
     *
     * @param list list
     */
    @Async
    public void insertSqlHistory(List<SqlHistoryDO> list) {
        managerService.insertHistory(list);
    }
}
