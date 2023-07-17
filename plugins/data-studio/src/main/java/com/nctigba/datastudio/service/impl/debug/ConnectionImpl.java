/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service.impl.debug;

import com.alibaba.fastjson.JSON;
import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.model.PublicParamReq;
import com.nctigba.datastudio.service.OperationInterface;
import com.nctigba.datastudio.util.DebugUtils;
import com.nctigba.datastudio.util.LocaleString;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import static com.nctigba.datastudio.dao.ConnectionMapDAO.conMap;
import static com.nctigba.datastudio.enums.MessageEnum.OTHER;

/**
 * ConnectionImpl
 *
 * @since 2023-6-26
 */
@Slf4j
@Service("connection")
public class ConnectionImpl implements OperationInterface {
    @Override
    public void operate(WebSocketServer webSocketServer, Object obj) throws SQLException, IOException {
        PublicParamReq paramReq = DebugUtils.changeParamType(obj);
        String windowName = paramReq.getWindowName();
        log.info("connection paramReq is: " + paramReq);
        if (!conMap.containsKey(paramReq.getUuid())) {
            throw new CustomException(LocaleString.transLanguageWs("1004", webSocketServer));
        }
        Connection connection = webSocketServer.createConnection(paramReq.getUuid(), windowName);
        webSocketServer.setConnection(windowName, connection);
        webSocketServer.sendMessage(windowName, OTHER, LocaleString.transLanguageWs("1001", webSocketServer), null);
        webSocketServer.setLanguage(paramReq.getLanguage());
    }

    @Override
    public Object formatJson(String str) {
        return JSON.parseObject(str, PublicParamReq.class);
    }
}
