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
 *  StartDebugImpl.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/service/impl/debug/StartDebugImpl.java
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
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.DEFINITION;
import static com.nctigba.datastudio.constants.CommonConstants.FIVE_HUNDRED;
import static com.nctigba.datastudio.constants.CommonConstants.RESULT;
import static com.nctigba.datastudio.constants.CommonConstants.SUCCESS;
import static com.nctigba.datastudio.constants.SqlConstants.QUERY_DEF_SQL;
import static com.nctigba.datastudio.enums.MessageEnum.PARAM_WINDOW;
import static com.nctigba.datastudio.enums.MessageEnum.WINDOW;

/**
 * StartDebugImpl
 *
 * @since 2023-6-26
 */
@Slf4j
@Service("startDebug")
public class StartDebugImpl implements OperationInterface {
    @Autowired
    private InputParamImpl inputParam;

    @Autowired
    private FuncProcedureImpl funcProcedure;

    @Override
    public void operate(WebSocketServer webSocketServer, Object obj) throws SQLException, IOException {
        PublicParamQuery paramReq = DebugUtils.changeParamType(obj);
        log.info("startDebug paramReq: " + paramReq);

        String rootWindowName = paramReq.getRootWindowName();
        String windowName = paramReq.getWindowName();
        Statement statement = webSocketServer.getStatement(rootWindowName);
        if (statement == null) {
            statement = webSocketServer.getConnection(rootWindowName).createStatement();
            webSocketServer.setStatement(rootWindowName, statement);
        }

        String oid = paramReq.getOid();
        String definition = Strings.EMPTY;
        try (
                ResultSet defResultSet = statement.executeQuery(String.format(QUERY_DEF_SQL, oid))
        ) {
            while (defResultSet.next()) {
                definition = defResultSet.getString(DEFINITION);
                if (StringUtils.isEmpty(definition)) {
                    throw new CustomException(LocaleStringUtils.transLanguageWs("2015", webSocketServer));
                } else {
                    definition = DebugUtils.sqlHandleAfter(definition);
                }
            }
            log.info("startDebug definition: " + definition);
        }

        if (!DebugUtils.replaceLine(paramReq.getSql()).equals(DebugUtils.replaceLine(definition))) {
            webSocketServer.sendMessage(windowName, WINDOW, FIVE_HUNDRED,
                    LocaleStringUtils.transLanguageWs("1007", webSocketServer), null);
            return;
        }

        OperateStatusDO operateStatus = webSocketServer.getOperateStatus(windowName);
        operateStatus.setDebug(true);
        operateStatus.enableStopDebug();
        webSocketServer.setOperateStatus(windowName, operateStatus);

        Map<String, List<Map<String, Object>>> map = new HashMap<>();
        List<Map<String, Object>> paramList = DebugUtils.getParamMap(webSocketServer, windowName, oid);
        log.info("startDebug paramList: " + paramList);
        map.put(RESULT, paramList);
        if (CollectionUtils.isEmpty(paramList)) {
            inputParam.operate(webSocketServer, paramReq);
        } else {
            webSocketServer.sendMessage(windowName, PARAM_WINDOW, SUCCESS, map);
        }
    }

    @Override
    public Object formatJson(String str) {
        return JSON.parseObject(str, PublicParamQuery.class);
    }
}
