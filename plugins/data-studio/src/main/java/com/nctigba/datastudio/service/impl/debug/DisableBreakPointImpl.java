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
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.BREAK_POINT;
import static com.nctigba.datastudio.constants.CommonConstants.CAN_BREAK;
import static com.nctigba.datastudio.constants.CommonConstants.DIFFER;
import static com.nctigba.datastudio.constants.CommonConstants.OID;
import static com.nctigba.datastudio.constants.CommonConstants.STATEMENT;
import static com.nctigba.datastudio.constants.CommonConstants.SUCCESS;
import static com.nctigba.datastudio.constants.SqlConstants.DISABLE_BREAKPOINT_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.INFO_BREAKPOINT_PRE;
import static com.nctigba.datastudio.constants.SqlConstants.INFO_BREAKPOINT_SQL;
import static com.nctigba.datastudio.enums.MessageEnum.BREAKPOINT;

/**
 * DisableBreakPointImpl
 *
 * @since 2023-6-26
 */
@Slf4j
@Service("disableBreakPoint")
public class DisableBreakPointImpl implements OperationInterface {
    @Override
    public void operate(WebSocketServer webSocketServer, Object obj) throws SQLException, IOException {
        PublicParamReq paramReq = DebugUtils.changeParamType(obj);
        log.info("disableBreakPoint paramReq: " + paramReq);

        String windowName = paramReq.getWindowName();
        Map<Integer, String> breakPointMap = DebugUtils.changeParamType(webSocketServer, windowName, BREAK_POINT);
        log.info("disableBreakPoint breakPointMap: " + breakPointMap);

        String rootWindowName = paramReq.getRootWindowName();
        Statement stat = DebugUtils.changeParamType(webSocketServer, rootWindowName, STATEMENT);
        if (stat == null) {
            return;
        }

        int line = paramReq.getLine();
        int differ = DebugUtils.changeParamType(webSocketServer, windowName, DIFFER);
        List<Integer> list = DebugUtils.changeParamType(webSocketServer, windowName, CAN_BREAK);
        log.info("disableBreakPoint list: " + list);
        if (list.contains(line - differ)) {
            String no = breakPointMap.get(line);
            log.info("disableBreakPoint no: " + no);
            stat.execute(String.format(DISABLE_BREAKPOINT_SQL, no));
        }

        ResultSet bpResult = stat.executeQuery(INFO_BREAKPOINT_PRE + differ + INFO_BREAKPOINT_SQL);
        String oid = DebugUtils.changeParamType(webSocketServer, windowName, OID);
        webSocketServer.sendMessage(windowName, BREAKPOINT, SUCCESS, DebugUtils.parseBreakPoint(bpResult, oid));
    }

    @Override
    public Object formatJson(String str) {
        return JSON.parseObject(str, PublicParamReq.class);
    }
}
