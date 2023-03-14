package com.nctigba.datastudio.service.impl.debug;

import com.alibaba.fastjson.JSON;
import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.model.PublicParamReq;
import com.nctigba.datastudio.service.OperationInterface;
import com.nctigba.datastudio.util.DebugUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.BREAK_POINT;
import static com.nctigba.datastudio.constants.CommonConstants.DIFFER;
import static com.nctigba.datastudio.constants.CommonConstants.OID;
import static com.nctigba.datastudio.constants.CommonConstants.STATEMENT;
import static com.nctigba.datastudio.constants.CommonConstants.SUCCESS;
import static com.nctigba.datastudio.constants.SqlConstants.DISABLE_BREAKPOINT_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.INFO_BREAKPOINT_PRE;
import static com.nctigba.datastudio.constants.SqlConstants.INFO_BREAKPOINT_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.PARENTHESES_SEMICOLON;
import static com.nctigba.datastudio.enums.MessageEnum.breakPoint;

/**
 * disable break point
 */
@Slf4j
@Service("disableBreakPoint")
public class DisableBreakPointImpl implements OperationInterface {
    @Override
    public void operate(WebSocketServer webSocketServer, Object obj) throws Exception {
        log.info("disableBreakPoint obj is: " + obj);
        PublicParamReq paramReq = (PublicParamReq) obj;
        String windowName = paramReq.getWindowName();
        int line = paramReq.getLine();
        int differ = (int) webSocketServer.getParamMap(windowName).get(DIFFER);
        Map<Integer, String> breakPointMap = (Map<Integer, String>) webSocketServer.getParamMap(windowName).get(BREAK_POINT);
        log.info("disableBreakPoint breakPointMap is: " + breakPointMap);

        String name = paramReq.getOldWindowName();
        if (StringUtils.isEmpty(name)) {
            name = windowName;
        }
        Statement stat = (Statement) webSocketServer.getParamMap(name).get(STATEMENT);
        if (stat == null) {
            return;
        }

        if (webSocketServer.checkLine(windowName, line)) {
            String no = breakPointMap.get(line);
            log.info("disableBreakPoint no is: " + no);
            stat.execute(DISABLE_BREAKPOINT_SQL + no + PARENTHESES_SEMICOLON);
        }

        ResultSet bpResult = stat.executeQuery(INFO_BREAKPOINT_PRE + differ + INFO_BREAKPOINT_SQL);
        String oid = (String) webSocketServer.getParamMap(windowName).get(OID);
        webSocketServer.sendMessage(windowName, breakPoint, SUCCESS, DebugUtils.parseBeakPoint(bpResult, oid));
    }

    @Override
    public Object formatJson(String str) {
        return JSON.parseObject(str, PublicParamReq.class);
    }
}
