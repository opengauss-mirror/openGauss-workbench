/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service.impl.debug;

import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.model.PublicParamReq;
import com.nctigba.datastudio.model.entity.OperateStatusDO;
import com.nctigba.datastudio.util.DebugUtils;
import com.nctigba.datastudio.util.LocaleString;
import lombok.extern.slf4j.Slf4j;
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

import static com.nctigba.datastudio.constants.CommonConstants.CONNECTION;
import static com.nctigba.datastudio.constants.CommonConstants.FIVE_HUNDRED;
import static com.nctigba.datastudio.constants.CommonConstants.OID;
import static com.nctigba.datastudio.constants.CommonConstants.RESULT;
import static com.nctigba.datastudio.constants.CommonConstants.STATEMENT;
import static com.nctigba.datastudio.constants.CommonConstants.SUCCESS;
import static com.nctigba.datastudio.constants.SqlConstants.TURN_OFF_SQL;
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
    @Async
    public void task(WebSocketServer webSocketServer, PublicParamReq paramReq) throws SQLException, IOException {
        log.info("AsyncHelper paramReq: " + paramReq);
        String windowName = paramReq.getWindowName();
        Statement statement = webSocketServer.getStatement(windowName);
        try (
                ResultSet resultSet = statement.executeQuery(DebugUtils.prepareSql(paramReq))
        ) {
            if (!paramReq.isCoverage()) {
                closeConnection(webSocketServer, windowName, statement);
                Map<String, String> map = new HashMap<>();
                map.put(RESULT, LocaleString.transLanguageWs("1010", webSocketServer));
                webSocketServer.sendMessage(windowName, CREATE_COVERAGE_RATE, SUCCESS, map);
                return;
            }

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
        } catch (SQLException | IOException e) {
            webSocketServer.sendMessage(windowName, WINDOW, FIVE_HUNDRED, e.getMessage(), e.getStackTrace());
        }

        OperateStatusDO operateStatusDO = webSocketServer.getOperateStatus(windowName);
        operateStatusDO.enableStartDebug();
        webSocketServer.setOperateStatus(windowName, operateStatusDO);
        Map<String, Object> operateStatusMap = new HashMap<>();
        operateStatusMap.put(RESULT, operateStatusDO);
        webSocketServer.sendMessage(windowName, OPERATE_STATUS, SUCCESS, operateStatusMap);

        String oid = DebugUtils.changeParamType(webSocketServer, windowName, OID);
        statement.execute(String.format(TURN_OFF_SQL, oid));
        log.info("AsyncHelper oid: " + oid);
        closeConnection(webSocketServer, windowName, statement);
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
}
