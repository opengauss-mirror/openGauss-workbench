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
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.CONNECTION;
import static com.nctigba.datastudio.constants.CommonConstants.RESULT;
import static com.nctigba.datastudio.constants.CommonConstants.STATEMENT;
import static com.nctigba.datastudio.constants.CommonConstants.SUCCESS;
import static com.nctigba.datastudio.constants.SqlConstants.ABORT_SQL;
import static com.nctigba.datastudio.enums.MessageEnum.TEXT;

/**
 * StopDebugImpl
 *
 * @since 2023-6-26
 */
@Slf4j
@Service("stopDebug")
public class StopDebugImpl implements OperationInterface {
    @Override
    public void operate(WebSocketServer webSocketServer, Object obj) throws SQLException, IOException {
        PublicParamReq paramReq = DebugUtils.changeParamType(obj);
        log.info("stopDebug paramReq: " + paramReq);
        String windowName = paramReq.getWindowName();
        Connection conn = DebugUtils.changeParamType(webSocketServer, windowName, CONNECTION);
        Statement statNew = DebugUtils.changeParamType(webSocketServer, windowName, STATEMENT);
        try {
            if (statNew != null) {
                statNew.execute(ABORT_SQL);
            }
        } finally {
            if (statNew != null) {
                statNew.close();
                statNew.cancel();
                webSocketServer.setParamMap(windowName, STATEMENT, null);
            }
            if (conn != null) {
                conn.close();
                webSocketServer.setParamMap(windowName, CONNECTION, null);
            }
        }

        OperateStatusDO operateStatus = webSocketServer.getOperateStatus(windowName);
        if (paramReq.getOid().equals("0")) {
            operateStatus.enableStartAnonymous();
        } else {
            operateStatus.enableStartDebug();
        }
        webSocketServer.setOperateStatus(windowName, operateStatus);
        Map<String, String> map = new HashMap<>();
        map.put(RESULT, LocaleString.transLanguageWs("1003", webSocketServer));
        webSocketServer.sendMessage(windowName, TEXT, SUCCESS, map);

        Statement statement = webSocketServer.getStatement(windowName);
        if (statement != null) {
            statement.close();
            statement.cancel();
            webSocketServer.setStatement(windowName, null);
        }
    }

    @Override
    public Object formatJson(String str) {
        return JSON.parseObject(str, PublicParamReq.class);
    }
}
