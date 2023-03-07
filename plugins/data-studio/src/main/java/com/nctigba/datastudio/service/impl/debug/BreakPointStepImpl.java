package com.nctigba.datastudio.service.impl.debug;

import com.alibaba.fastjson.JSON;
import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.model.PublicParamReq;
import com.nctigba.datastudio.service.OperationInterface;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.Statement;

import static com.nctigba.datastudio.constants.CommonConstants.FUNC_OID;
import static com.nctigba.datastudio.constants.CommonConstants.OID;
import static com.nctigba.datastudio.constants.CommonConstants.STATEMENT;
import static com.nctigba.datastudio.constants.CommonConstants.SUCCESS;
import static com.nctigba.datastudio.constants.SqlConstants.CONTINUE_SQL;
import static com.nctigba.datastudio.enums.MessageEnum.closeWindow;

/**
 * break point step
 */
@Slf4j
@Service("breakPointStep")
public class BreakPointStepImpl implements OperationInterface {
    @Autowired
    private SingleStepImpl singleStep;

    @Override
    public void operate(WebSocketServer webSocketServer, Object obj) throws Exception {
        log.info("breakPointStep obj is: " + obj);
        PublicParamReq paramReq = (PublicParamReq) obj;
        String name = paramReq.getOldWindowName();
        String windowName = paramReq.getWindowName();
        if (StringUtils.isEmpty(name)) {
            name = windowName;
        }

        try {
            Statement stat = (Statement) webSocketServer.getParamMap(name).get(STATEMENT);
            if (stat == null) {
                return;
            }
            String oid = (String) webSocketServer.getParamMap(windowName).get(OID);
            log.info("breakPointStep oid is: " + oid);
            ResultSet resultSet = stat.executeQuery(CONTINUE_SQL);
            String newOid = Strings.EMPTY;
            while (resultSet.next()) {
                newOid = resultSet.getString(FUNC_OID);
                log.info("breakPointStep newOid is: " + newOid);
            }

            if (!oid.equals(newOid)) {
                webSocketServer.sendMessage(windowName, closeWindow, SUCCESS, null);
                paramReq.setCloseWindow(true);
            }
            singleStep.showDebugInfo(webSocketServer, paramReq);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object formatJson(String str) {
        return JSON.parseObject(str, PublicParamReq.class);
    }
}
