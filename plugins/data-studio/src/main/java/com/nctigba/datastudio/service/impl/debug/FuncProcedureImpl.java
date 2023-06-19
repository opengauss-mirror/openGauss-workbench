/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service.impl.debug;

import com.alibaba.fastjson.JSON;
import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.model.PublicParamReq;
import com.nctigba.datastudio.model.entity.OperateStatusDO;
import com.nctigba.datastudio.service.OperationInterface;
import com.nctigba.datastudio.util.DebugUtils;
import com.nctigba.datastudio.util.LocaleString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
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
import static com.nctigba.datastudio.enums.MessageEnum.view;

/**
 * show function/procedure
 */
@Slf4j
@Service("funcProcedure")
public class FuncProcedureImpl implements OperationInterface {
    @Autowired
    private StopDebugImpl stopDebug;

    @Override
    public void operate(WebSocketServer webSocketServer, Object obj) throws Exception {
        PublicParamReq paramReq = (PublicParamReq) obj;
        log.info("funcProcedure paramReq: " + paramReq);

        String rootWindowName = paramReq.getRootWindowName();
        String oldWindowName = paramReq.getOldWindowName();
        String windowName = paramReq.getWindowName();
        OperateStatusDO operateStatus = webSocketServer.getOperateStatus(windowName);
        operateStatus.enableStartDebug();
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
            statement = (Statement) webSocketServer.getParamMap(rootWindowName).get(STATEMENT);
        }

        String oid = paramReq.getOid();
        webSocketServer.setParamMap(windowName, OID, oid);
        log.info("funcProcedure oid: " + oid);
        String definition = Strings.EMPTY;
        try (
                ResultSet defResultSet = statement.executeQuery(String.format(QUERY_DEF_SQL, oid))
        ) {
            while (defResultSet.next()) {
                definition = DebugUtils.sqlHandleAfter(defResultSet.getString(DEFINITION));
                if (StringUtils.isEmpty(definition)) {
                    throw new CustomException(LocaleString.transLanguageWs("2015", webSocketServer));
                }
            }
            log.info("funcProcedure definition: " + definition);
        } catch (Exception e) {
            log.info(e.toString());
            throw new RuntimeException(e);
        }

        try (
                ResultSet resultSet = statement.executeQuery(String.format(PROC_SQL, oid))
        ) {
            while (resultSet.next()) {
                webSocketServer.setParamMap(windowName, TYPE, resultSet.getString(PRO_KIND));
            }
        } catch (Exception e) {
            log.info(e.toString());
            throw new RuntimeException(e);
        }
        Map<String, Object> map = new HashMap<>();
        map.put(RESULT, definition);
        webSocketServer.sendMessage(windowName, view, SUCCESS, map);
        webSocketServer.setParamMap(rootWindowName, oid, windowName);
    }

    @Override
    public Object formatJson(String str) {
        return JSON.parseObject(str, PublicParamReq.class);
    }
}
