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
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.BREAK_POINT;
import static com.nctigba.datastudio.constants.CommonConstants.BREAK_POINT_NO;
import static com.nctigba.datastudio.constants.CommonConstants.CAN_BREAK;
import static com.nctigba.datastudio.constants.CommonConstants.DIFFER;
import static com.nctigba.datastudio.constants.CommonConstants.OID;
import static com.nctigba.datastudio.constants.CommonConstants.STATEMENT;
import static com.nctigba.datastudio.constants.CommonConstants.SUCCESS;
import static com.nctigba.datastudio.constants.SqlConstants.ADD_BREAKPOINT_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.INFO_BREAKPOINT_PRE;
import static com.nctigba.datastudio.constants.SqlConstants.INFO_BREAKPOINT_SQL;
import static com.nctigba.datastudio.enums.MessageEnum.breakPoint;

/**
 * add break point
 */
@Slf4j
@Service("addBreakPoint")
public class AddBreakPointImpl implements OperationInterface {
    @Override
    public void operate(WebSocketServer webSocketServer, Object obj) throws Exception {
        PublicParamReq paramReq = (PublicParamReq) obj;
        log.info("AddBreakPointImpl paramReq: " + paramReq);

        String windowName = paramReq.getWindowName();
        Map<Integer, String> breakPointMap = (Map<Integer, String>) webSocketServer.getParamMap(windowName).get(
                BREAK_POINT);
        if (CollectionUtils.isEmpty(breakPointMap)) {
            breakPointMap = new HashMap<>();
        }
        log.info("AddBreakPointImpl old breakPointMap: " + breakPointMap);

        String rootWindowName = paramReq.getRootWindowName();
        Statement stat = (Statement) webSocketServer.getParamMap(rootWindowName).get(STATEMENT);
        if (stat == null) {
            return;
        }

        int line = paramReq.getLine();
        int differ = (int) webSocketServer.getParamMap(windowName).get(DIFFER);
        String oid = (String) webSocketServer.getParamMap(windowName).get(OID);
        List<Integer> list = (List<Integer>) webSocketServer.getParamMap(windowName).get(CAN_BREAK);
        if (list.contains(line - differ)) {
            try (
                    ResultSet resultSet = stat.executeQuery(String.format(ADD_BREAKPOINT_SQL, oid, (line - differ)))
            ) {
                while (resultSet.next()) {
                    String no = resultSet.getString(BREAK_POINT_NO);
                    breakPointMap.put(line, no);
                }
            } catch (Exception e) {
                log.info(e.toString());
                throw new RuntimeException(e);
            }
        }
        webSocketServer.setParamMap(windowName, BREAK_POINT, breakPointMap);
        log.info("AddBreakPointImpl new breakPointMap: " + breakPointMap);

        ResultSet bpResult = stat.executeQuery(INFO_BREAKPOINT_PRE + differ + INFO_BREAKPOINT_SQL);
        webSocketServer.sendMessage(windowName, breakPoint, SUCCESS, DebugUtils.parseBreakPoint(bpResult, oid));
    }

    @Override
    public Object formatJson(String str) {
        return JSON.parseObject(str, PublicParamReq.class);
    }
}
