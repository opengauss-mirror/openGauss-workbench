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
 *  StepOutImpl.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/service/impl/debug/StepOutImpl.java
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
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.FUNC_OID;
import static com.nctigba.datastudio.constants.CommonConstants.OID;
import static com.nctigba.datastudio.constants.CommonConstants.RESULT;
import static com.nctigba.datastudio.constants.CommonConstants.STATEMENT;
import static com.nctigba.datastudio.constants.CommonConstants.SUCCESS;
import static com.nctigba.datastudio.constants.SqlConstants.FINISH_SQL;
import static com.nctigba.datastudio.enums.MessageEnum.CLOSE_WINDOW;
import static com.nctigba.datastudio.enums.MessageEnum.SWITCH_WINDOW;

/**
 * StepOutImpl
 *
 * @since 2023-6-26
 */
@Slf4j
@Service("stepOut")
public class StepOutImpl implements OperationInterface {
    @Autowired
    private SingleStepImpl singleStep;

    @Autowired
    private DeleteBreakPointImpl deleteBreakPoint;

    @Override
    public void operate(WebSocketServer webSocketServer, Object obj) throws SQLException, IOException {
        PublicParamQuery paramReq = DebugUtils.changeParamType(obj);
        log.info("stepOut paramReq: " + paramReq);
        String rootWindowName = paramReq.getRootWindowName();
        Statement stat = DebugUtils.changeParamType(webSocketServer, rootWindowName, STATEMENT);
        if (stat == null) {
            return;
        }

        String windowName = paramReq.getWindowName();
        String oid = DebugUtils.changeParamType(webSocketServer, windowName, OID);
        log.info("stepOut oid: " + oid);

        String newOid = Strings.EMPTY;
        try (
                ResultSet resultSet = stat.executeQuery(FINISH_SQL)
        ) {
            if (resultSet.next()) {
                newOid = resultSet.getString(FUNC_OID);
                log.info("stepOut newOid: " + newOid);
            }
        }
        try {
            List<String> oidList = DebugUtils.getOidList(webSocketServer, rootWindowName);
            if (oid.equals(newOid)) {
                singleStep.showDebugInfo(webSocketServer, stat, paramReq);
                return;
            }
            if (oidList.contains(oid)) {
                Map<String, String> map = new HashMap<>();
                map.put(RESULT, newOid);
                webSocketServer.sendMessage(windowName, SWITCH_WINDOW, SUCCESS, map);
                DebugUtils.disableButton(webSocketServer, windowName);
            } else {
                webSocketServer.sendMessage(windowName, CLOSE_WINDOW, SUCCESS, null);
                Map<String, Object> paramMap = webSocketServer.getParamMap(rootWindowName);
                paramMap.keySet().removeIf(oid::equals);
            }
            String name = DebugUtils.getOldWindowName(webSocketServer, rootWindowName, newOid);
            DebugUtils.enableButton(webSocketServer, name);
            paramReq.setCloseWindow(true);
            paramReq.setOldWindowName(name);
            singleStep.showDebugInfo(webSocketServer, stat, paramReq);
        } catch (SQLException | IOException e) {
            log.info(e.getMessage());
        }
    }

    @Override
    public Object formatJson(String str) {
        return JSON.parseObject(str, PublicParamQuery.class);
    }
}
