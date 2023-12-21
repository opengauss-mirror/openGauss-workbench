/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  DeleteBreakPointImpl.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/service/impl/debug/DeleteBreakPointImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.service.impl.debug;

import com.alibaba.fastjson.JSON;
import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.model.query.PublicParamQuery;
import com.nctigba.datastudio.service.OperationInterface;
import com.nctigba.datastudio.utils.DebugUtils;
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
        PublicParamQuery paramReq = DebugUtils.changeParamType(obj);
        log.info("deleteBreakPoint paramReq: " + paramReq);

        String windowName = paramReq.getWindowName();
        Map<Integer, String> breakPointMap = DebugUtils.changeParamType(webSocketServer, windowName, BREAK_POINT);
        log.info("deleteBreakPoint breakPointMap: " + breakPointMap);

        String rootWindowName = paramReq.getRootWindowName();
        Statement stat = DebugUtils.changeParamType(webSocketServer, rootWindowName, STATEMENT);
        if (stat == null) {
            return;
        }

        List<Integer> breakPoints = paramReq.getBreakPoints();
        int differ = DebugUtils.changeParamType(webSocketServer, windowName, DIFFER);
        List<Integer> list = DebugUtils.changeParamType(webSocketServer, windowName, CAN_BREAK);
        log.info("deleteBreakPoint list: " + list);
        for (Integer line : breakPoints) {
            if (list.contains(line - differ)) {
                int no = Integer.parseInt(breakPointMap.get(line));
                stat.execute(String.format(DELETE_BREAKPOINT_SQL, no));
                deleteBreakPoint(breakPointMap, line);
                webSocketServer.setParamMap(windowName, BREAK_POINT, breakPointMap);
            }
        }

        if (!paramReq.isCloseWindow()) {
            ResultSet bpResult = stat.executeQuery(INFO_BREAKPOINT_PRE + differ + INFO_BREAKPOINT_SQL);
            String oid = DebugUtils.changeParamType(webSocketServer, windowName, OID);
            webSocketServer.sendMessage(windowName, BREAKPOINT, SUCCESS, DebugUtils.parseBreakPoint(bpResult, oid));
        }
    }

    private void deleteBreakPoint(Map<Integer, String> breakPointMap, Integer line) {
        Iterator<Integer> iterator = breakPointMap.keySet().iterator();
        log.info("deleteBreakPoint iterator: " + iterator);
        while (iterator.hasNext()) {
            if (line.equals(iterator.next())) {
                iterator.remove();
            }
        }
    }

    @Override
    public Object formatJson(String str) {
        return JSON.parseObject(str, PublicParamQuery.class);
    }
}
