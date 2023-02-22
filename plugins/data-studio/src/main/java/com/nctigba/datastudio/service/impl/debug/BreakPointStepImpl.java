package com.nctigba.datastudio.service.impl.debug;

import com.alibaba.fastjson.JSON;
import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.model.PublicParamReq;
import com.nctigba.datastudio.service.OperationInterface;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Statement;

import static com.nctigba.datastudio.constants.CommonConstants.STATEMENT;
import static com.nctigba.datastudio.constants.SqlConstants.CONTINUE_SQL;

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
        String windowName = paramReq.getOldWindowName();
        if (StringUtils.isEmpty(windowName)) {
            windowName = paramReq.getWindowName();
        }

        try {
            Statement stat = (Statement) webSocketServer.getParamMap(windowName).get(STATEMENT);
            if (stat == null) {
                return;
            }
            stat.executeQuery(CONTINUE_SQL);
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
