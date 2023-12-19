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
 *  CompileImpl.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/service/impl/debug/CompileImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.service.impl.debug;

import com.alibaba.fastjson.JSON;
import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.model.query.PublicParamQuery;
import com.nctigba.datastudio.service.OperationInterface;
import com.nctigba.datastudio.utils.DebugUtils;
import com.nctigba.datastudio.utils.LocaleStringUtils;
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
        PublicParamQuery paramReq = DebugUtils.changeParamType(obj);
        log.info("compile paramReq: " + paramReq);

        String rootWindowName = paramReq.getRootWindowName();
        String windowName = paramReq.getRootWindowName();
        Statement statement = webSocketServer.getStatement(rootWindowName);
        if (statement == null) {
            statement = webSocketServer.getConnection(rootWindowName).createStatement();
            webSocketServer.setStatement(rootWindowName, statement);
        }

        String sql = paramReq.getSql();
        if (paramReq.isPackage()) {
            statement.execute(sql);
            Map<String, String> messageMap = new HashMap<>();
            messageMap.put(RESULT, LocaleStringUtils.transLanguageWs("1002", webSocketServer));
            webSocketServer.sendMessage(windowName, MESSAGE, SUCCESS, messageMap);
            return;
        }
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
        if (StringUtils.isEmpty(newOid)) {
            webSocketServer.sendMessage(windowName, TEXT,
                    LocaleStringUtils.transLanguageWs("1002", webSocketServer), null);
            return;
        }

        Map<String, String> messageMap = new HashMap<>();
        messageMap.put(RESULT, LocaleStringUtils.transLanguageWs("1005", webSocketServer));
        webSocketServer.sendMessage(windowName, MESSAGE, SUCCESS, messageMap);
        paramReq.setOid(newOid);
        Map<String, Map<String, String>> map = DebugUtils.getResultMap(webSocketServer, paramReq);
        webSocketServer.sendMessage(windowName, NEW_FILE, SUCCESS, map);
    }

    @Override
    public Object formatJson(String str) {
        return JSON.parseObject(str, PublicParamQuery.class);
    }
}
