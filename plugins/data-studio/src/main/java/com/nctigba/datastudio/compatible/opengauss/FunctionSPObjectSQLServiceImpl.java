/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.compatible.opengauss;

import com.nctigba.datastudio.compatible.FunctionSPObjectSQLService;
import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.model.dto.DatabaseFunctionSPDTO;
import com.nctigba.datastudio.util.LocaleString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static com.nctigba.datastudio.constants.CommonConstants.DEFINITION;
import static com.nctigba.datastudio.constants.CommonConstants.OPENGAUSS;
import static com.nctigba.datastudio.constants.CommonConstants.PRO_KIND;
import static com.nctigba.datastudio.constants.SqlConstants.DROP_FUNCTION_KEYWORD_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.DROP_PROCEDURE_KEYWORD_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.PROC_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.QUERY_DEF_SQL;

/**
 * FunctionSPObjectSQLService achieve
 *
 * @since 2023-06-26
 */
@Slf4j
@Service
public class FunctionSPObjectSQLServiceImpl implements FunctionSPObjectSQLService {
    @Autowired
    private ConnectionConfig connectionConfig;

    @Override
    public String type() {
        return OPENGAUSS;
    }

    @Override
    public String functionDdl(DatabaseFunctionSPDTO request) throws SQLException {
        log.info("FunctionSPObjectSQLServiceImpl functionDdl request: " + request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
            try (
                    ResultSet resultSet = statement.executeQuery(String.format(QUERY_DEF_SQL, request.getOid()))
            ) {
                String definition = Strings.EMPTY;
                if (resultSet.next()) {
                    definition = resultSet.getString(DEFINITION);
                    if (StringUtils.isEmpty(definition)) {
                        throw new CustomException(LocaleString.transLanguage("2015"));
                    }
                }

                log.info("dropFunctionSP definition is: " + definition);
                return definition;
            }
        }
    }

    @Override
    public String dropFunctionSP(DatabaseFunctionSPDTO request) throws SQLException {
        log.info("dropFunctionSP request is: " + request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
            try (
                    ResultSet funcResult = statement.executeQuery(String.format(PROC_SQL, request.getOid()))
            ) {
                String proKind = "";
                if (funcResult.next()) {
                    proKind = funcResult.getString(PRO_KIND);
                } else {
                    throw new CustomException(LocaleString.transLanguage("2015"));
                }
                String sql = "";
                if ("f".equals(proKind)) {
                    sql = String.format(DROP_FUNCTION_KEYWORD_SQL, request.getSchema(), request.getFunctionSPName());
                } else if ("p".equals(proKind)) {
                    sql = String.format(DROP_PROCEDURE_KEYWORD_SQL, request.getSchema(), request.getFunctionSPName());
                }
                log.info("dropFunctionSP sql is: " + sql);
                return sql;
            }
        }
    }
}
