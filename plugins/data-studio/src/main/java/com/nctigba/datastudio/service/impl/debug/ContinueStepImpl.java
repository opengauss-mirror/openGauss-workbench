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
import org.springframework.util.CollectionUtils;

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
import static com.nctigba.datastudio.constants.SqlConstants.CONTINUE_SQL;
import static com.nctigba.datastudio.enums.MessageEnum.closeWindow;
import static com.nctigba.datastudio.enums.MessageEnum.switchWindow;

/**
 * break point step
 */
@Slf4j
@Service("continueStep")
public class ContinueStepImpl implements OperationInterface {
    @Autowired
    private SingleStepImpl singleStep;

    @Autowired
    private StepOutImpl stepOut;

    @Override
    public void operate(WebSocketServer webSocketServer, Object obj) throws Exception {
        PublicParamReq paramReq = (PublicParamReq) obj;
        log.info("continueStep paramReq: " + paramReq);
        String rootWindowName = paramReq.getRootWindowName();
        String oldWindowName = paramReq.getOldWindowName();
        String windowName = paramReq.getWindowName();

        try {
            Statement stat = (Statement) webSocketServer.getParamMap(rootWindowName).get(STATEMENT);
            if (stat == null) {
                return;
            }
            String oid = (String) webSocketServer.getParamMap(windowName).get(OID);
            log.info("continueStep windowName oid: " + oid);
            String newOid = Strings.EMPTY;
            ResultSet resultSet = stat.executeQuery(CONTINUE_SQL);
            if (resultSet.next()) {
                newOid = resultSet.getString(FUNC_OID);
            }

            List<String> oidList = DebugUtils.getOidList(webSocketServer, rootWindowName);
            if (!CollectionUtils.isEmpty(oidList)) {
                if (oidList.contains(oid) && !oidList.get(0).equals(oid)) {
                    Map<String, String> map = new HashMap<>();
                    map.put(RESULT, oidList.get(0));
                    webSocketServer.sendMessage(windowName, switchWindow, SUCCESS, map);
                    String name = DebugUtils.getOldWindowName(webSocketServer, rootWindowName, newOid);
                    DebugUtils.disableButton(webSocketServer, windowName);
                    DebugUtils.enableButton(webSocketServer, name);
                    paramReq.setCloseWindow(true);
                    paramReq.setOldWindowName(name);
                }
                if (!oidList.contains(oid)) {
                    webSocketServer.sendMessage(windowName, closeWindow, SUCCESS, null);
                    DebugUtils.enableButton(webSocketServer, oldWindowName);
                    paramReq.setCloseWindow(true);
                    Map<String, Object> paramMap = webSocketServer.getParamMap(rootWindowName);
                    paramMap.keySet().removeIf(oid::equals);
                }
            }
            singleStep.showDebugInfo(webSocketServer, paramReq);
        } catch (Exception e) {
            log.info(e.toString());

        }
    }

    @Override
    public Object formatJson(String str) {
        return JSON.parseObject(str, PublicParamReq.class);
    }
}
