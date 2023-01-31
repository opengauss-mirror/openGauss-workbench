package com.nctigba.datastudio.service.impl.debug;

import com.alibaba.fastjson.JSON;
import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.model.PublicParamReq;
import com.nctigba.datastudio.service.OperationInterface;
import com.nctigba.datastudio.util.DebugUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.BREAK_POINT;
import static com.nctigba.datastudio.constants.CommonConstants.DIFFER;
import static com.nctigba.datastudio.constants.CommonConstants.STATEMENT;
import static com.nctigba.datastudio.constants.CommonConstants.SUCCESS;
import static com.nctigba.datastudio.constants.SqlConstants.DELETE_BREAKPOINT_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.INFO_BREAKPOINT_PRE;
import static com.nctigba.datastudio.constants.SqlConstants.INFO_BREAKPOINT_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.PARENTHESES_SEMICOLON;
import static com.nctigba.datastudio.enums.MessageEnum.breakPoint;

/**
 * delete break point
 */
@Slf4j
@Service("deleteBreakPoint")
public class DeleteBreakPointImpl implements OperationInterface {
    @Override
    public void operate(WebSocketServer webSocketServer, Object obj) throws Exception {
        log.info("deleteBreakPoint obj is: " + obj);
        PublicParamReq paramReq = (PublicParamReq) obj;
        String windowName = paramReq.getWindowName();
        int line = paramReq.getLine();
        int differ = (int) webSocketServer.getParamMap(windowName).get(DIFFER);
        Map<Integer, String> breakPointMap = (Map<Integer, String>) webSocketServer.getParamMap(windowName).get(BREAK_POINT);
        log.info("deleteBreakPoint breakPointMap is: " + breakPointMap);

        Statement stat = (Statement) webSocketServer.getParamMap(windowName).get(STATEMENT);
        if (stat == null) {
            return;
        }

        if (webSocketServer.checkLine(windowName, line)) {
            Integer no = Integer.valueOf(breakPointMap.get(line));
            stat.execute(DELETE_BREAKPOINT_SQL + no + PARENTHESES_SEMICOLON);
            Iterator<Integer> iterator = breakPointMap.keySet().iterator();
            log.info("deleteBreakPoint iterator is: " + iterator);
            while (iterator.hasNext()) {
                if (line == iterator.next()) {
                    iterator.remove();
                }
            }
            webSocketServer.setParamMap(windowName, BREAK_POINT, breakPointMap);
        }

        ResultSet bpResult = stat.executeQuery(INFO_BREAKPOINT_PRE + differ + INFO_BREAKPOINT_SQL);
        webSocketServer.sendMessage(windowName, breakPoint, SUCCESS, DebugUtils.parseResultSet(bpResult));
    }

    @Override
    public Object formatJson(String str) {
        return JSON.parseObject(str, PublicParamReq.class);
    }
}
