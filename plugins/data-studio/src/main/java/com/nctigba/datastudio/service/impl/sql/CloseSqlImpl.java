/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service.impl.sql;

import com.alibaba.fastjson.JSON;
import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.dao.ConnectionMapDAO;
import com.nctigba.datastudio.model.PublicParamReq;
import com.nctigba.datastudio.model.dto.ConnectionDTO;
import com.nctigba.datastudio.service.OperationInterface;
import com.nctigba.datastudio.util.DebugUtils;
import com.nctigba.datastudio.util.LocaleString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static com.nctigba.datastudio.dao.ConnectionMapDAO.conMap;
import static com.nctigba.datastudio.enums.MessageEnum.TEXT;

/**
 * CloseSqlImpl
 *
 * @since 2023-6-26
 */
@Slf4j
@Service("close")
public class CloseSqlImpl implements OperationInterface {

    @Override
    public void operate(WebSocketServer webSocketServer, Object obj) throws SQLException, IOException {
        try {
            PublicParamReq paramReq = DebugUtils.changeParamType(obj);
            ConnectionDTO connectionDTO = conMap.get(paramReq.getUuid());
            String windowName = paramReq.getWindowName();
            Statement statement = webSocketServer.getStatement(windowName);
            Connection connection = webSocketServer.getConnection(windowName);
            connectionDTO.reduceConnectionDTO(connectionDTO, windowName);
            ConnectionMapDAO.setConMap(paramReq.getUuid(), connectionDTO);
            if (statement != null) {
                statement.close();
                statement.cancel();
                connection.close();
                webSocketServer.setStatement(windowName, null);
            }
            webSocketServer.sendMessage(windowName, TEXT, LocaleString.transLanguageWs("2004", webSocketServer), null);
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object formatJson(String str) {
        return JSON.parseObject(str, PublicParamReq.class);
    }
}
