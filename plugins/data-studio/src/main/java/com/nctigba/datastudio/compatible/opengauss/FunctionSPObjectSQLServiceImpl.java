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
 *  FunctionSPObjectSQLServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/compatible/opengauss/FunctionSPObjectSQLServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.compatible.opengauss;

import com.nctigba.datastudio.compatible.FunctionSPObjectSQLService;
import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.model.dto.DatabaseFunctionSPDTO;
import com.nctigba.datastudio.model.query.PackageQuery;
import com.nctigba.datastudio.utils.DebugUtils;
import com.nctigba.datastudio.utils.LocaleStringUtils;
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
import static com.nctigba.datastudio.constants.SqlConstants.DROP_PACKAGE_SQL;
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
                        throw new CustomException(LocaleStringUtils.transLanguage("2015"));
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
                String proKind;
                if (funcResult.next()) {
                    proKind = funcResult.getString(PRO_KIND);
                } else {
                    throw new CustomException(LocaleStringUtils.transLanguage("2015"));
                }
                String sql = "";
                if ("f".equals(proKind)) {
                    sql = String.format(DROP_FUNCTION_KEYWORD_SQL, DebugUtils.needQuoteName(request.getSchema()),
                            request.getFunctionSPName());
                } else if ("p".equals(proKind)) {
                    sql = String.format(DROP_PROCEDURE_KEYWORD_SQL, DebugUtils.needQuoteName(request.getSchema()),
                            request.getFunctionSPName());
                }
                log.info("dropFunctionSP sql is: " + sql);
                return sql;
            }
        }
    }

    @Override
    public String dropPackage(PackageQuery request) {
        return String.format(DROP_PACKAGE_SQL, DebugUtils.needQuoteName(request.getSchema()), request.getPackageName());
    }
}
