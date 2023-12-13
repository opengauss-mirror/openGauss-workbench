/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service.impl.sql;

import com.alibaba.fastjson.JSON;
import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.model.query.PublicParamQuery;
import com.nctigba.datastudio.model.entity.OperateStatusDO;
import com.nctigba.datastudio.service.OperationInterface;
import com.nctigba.datastudio.utils.DebugUtils;
import com.nctigba.datastudio.utils.LocaleStringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;

import static com.nctigba.datastudio.enums.MessageEnum.TEXT;

/**
 * StopSqlImpl
 *
 * @since 2023-6-26
 */
@Slf4j
@Service("stopRun")
public class StopSqlImpl implements OperationInterface {
    @Override
    public void operate(WebSocketServer webSocketServer, Object obj) throws SQLException, IOException {
        PublicParamQuery paramReq = DebugUtils.changeParamType(obj);
        String windowName = paramReq.getWindowName();
        Statement statement = webSocketServer.getStatement(windowName);
        if (statement != null) {
            statement.close();
            statement.cancel();
        }
        OperateStatusDO operateStatus = webSocketServer.getOperateStatus(windowName);
        operateStatus.enableStopRun();
        webSocketServer.setOperateStatus(windowName, operateStatus);
        webSocketServer.sendMessage(windowName, TEXT, LocaleStringUtils.transLanguageWs("2004", webSocketServer), null);
    }

    @Override
    public Object formatJson(String str) {
        return JSON.parseObject(str, PublicParamQuery.class);
    }
}
