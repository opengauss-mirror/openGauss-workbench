package com.nctigba.datastudio.service.impl.debug;

import com.alibaba.fastjson.JSON;
import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.model.PublicParamReq;
import com.nctigba.datastudio.model.entity.OperateStatusDO;
import com.nctigba.datastudio.service.OperationInterface;
import com.nctigba.datastudio.util.DebugUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.FUNC_OID;
import static com.nctigba.datastudio.constants.CommonConstants.LINE_FEED;
import static com.nctigba.datastudio.constants.CommonConstants.LINE_NO;
import static com.nctigba.datastudio.constants.CommonConstants.OID;
import static com.nctigba.datastudio.constants.CommonConstants.RESULT;
import static com.nctigba.datastudio.constants.CommonConstants.STATEMENT;
import static com.nctigba.datastudio.constants.CommonConstants.SUCCESS;
import static com.nctigba.datastudio.constants.CommonConstants.TYPE;
import static com.nctigba.datastudio.constants.SqlConstants.INFO_CODE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.NEXT_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.PARENTHESES_SEMICOLON;
import static com.nctigba.datastudio.constants.SqlConstants.STEP_SQL;
import static com.nctigba.datastudio.enums.MessageEnum.closeWindow;
import static com.nctigba.datastudio.enums.MessageEnum.newWindow;

/**
 * step in
 */
@Slf4j
@Service("stepIn")
public class StepInImpl implements OperationInterface {
    @Autowired
    private SingleStepImpl singleStep;

    @Autowired
    private StepOutImpl stepOut;

    @Override
    public void operate(WebSocketServer webSocketServer, Object obj) throws Exception {
        log.info("stepIn obj is: " + obj);
        PublicParamReq paramReq = (PublicParamReq) obj;
        String name = paramReq.getOldWindowName();
        String windowName = paramReq.getWindowName();
        String oldWindowName = paramReq.getOldWindowName();
        if (StringUtils.isEmpty(name)) {
            name = windowName;
        }
        OperateStatusDO operateStatus = webSocketServer.getOperateStatus(name);
        if (!operateStatus.isStepIn()) {
            return;
        }
        Statement stat = (Statement) webSocketServer.getParamMap(name).get(STATEMENT);
        if (stat == null) {
            return;
        }

        String oid = (String) webSocketServer.getParamMap(windowName).get(OID);
        log.info("stepIn oid is: " + oid);
        ResultSet resultSet = stat.executeQuery(STEP_SQL);
        String newOid = Strings.EMPTY;
        int lineNo = -1;
        while (resultSet.next()) {
            lineNo = resultSet.getInt(LINE_NO);
            newOid = resultSet.getString(FUNC_OID);
            log.info("stepIn newOid is: " + newOid);
        }
        String type = (String) webSocketServer.getParamMap(windowName).get(TYPE);
        if ("p".equals(type)) {
            if (!oid.equals(newOid) && StringUtils.isNotEmpty(oldWindowName)) {
                stepOut.deleteBreakPoint(webSocketServer, paramReq);
                webSocketServer.sendMessage(windowName, closeWindow, SUCCESS, null);
                paramReq.setCloseWindow(true);
                singleStep.showDebugInfo(webSocketServer, paramReq);
                return;
            }
        } else if ("f".equals(type)) {
            if (lineNo == 0 && StringUtils.isNotEmpty(oldWindowName)) {
                stepOut.deleteBreakPoint(webSocketServer, paramReq);
                stat.execute(NEXT_SQL);
                webSocketServer.sendMessage(windowName, closeWindow, SUCCESS, null);
                paramReq.setCloseWindow(true);
                singleStep.showDebugInfo(webSocketServer, paramReq);
                return;
            }
        }
        if (oid.equals(newOid)) {
            singleStep.showDebugInfo(webSocketServer, paramReq);
        } else {
            Map<String, String> map = new HashMap<>();
            ResultSet infoCodeResult = stat.executeQuery(INFO_CODE_SQL + newOid + PARENTHESES_SEMICOLON);
            StringBuilder sb = new StringBuilder();
            while (infoCodeResult.next()) {
                sb.append(infoCodeResult.getString("query")).append(LINE_FEED);
            }
            log.info("stepIn sb is: " + sb);
            map.put(RESULT, DebugUtils.prepareName(sb.toString()));
            webSocketServer.sendMessage(name, newWindow, SUCCESS, map);
        }
    }

    @Override
    public Object formatJson(String str) {
        return JSON.parseObject(str, PublicParamReq.class);
    }
}
