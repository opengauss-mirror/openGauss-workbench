package com.nctigba.datastudio.service.impl.debug;

import com.alibaba.fastjson.JSON;
import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.model.PublicParamReq;
import com.nctigba.datastudio.model.entity.OperateStatusDO;
import com.nctigba.datastudio.service.OperationInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.CONNECTION;
import static com.nctigba.datastudio.constants.CommonConstants.FUNC_OID;
import static com.nctigba.datastudio.constants.CommonConstants.RESULT;
import static com.nctigba.datastudio.constants.CommonConstants.STATEMENT;
import static com.nctigba.datastudio.constants.CommonConstants.SUCCESS;
import static com.nctigba.datastudio.constants.SqlConstants.ABORT_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.DEBUG_INFO_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.PARENTHESES_SEMICOLON;
import static com.nctigba.datastudio.constants.SqlConstants.TURN_OFF_SQL;
import static com.nctigba.datastudio.enums.MessageEnum.text;

/**
 * stop debug
 */
@Slf4j
@Service("stopDebug")
public class StopDebugImpl implements OperationInterface {
    @Override
    public void operate(WebSocketServer webSocketServer, Object obj) throws Exception {
        log.info("stopDebug obj is: " + obj);
        PublicParamReq paramReq = (PublicParamReq) obj;
        String windowName = paramReq.getWindowName();
        Connection conn = (Connection) webSocketServer.getParamMap(windowName).get(CONNECTION);
        Statement statNew = (Statement) webSocketServer.getParamMap(windowName).get(STATEMENT);
        try {
            if (statNew != null) {
                statNew.execute(ABORT_SQL);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (statNew != null) {
                statNew.close();
                statNew.cancel();
            }
            if (conn != null) {
                conn.close();
            }
        }

        OperateStatusDO operateStatus = webSocketServer.getOperateStatus(windowName);
        operateStatus.enableStartDebug();
        webSocketServer.setOperateStatus(windowName, operateStatus);

        Map<String, String> map = new HashMap<>();
        map.put(RESULT, "stop debug！");
        webSocketServer.sendMessage(windowName, text, SUCCESS, map);
    }

    @Override
    public Object formatJson(String str) {
        return JSON.parseObject(str, PublicParamReq.class);
    }
}
