/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service.impl.debug;

import com.alibaba.fastjson.JSON;
import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.model.PublicParamReq;
import com.nctigba.datastudio.service.OperationInterface;
import com.nctigba.datastudio.util.DebugUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.FUNC_OID;
import static com.nctigba.datastudio.constants.CommonConstants.OID;
import static com.nctigba.datastudio.constants.CommonConstants.RESULT;
import static com.nctigba.datastudio.constants.CommonConstants.STATEMENT;
import static com.nctigba.datastudio.constants.CommonConstants.SUCCESS;
import static com.nctigba.datastudio.constants.SqlConstants.FINISH_SQL;
import static com.nctigba.datastudio.enums.MessageEnum.closeWindow;
import static com.nctigba.datastudio.enums.MessageEnum.switchWindow;

/**
 * step out
 */
@Slf4j
@Service("stepOut")
public class StepOutImpl implements OperationInterface {
    @Autowired
    private SingleStepImpl singleStep;

    @Autowired
    private DeleteBreakPointImpl deleteBreakPoint;

    @Override
    public void operate(WebSocketServer webSocketServer, Object obj) throws Exception {
        PublicParamReq paramReq = (PublicParamReq) obj;
        log.info("stepOut paramReq: " + paramReq);
        String rootWindowName = paramReq.getRootWindowName();
        Statement stat = (Statement) webSocketServer.getParamMap(rootWindowName).get(STATEMENT);
        if (stat == null) {
            return;
        }

        String windowName = paramReq.getWindowName();
        String oid = (String) webSocketServer.getParamMap(windowName).get(OID);
        log.info("stepOut oid: " + oid);

        String newOid = Strings.EMPTY;
        try (
                ResultSet resultSet = stat.executeQuery(FINISH_SQL)
        ) {
            if (resultSet.next()) {
                newOid = resultSet.getString(FUNC_OID);
                log.info("stepOut newOid: " + newOid);
            }
        }

        List<String> oidList = DebugUtils.getOidList(webSocketServer, rootWindowName);
        if (oid.equals(newOid)) {
            singleStep.showDebugInfo(webSocketServer, paramReq);
            return;
        }
        if (oidList.contains(oid)) {
            Map<String, String> map = new HashMap<>();
            map.put(RESULT, newOid);
            webSocketServer.sendMessage(windowName, switchWindow, SUCCESS, map);
            DebugUtils.disableButton(webSocketServer, windowName);
        } else {
            webSocketServer.sendMessage(windowName, closeWindow, SUCCESS, null);
            Map<String, Object> paramMap = webSocketServer.getParamMap(rootWindowName);
            paramMap.keySet().removeIf(oid::equals);
        }
        String name = DebugUtils.getOldWindowName(webSocketServer, rootWindowName, newOid);
        DebugUtils.enableButton(webSocketServer, name);
        paramReq.setCloseWindow(true);
        paramReq.setOldWindowName(name);
        singleStep.showDebugInfo(webSocketServer, paramReq);
    }

    @Override
    public Object formatJson(String str) {
        return JSON.parseObject(str, PublicParamReq.class);
    }
}
