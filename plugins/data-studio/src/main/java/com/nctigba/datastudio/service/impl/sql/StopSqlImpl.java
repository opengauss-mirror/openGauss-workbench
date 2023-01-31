package com.nctigba.datastudio.service.impl.sql;

import com.alibaba.fastjson.JSON;
import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.model.PublicParamReq;
import com.nctigba.datastudio.service.OperationInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Statement;

import static com.nctigba.datastudio.enums.MessageEnum.text;

@Slf4j
@Service("stopRun")
public class StopSqlImpl implements OperationInterface {

    @Override
    public void operate(WebSocketServer webSocketServer, Object obj) {
        try {
            PublicParamReq paramReq = (PublicParamReq) obj;
            String windowName = paramReq.getWindowName();
            Statement statement = webSocketServer.getStatement(windowName);
            if (statement != null) {
                statement.close();
                statement.cancel();
                webSocketServer.setStatement(windowName, null);
            }
            webSocketServer.sendMessage(windowName, text, "Close successfully", null);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Object formatJson(String str) {
        return JSON.parseObject(str, PublicParamReq.class);
    }
}
