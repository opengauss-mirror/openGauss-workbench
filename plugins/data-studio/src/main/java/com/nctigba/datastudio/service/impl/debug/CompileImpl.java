/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service.impl.debug;

import com.alibaba.fastjson.JSON;
import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.model.PublicParamReq;
import com.nctigba.datastudio.service.OperationInterface;
import com.nctigba.datastudio.util.DebugUtils;
import com.nctigba.datastudio.util.LocaleString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.OID;
import static com.nctigba.datastudio.constants.CommonConstants.RESULT;
import static com.nctigba.datastudio.constants.CommonConstants.SUCCESS;
import static com.nctigba.datastudio.constants.SqlConstants.QUERY_OID_SQL;
import static com.nctigba.datastudio.enums.MessageEnum.MESSAGE;
import static com.nctigba.datastudio.enums.MessageEnum.NEW_FILE;
import static com.nctigba.datastudio.enums.MessageEnum.TEXT;

/**
 * CompileImpl
 *
 * @since 2023-6-26
 */
@Slf4j
@Service("compile")
public class CompileImpl implements OperationInterface {
    @Override
    public void operate(WebSocketServer webSocketServer, Object obj) throws SQLException, IOException {
        PublicParamReq paramReq = DebugUtils.changeParamType(obj);
        log.info("compile paramReq: " + paramReq);

        String rootWindowName = paramReq.getRootWindowName();
        Statement statement = webSocketServer.getStatement(rootWindowName);
        if (statement == null) {
            statement = webSocketServer.getConnection(rootWindowName).createStatement();
            webSocketServer.setStatement(rootWindowName, statement);
        }

        String sql = paramReq.getSql();
        String schema = DebugUtils.getSchemaBySql(sql);
        String name = DebugUtils.getNameBySql(sql);
        if (StringUtils.isEmpty(schema)) {
            schema = paramReq.getSchema();
        }

        List<String> oldOidList = new ArrayList<>();
        try (
                ResultSet resultSetOld = statement.executeQuery(String.format(QUERY_OID_SQL, name, schema))
        ) {
            while (resultSetOld.next()) {
                oldOidList.add(resultSetOld.getString(OID));
            }
        }

        log.info("compile oldOidList: " + oldOidList);
        statement.execute(sql);
        List<String> newOidList = new ArrayList<>();

        try (
                ResultSet resultSetNew = statement.executeQuery(String.format(QUERY_OID_SQL, name, schema))
        ) {
            while (resultSetNew.next()) {
                newOidList.add(resultSetNew.getString(OID));
            }
        }
        log.info("compile newOidList: " + newOidList);

        String newOid = Strings.EMPTY;
        for (String id : newOidList) {
            if (!oldOidList.contains(id)) {
                newOid = id;
            }
        }
        log.info("compile newOid: " + newOid);
        String windowName = paramReq.getRootWindowName();
        if (StringUtils.isEmpty(newOid)) {
            webSocketServer.sendMessage(windowName, TEXT, LocaleString.transLanguageWs("1002", webSocketServer), null);
            return;
        }

        Map<String, String> messageMap = new HashMap<>();
        messageMap.put(RESULT, LocaleString.transLanguageWs("1005", webSocketServer));
        webSocketServer.sendMessage(windowName, MESSAGE, SUCCESS, messageMap);
        paramReq.setOid(newOid);
        Map<String, Map<String, String>> map = DebugUtils.getResultMap(webSocketServer, paramReq);
        webSocketServer.sendMessage(windowName, NEW_FILE, SUCCESS, map);
    }

    @Override
    public Object formatJson(String str) {
        return JSON.parseObject(str, PublicParamReq.class);
    }
}
