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
 *  ShowPackageImpl.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/service/impl/debug/ShowPackageImpl.java
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
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.RESULT;
import static com.nctigba.datastudio.constants.CommonConstants.SUCCESS;
import static com.nctigba.datastudio.constants.SqlConstants.POINT;
import static com.nctigba.datastudio.constants.SqlConstants.QUERY_PACKAGE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.SPACE;
import static com.nctigba.datastudio.enums.MessageEnum.VIEW;

/**
 * ShowPackageImpl
 *
 * @since 2023-08-31
 */
@Slf4j
@Service("showPackage")
public class ShowPackageImpl implements OperationInterface {
    @Override
    public void operate(WebSocketServer webSocketServer, Object obj) throws SQLException, IOException {
        PublicParamQuery paramReq = DebugUtils.changeParamType(obj);
        log.info("showPackage paramReq: " + paramReq);

        String windowName = paramReq.getWindowName();
        Statement statement = webSocketServer.getStatement(windowName);
        if (statement == null || statement.isClosed()) {
            Connection connection = webSocketServer.getConnection(windowName);
            if (connection == null || connection.isClosed()) {
                connection = webSocketServer.createConnection(paramReq.getUuid(), windowName);
            }
            statement = connection.createStatement();
            webSocketServer.setStatement(windowName, statement);
        }

        if (statement == null) {
            return;
        }
        StringBuilder definition = new StringBuilder();
        try (
                ResultSet resultSet = statement.executeQuery(String.format(QUERY_PACKAGE_SQL,
                        paramReq.getSchema(), paramReq.getOid()));
        ) {
            while (resultSet.next()) {
                String pkgName = resultSet.getString("pkgname");
                String pkgSrc = resultSet.getString("pkgspecsrc").trim();
                String pkgBodySrc = resultSet.getString("pkgbodydeclsrc").trim();
                String nspName = resultSet.getString("nspname");
                if (StringUtils.isEmpty(pkgName)) {
                    throw new CustomException(LocaleStringUtils.transLanguageWs("2015", webSocketServer));
                }

                String str = "PACKAGE  DECLARE";
                pkgSrc = pkgSrc.replace(str,
                        "create or replace package " + nspName + POINT +  pkgName + " is ");
                pkgBodySrc = pkgBodySrc.replace(str,
                        "create or replace package body " + nspName + POINT +  pkgName + " is ");
                definition.append(pkgSrc).append(SPACE).append(pkgName).append(";\n/\n");
                definition.append(pkgBodySrc).append(SPACE).append(pkgName).append(";\n/");
            }
            log.info("showPackage definition: " + definition);
        }

        Map<String, Object> map = new HashMap<>();
        map.put(RESULT, definition);
        webSocketServer.sendMessage(windowName, VIEW, SUCCESS, map);
    }

    @Override
    public Object formatJson(String str) {
        return JSON.parseObject(str, PublicParamQuery.class);
    }
}
