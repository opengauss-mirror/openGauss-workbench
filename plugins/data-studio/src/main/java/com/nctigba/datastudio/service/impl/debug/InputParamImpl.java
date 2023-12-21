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
 *  InputParamImpl.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/service/impl/debug/InputParamImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.service.impl.debug;

import com.alibaba.fastjson.JSON;
import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.model.query.PublicParamQuery;
import com.nctigba.datastudio.model.entity.OperateStatusDO;
import com.nctigba.datastudio.service.OperationInterface;
import com.nctigba.datastudio.utils.DebugUtils;
import com.nctigba.datastudio.utils.LocaleStringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.CONNECTION;
import static com.nctigba.datastudio.constants.CommonConstants.DIFFER;
import static com.nctigba.datastudio.constants.CommonConstants.NODE_NAME;
import static com.nctigba.datastudio.constants.CommonConstants.OID;
import static com.nctigba.datastudio.constants.CommonConstants.PORT;
import static com.nctigba.datastudio.constants.CommonConstants.RESULT;
import static com.nctigba.datastudio.constants.CommonConstants.STATEMENT;
import static com.nctigba.datastudio.constants.CommonConstants.SUCCESS;
import static com.nctigba.datastudio.constants.SqlConstants.ATTACH_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.BACKTRACE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.BACKTRACE_SQL_PRE;
import static com.nctigba.datastudio.constants.SqlConstants.INFO_BREAKPOINT_PRE;
import static com.nctigba.datastudio.constants.SqlConstants.INFO_BREAKPOINT_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.INFO_LOCALS_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.TURN_ON_SQL;
import static com.nctigba.datastudio.enums.MessageEnum.BREAKPOINT;
import static com.nctigba.datastudio.enums.MessageEnum.STACK;
import static com.nctigba.datastudio.enums.MessageEnum.TABLE;
import static com.nctigba.datastudio.enums.MessageEnum.TEXT;
import static com.nctigba.datastudio.enums.MessageEnum.VARIABLE;

/**
 * InputParamImpl
 *
 * @since 2023-6-26
 */
@Slf4j
@Service("inputParam")
public class InputParamImpl implements OperationInterface {
    @Autowired
    private AsyncHelper asyncHelper;

    @Autowired
    private AddBreakPointImpl addBreakPoint;

    @Override
    public void operate(WebSocketServer webSocketServer, Object obj) throws SQLException, IOException {
        PublicParamQuery paramReq = DebugUtils.changeParamType(obj);
        log.info("inputParam paramReq: " + paramReq);
        String windowName = paramReq.getWindowName();

        OperateStatusDO operateStatus = webSocketServer.getOperateStatus(windowName);
        if (!operateStatus.isDebug()) {
            Statement statement = webSocketServer.getStatement(windowName);
            ResultSet resultSet = statement.executeQuery(DebugUtils.prepareSql(paramReq));
            Map<String, Object> map = DebugUtils.parseResultSet(resultSet);
            log.info("inputParam result map: " + map);

            List<List<Object>> list = (List<List<Object>>) map.get(RESULT);
            if (list.size() == 1) {
                if (list.get(0).size() == 1) {
                    webSocketServer.sendMessage(windowName, TEXT, SUCCESS, map);
                } else {
                    Map<String, String> messageMap = new HashMap<>();
                    messageMap.put(RESULT, LocaleStringUtils.transLanguageWs("1008", webSocketServer));
                    webSocketServer.sendMessage(windowName, TEXT, SUCCESS, messageMap);
                    webSocketServer.sendMessage(windowName, TABLE, SUCCESS, map);
                }
            } else {
                webSocketServer.sendMessage(windowName, TEXT, SUCCESS, map);
            }
            statement.close();
            webSocketServer.setStatement(windowName, null);
        } else {
            debugOperate(webSocketServer, paramReq);
        }
    }

    private void debugOperate(
            WebSocketServer webSocketServer, PublicParamQuery paramReq) throws SQLException, IOException {
        String windowName = paramReq.getWindowName();
        Statement statement = webSocketServer.getStatement(windowName);

        String nodeName = DebugUtils.changeParamType(webSocketServer, windowName, NODE_NAME);
        String port = DebugUtils.changeParamType(webSocketServer, windowName, PORT);
        String oid = paramReq.getOid();

        try (
                ResultSet turnNoResult = statement.executeQuery(String.format(TURN_ON_SQL, oid))
        ) {
            while (turnNoResult.next()) {
                nodeName = turnNoResult.getString(NODE_NAME);
                port = turnNoResult.getString(PORT);
                log.info("inputParam nodeName and port is: " + nodeName + "---" + port);
                webSocketServer.setParamMap(windowName, NODE_NAME, nodeName);
                webSocketServer.setParamMap(windowName, PORT, port);
            }
        } catch (SQLException e) {
            log.info(e.getMessage());
        } finally {
            asyncHelper.task(webSocketServer, paramReq);
            Connection conn = webSocketServer.createConnection(paramReq.getUuid(), windowName);
            Statement statNew = conn.createStatement();
            webSocketServer.setParamMap(windowName, OID, oid);
            webSocketServer.setParamMap(windowName, CONNECTION, conn);
            webSocketServer.setParamMap(windowName, STATEMENT, statNew);

            statNew.execute(String.format(ATTACH_SQL, nodeName, port));
            List<Integer> list = DebugUtils.getAvailableBreakPoints(paramReq, webSocketServer);
            List<Integer> breakPoints = paramReq.getBreakPoints();
            int differ = DebugUtils.changeParamType(webSocketServer, windowName, DIFFER);
            List<Integer> lineList = new ArrayList<>();
            if (!CollectionUtils.isEmpty(breakPoints)) {
                for (Integer i : breakPoints) {
                    if (list.contains(i - differ)) {
                        lineList.add(i);
                    }
                }
                paramReq.setBreakPoints(lineList);
                addBreakPoint.operate(webSocketServer, paramReq);
            }

            ResultSet stackResult = statNew.executeQuery(BACKTRACE_SQL_PRE + differ + BACKTRACE_SQL);
            webSocketServer.sendMessage(windowName, STACK, SUCCESS, DebugUtils.parseResultSet(stackResult));

            ResultSet bpResult = statNew.executeQuery(INFO_BREAKPOINT_PRE + differ + INFO_BREAKPOINT_SQL);
            webSocketServer.sendMessage(windowName, BREAKPOINT, SUCCESS, DebugUtils.parseBreakPoint(bpResult, oid));

            Map<String, Object> variableMap = DebugUtils.parseVariable(statNew.executeQuery(INFO_LOCALS_SQL));
            Map<String, Object> paramMap = DebugUtils.addMapParam(variableMap, webSocketServer, paramReq);
            webSocketServer.sendMessage(windowName, VARIABLE, SUCCESS, paramMap);
        }
    }

    @Override
    public Object formatJson(String str) {
        return JSON.parseObject(str, PublicParamQuery.class);
    }
}
