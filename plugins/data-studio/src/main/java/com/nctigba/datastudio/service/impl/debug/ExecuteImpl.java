package com.nctigba.datastudio.service.impl.debug;

import com.alibaba.fastjson.JSON;
import com.nctigba.datastudio.base.SpringApplicationContext;
import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.model.PublicParamReq;
import com.nctigba.datastudio.model.entity.OperateStatusDO;
import com.nctigba.datastudio.service.OperationInterface;
import com.nctigba.datastudio.util.DebugUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.RESULT;
import static com.nctigba.datastudio.constants.CommonConstants.SUCCESS;
import static com.nctigba.datastudio.enums.MessageEnum.refresh;
import static com.nctigba.datastudio.enums.MessageEnum.text;

/**
 * execute
 */
@Slf4j
@Service("execute")
public class ExecuteImpl implements OperationInterface {
    @Autowired
    private StartDebugImpl startDebug;

    @Override
    public void operate(WebSocketServer webSocketServer, Object obj) throws Exception {
        log.info("execute obj is: " + obj);
        PublicParamReq paramReq = (PublicParamReq) obj;
        String sql = paramReq.getSql();
        String windowName = paramReq.getWindowName();
        Statement statement = webSocketServer.getStatement(windowName);
        if (statement == null) {
            Connection connection = webSocketServer.getConnection(windowName);
            if (connection == null) {
                connection = webSocketServer.createConnection(paramReq.getConnectionName(), paramReq.getWebUser());
            }
            statement = connection.createStatement();
            webSocketServer.setStatement(windowName, statement);
        }
        String name = DebugUtils.prepareName(sql);
        log.info("execute name is: " + name);
        ResultSet result = statement.executeQuery(DebugUtils.getFuncSql(windowName, name, webSocketServer));
        if (!result.next()) {
            statement.execute(sql);
            OperateStatusDO operateStatus = webSocketServer.getOperateStatus(windowName);
            operateStatus.enableStartDebug();
            webSocketServer.setOperateStatus(windowName, operateStatus);

            Map<String, String> resultMap = new HashMap<>();
            resultMap.put(RESULT, name);
            webSocketServer.sendMessage(windowName, refresh, SUCCESS, resultMap);
            return;
        }

        statement.execute(sql);
        OperateStatusDO operateStatus = webSocketServer.getOperateStatus(windowName);
        operateStatus.enableStartDebug();
        operateStatus.setDebug(paramReq.isDebug());
        webSocketServer.setOperateStatus(windowName, operateStatus);

        Map<String, String> map = new HashMap<>();
        map.put(RESULT, "build success！");
        webSocketServer.sendMessage(windowName, text, SUCCESS, map);
        startDebug.operate(webSocketServer, paramReq);
    }

    @Override
    public Object formatJson(String str) {
        return JSON.parseObject(str, PublicParamReq.class);
    }
}
