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
        PublicParamReq paramReq = DebugUtils.changeParamType(obj);
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
                if (StringUtils.isEmpty(pkgName)) {
                    throw new CustomException(LocaleString.transLanguageWs("2015", webSocketServer));
                }

                String str = "PACKAGE  DECLARE";
                pkgSrc = pkgSrc.replace(str, "create or replace package " + pkgName + " is ");
                pkgBodySrc = pkgBodySrc.replace(str, "create or replace package body " + pkgName + " is ");
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
        return JSON.parseObject(str, PublicParamReq.class);
    }
}
