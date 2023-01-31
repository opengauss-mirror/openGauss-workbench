package com.nctigba.datastudio.service.impl.sql;

import com.alibaba.fastjson.JSON;
import com.nctigba.datastudio.base.SpringApplicationContext;
import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.model.PublicParamReq;
import com.nctigba.datastudio.service.OperationInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Connection;

import static com.nctigba.datastudio.enums.MessageEnum.other;

@Slf4j
@Service("connection")
public class ConnectionImpl implements OperationInterface {

    @Override
    public void operate(WebSocketServer webSocketServer, Object obj) throws Exception {
        PublicParamReq paramReq = (PublicParamReq) obj;
        String windowName = paramReq.getWindowName();
        log.info("connection name is: " + paramReq.getConnectionName());
        Connection connection = webSocketServer.createConnection(paramReq.getConnectionName(), paramReq.getWebUser());
        webSocketServer.setConnection(windowName, connection);
        webSocketServer.sendMessage(windowName, other, "connect connection!", null);
    }

    @Override
    public Object formatJson(String str) {
        return JSON.parseObject(str, PublicParamReq.class);
    }
}
