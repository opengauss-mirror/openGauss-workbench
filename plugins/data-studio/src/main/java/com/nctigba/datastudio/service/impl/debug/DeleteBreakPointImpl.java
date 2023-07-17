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
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.BREAK_POINT;
import static com.nctigba.datastudio.constants.CommonConstants.CAN_BREAK;
import static com.nctigba.datastudio.constants.CommonConstants.DIFFER;
import static com.nctigba.datastudio.constants.CommonConstants.OID;
import static com.nctigba.datastudio.constants.CommonConstants.STATEMENT;
import static com.nctigba.datastudio.constants.CommonConstants.SUCCESS;
import static com.nctigba.datastudio.constants.SqlConstants.DELETE_BREAKPOINT_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.INFO_BREAKPOINT_PRE;
import static com.nctigba.datastudio.constants.SqlConstants.INFO_BREAKPOINT_SQL;
import static com.nctigba.datastudio.enums.MessageEnum.BREAKPOINT;

/**
 * DeleteBreakPointImpl
 *
 * @since 2023-6-26
 */
@Slf4j
@Service("deleteBreakPoint")
public class DeleteBreakPointImpl implements OperationInterface {
    @Override
    public void operate(WebSocketServer webSocketServer, Object obj) throws SQLException, IOException {
        PublicParamReq paramReq = DebugUtils.changeParamType(obj);
        log.info("deleteBreakPoint paramReq: " + paramReq);

        String windowName = paramReq.getWindowName();
        Map<Integer, String> breakPointMap = DebugUtils.changeParamType(webSocketServer, windowName, BREAK_POINT);
        log.info("deleteBreakPoint breakPointMap: " + breakPointMap);

        String rootWindowName = paramReq.getRootWindowName();
        Statement stat = DebugUtils.changeParamType(webSocketServer, rootWindowName, STATEMENT);
        if (stat == null) {
            return;
        }

        int line = paramReq.getLine();
        int differ = DebugUtils.changeParamType(webSocketServer, windowName, DIFFER);
        List<Integer> list = DebugUtils.changeParamType(webSocketServer, windowName, CAN_BREAK);
        log.info("deleteBreakPoint list: " + list);
        if (list.contains(line - differ)) {
            int no = Integer.parseInt(breakPointMap.get(line));
            stat.execute(String.format(DELETE_BREAKPOINT_SQL, no));
            Iterator<Integer> iterator = breakPointMap.keySet().iterator();
            log.info("deleteBreakPoint iterator: " + iterator);
            while (iterator.hasNext()) {
                if (line == iterator.next()) {
                    iterator.remove();
                }
            }
            webSocketServer.setParamMap(windowName, BREAK_POINT, breakPointMap);
        }

        if (!paramReq.isCloseWindow()) {
            ResultSet bpResult = stat.executeQuery(INFO_BREAKPOINT_PRE + differ + INFO_BREAKPOINT_SQL);
            String oid = DebugUtils.changeParamType(webSocketServer, windowName, OID);
            webSocketServer.sendMessage(windowName, BREAKPOINT, SUCCESS, DebugUtils.parseBreakPoint(bpResult, oid));
        }
    }

    @Override
    public Object formatJson(String str) {
        return JSON.parseObject(str, PublicParamReq.class);
    }
}
