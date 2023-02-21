package com.nctigba.datastudio.service.impl.debug;

import com.alibaba.fastjson.JSON;
import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.model.PublicParamReq;
import com.nctigba.datastudio.model.entity.OperateStatusDO;
import com.nctigba.datastudio.service.OperationInterface;
import com.nctigba.datastudio.util.DebugUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.FUNC_OID;
import static com.nctigba.datastudio.constants.CommonConstants.OID;
import static com.nctigba.datastudio.constants.CommonConstants.RESULT;
import static com.nctigba.datastudio.constants.CommonConstants.STATEMENT;
import static com.nctigba.datastudio.constants.CommonConstants.SUCCESS;
import static com.nctigba.datastudio.constants.SqlConstants.INFO_CODE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.PARENTHESES_SEMICOLON;
import static com.nctigba.datastudio.constants.SqlConstants.STEP_SQL;
import static com.nctigba.datastudio.enums.MessageEnum.newWindow;

/**
 * step in
 */
@Slf4j
@Service("stepIn")
public class StepInImpl implements OperationInterface {
    @Autowired
    private SingleStepImpl singleStep;

    @Override
    public void operate(WebSocketServer webSocketServer, Object obj) throws Exception {
        log.info("stepIn obj is: " + obj);
        PublicParamReq paramReq = (PublicParamReq) obj;
        String windowName = paramReq.getOldWindowName();
        if (StringUtils.isEmpty(windowName)) {
            windowName = paramReq.getWindowName();
        }
        OperateStatusDO operateStatus = webSocketServer.getOperateStatus(windowName);
        if (!operateStatus.isStepIn()) {
            return;
        }
        Statement stat = (Statement) webSocketServer.getParamMap(windowName).get(STATEMENT);
        if (stat == null) {
            return;
        }

        String oid = (String) webSocketServer.getParamMap(windowName).get(OID);
        log.info("stepIn oid is: " + oid);
        ResultSet resultSet = stat.executeQuery(STEP_SQL);
        String newOid = "";
        while (resultSet.next()) {
            newOid = resultSet.getString(FUNC_OID);
            log.info("stepIn newOid is: " + newOid);
        }

        if (oid.equals(newOid)) {
            singleStep.showDebugInfo(webSocketServer, paramReq);
        } else {
            Map<String, String> map = new HashMap<>();
            ResultSet infoCodeResult = stat.executeQuery(INFO_CODE_SQL + newOid + PARENTHESES_SEMICOLON);
            StringBuilder sb = new StringBuilder();
            while (infoCodeResult.next()) {
                sb.append(infoCodeResult.getString("query")).append("\n");
            }
            map.put(RESULT, DebugUtils.prepareName(sb.toString()));
            webSocketServer.sendMessage(windowName, newWindow, SUCCESS, map);
        }
    }

    @Override
    public Object formatJson(String str) {
        return JSON.parseObject(str, PublicParamReq.class);
    }
}
