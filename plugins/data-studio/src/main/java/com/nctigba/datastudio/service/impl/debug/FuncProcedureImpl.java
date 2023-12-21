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
 *  FuncProcedureImpl.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/service/impl/debug/FuncProcedureImpl.java
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
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.DEFINITION;
import static com.nctigba.datastudio.constants.CommonConstants.OID;
import static com.nctigba.datastudio.constants.CommonConstants.PRO_KIND;
import static com.nctigba.datastudio.constants.CommonConstants.RESULT;
import static com.nctigba.datastudio.constants.CommonConstants.STATEMENT;
import static com.nctigba.datastudio.constants.CommonConstants.SUCCESS;
import static com.nctigba.datastudio.constants.CommonConstants.TYPE;
import static com.nctigba.datastudio.constants.SqlConstants.PROC_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.QUERY_DEF_SQL;
import static com.nctigba.datastudio.enums.MessageEnum.VIEW;

/**
 * FuncProcedureImpl
 *
 * @since 2023-6-26
 */
@Slf4j
@Service("funcProcedure")
public class FuncProcedureImpl implements OperationInterface {
    @Override
    public void operate(WebSocketServer webSocketServer, Object obj) throws SQLException, IOException {
        PublicParamQuery paramReq = DebugUtils.changeParamType(obj);
        log.info("funcProcedure paramReq: " + paramReq);

        String rootWindowName = paramReq.getRootWindowName();
        String oldWindowName = paramReq.getOldWindowName();
        String windowName = paramReq.getWindowName();
        OperateStatusDO operateStatus = webSocketServer.getOperateStatus(windowName);
        if (paramReq.isInPackage()) {
            operateStatus.enableStartDebugPackage();
        } else {
            operateStatus.enableStartDebug();
        }
        webSocketServer.setOperateStatus(windowName, operateStatus);

        Statement statement;
        if (StringUtils.isEmpty(oldWindowName)) {
            statement = webSocketServer.getStatement(rootWindowName);
            if (statement == null || statement.isClosed()) {
                Connection connection = webSocketServer.getConnection(rootWindowName);
                if (connection == null || connection.isClosed()) {
                    connection = webSocketServer.createConnection(paramReq.getUuid(), windowName);
                }
                statement = connection.createStatement();
                webSocketServer.setStatement(rootWindowName, statement);
            }
        } else {
            statement = DebugUtils.changeParamType(webSocketServer, rootWindowName, STATEMENT);
        }

        String oid = paramReq.getOid();
        webSocketServer.setParamMap(windowName, OID, oid);
        log.info("funcProcedure oid: " + oid);
        String definition = Strings.EMPTY;
        if (statement == null) {
            return;
        }
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
            log.info("funcProcedure definition: " + definition);
        }

        try (
                ResultSet resultSet = statement.executeQuery(String.format(PROC_SQL, oid))
        ) {
            while (resultSet.next()) {
                webSocketServer.setParamMap(windowName, TYPE, resultSet.getString(PRO_KIND));
            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put(RESULT, definition);
        webSocketServer.sendMessage(windowName, VIEW, SUCCESS, map);
        webSocketServer.setParamMap(rootWindowName, oid, windowName);
    }

    @Override
    public Object formatJson(String str) {
        return JSON.parseObject(str, PublicParamQuery.class);
    }
}
