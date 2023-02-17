package com.nctigba.datastudio.service.impl.sql;

import cn.hutool.core.thread.ThreadUtil;
import com.alibaba.fastjson.JSON;
import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.model.PublicParamReq;
import com.nctigba.datastudio.model.entity.OperateStatusDO;
import com.nctigba.datastudio.service.OperationInterface;
import com.nctigba.datastudio.util.DebugUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import static com.nctigba.datastudio.enums.MessageEnum.table;
import static com.nctigba.datastudio.enums.MessageEnum.text;

@Slf4j
@Service("startRun")
public class StartSqlImpl implements OperationInterface {

    @Override
    @Async
    public void operate(WebSocketServer webSocketServer, Object obj) throws Exception {
        PublicParamReq paramReq = (PublicParamReq) obj;
        String windowName = paramReq.getWindowName();
        Connection connection = webSocketServer.getConnection(windowName);
        Statement stat = connection.createStatement();
        webSocketServer.setStatement(windowName, stat);

        log.info("tableColumnList request is: " + paramReq);
        ThreadUtil.execAsync(() -> {
        try {
            webSocketServer.sendMessage(windowName, text, "Execution start", null);
            boolean result = stat.execute(paramReq.getSql());
            OperateStatusDO operateStatus = webSocketServer.getOperateStatus(windowName);
            operateStatus.enableStopRun();
            webSocketServer.setOperateStatus(windowName, operateStatus);
            while (true) {
                if (result) {
                    webSocketServer.sendMessage(windowName, table, "Execution succeeded", DebugUtils.parseResultSet(stat.getResultSet()));
                    webSocketServer.sendMessage(windowName, text, "Execution succeeded", null);
                } else {
                    if (stat.getUpdateCount() != -1) {
                        webSocketServer.sendMessage(windowName, text, "Number of rows affected by successful execution" + stat.getUpdateCount(), null);
                    } else {
                        break;
                    }
                }
                result = stat.getMoreResults();
            }
            webSocketServer.sendMessage(windowName, text, "End of execution", null);
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                stat.close();
                stat.cancel();
                webSocketServer.setStatement(windowName, null);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        });
    }

    @Override
    public Object formatJson(String str) {
        return JSON.parseObject(str, PublicParamReq.class);
    }
}
