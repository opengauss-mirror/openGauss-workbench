/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service.impl.debug;

import com.alibaba.fastjson.JSON;
import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.model.PublicParamReq;
import com.nctigba.datastudio.model.entity.OperateStatusDO;
import com.nctigba.datastudio.service.OperationInterface;
import com.nctigba.datastudio.util.LocaleString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.CONNECTION;
import static com.nctigba.datastudio.constants.CommonConstants.RESULT;
import static com.nctigba.datastudio.constants.CommonConstants.STATEMENT;
import static com.nctigba.datastudio.constants.CommonConstants.SUCCESS;
import static com.nctigba.datastudio.constants.SqlConstants.ABORT_SQL;
import static com.nctigba.datastudio.enums.MessageEnum.text;

/**
 * stop debug
 */
@Slf4j
@Service("stopDebug")
public class StopDebugImpl implements OperationInterface {
    @Override
    public void operate(WebSocketServer webSocketServer, Object obj) throws Exception {
        PublicParamReq paramReq = (PublicParamReq) obj;
        log.info("stopDebug paramReq: " + paramReq);
        String windowName = paramReq.getWindowName();
        Connection conn = (Connection) webSocketServer.getParamMap(windowName).get(CONNECTION);
        Statement statNew = (Statement) webSocketServer.getParamMap(windowName).get(STATEMENT);
        try {
            if (statNew != null) {
                statNew.execute(ABORT_SQL);
            }
        } catch (Exception e) {
            log.info(e.toString());
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
        operateStatus.enableStartDebug();
        webSocketServer.setOperateStatus(windowName, operateStatus);
        Map<String, String> map = new HashMap<>();
        map.put(RESULT, LocaleString.transLanguageWs("1003", webSocketServer));
        webSocketServer.sendMessage(windowName, text, SUCCESS, map);

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
