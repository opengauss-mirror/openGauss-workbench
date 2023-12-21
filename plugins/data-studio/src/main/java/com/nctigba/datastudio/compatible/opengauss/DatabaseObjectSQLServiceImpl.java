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
 *  DatabaseObjectSQLServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/compatible/opengauss/DatabaseObjectSQLServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.compatible.opengauss;

import com.alibaba.druid.util.StringUtils;
import com.nctigba.datastudio.compatible.DatabaseObjectSQLService;
import com.nctigba.datastudio.model.dto.CreateDatabaseDTO;
import com.nctigba.datastudio.model.dto.DatabaseNameDTO;
import com.nctigba.datastudio.model.dto.RenameDatabaseDTO;
import com.nctigba.datastudio.utils.DebugUtils;
import com.nctigba.datastudio.utils.LocaleStringUtils;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.stereotype.Service;

import static com.nctigba.datastudio.constants.CommonConstants.OPENGAUSS;
import static com.nctigba.datastudio.constants.SqlConstants.ALTER_CONNECTION_LIMIT_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.ALTER_RENAME_DATABASE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.CONNECTION_LIMIT_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.CREATE_DATABASE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.DATABASE_ATTRIBUTE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.DATABASE_UPDATA_ATTRIBUTE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.DBCOMPATIBILITY_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.DROP_DATABASE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.LC_COLLATE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.LC_CTYPE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.QUOTES;
import static com.nctigba.datastudio.constants.SqlConstants.SEMICOLON;
import static com.nctigba.datastudio.constants.SqlConstants.WITH_ENCODING_SQL;

/**
 * DatabaseObjectSQLService achieve
 *
 * @since 2023-06-26
 */
@Slf4j
@Service
public class DatabaseObjectSQLServiceImpl implements DatabaseObjectSQLService {
    @Override
    public String type() {
        return OPENGAUSS;
    }

    @Override
    public String createDatabase(CreateDatabaseDTO request) {
        log.info("createDatabaseDDL request is: " + request);
        String ddl =
                CREATE_DATABASE_SQL + DebugUtils.containsSqlInjection(request.getDatabaseName()) + WITH_ENCODING_SQL
                        + request.getDatabaseCode() + DBCOMPATIBILITY_SQL + request.getCompatibleType();
        if (!StringUtils.isEmpty(request.getCollation())) {
            ddl = ddl + LC_COLLATE_SQL + request.getCollation();
        }
        if (!StringUtils.isEmpty(request.getCharacterType())) {
            ddl = ddl + LC_CTYPE_SQL + request.getCharacterType();
        }
        if (Integer.parseInt(request.getConRestrictions()) >= -1) {
            ddl = ddl + QUOTES + CONNECTION_LIMIT_SQL + request.getConRestrictions() + SEMICOLON;
        } else {
            throw new CustomException(LocaleStringUtils.transLanguage("2013"));
        }
        log.info("createDatabaseDDL is: " + ddl);
        return ddl;
    }

    @Override
    public String connectionDatabaseTest() {
        return "SELECT 1";
    }

    @Override
    public String deleteDatabaseSQL(DatabaseNameDTO request) {
        log.info("deleteDatabaseSQL request is: " + request);
        String ddl = String.format(DROP_DATABASE_SQL, request.getDatabaseName());
        log.info("deleteDatabaseSQL DDL is: " + request);
        return ddl;
    }

    @Override
    public String renameDatabaseSQL(RenameDatabaseDTO request) {
        log.info("renameDatabaseSQL request is: " + request);
        String ddl = String.format(ALTER_RENAME_DATABASE_SQL, request.getOldDatabaseName(), request.getDatabaseName());
        log.info("renameDatabaseSQL DDL is: " + ddl);
        return ddl;
    }

    @Override
    public String conRestrictionsSQL(RenameDatabaseDTO request) {
        log.info("conRestrictionsSQL request is: " + request);
        String ddl = String.format(ALTER_CONNECTION_LIMIT_SQL, request.getDatabaseName(), request.getConRestrictions());
        log.info("conRestrictionsSQL DDL is: " + ddl);
        return ddl;
    }

    @Override
    public String databaseAttributeSQL(DatabaseNameDTO request) {
        log.info("databaseAttributeSQL request is: " + request);
        String ddl = String.format(DATABASE_ATTRIBUTE_SQL, request.getDatabaseName());
        log.info("databaseAttributeSQL DDL is: " + ddl);
        return ddl;
    }

    @Override
    public String databaseAttributeUpdateSQL(DatabaseNameDTO request) {
        log.info("databaseAttributeUpdateSQL request is: " + request);
        String ddl = String.format(DATABASE_UPDATA_ATTRIBUTE_SQL, request.getDatabaseName());
        log.info("databaseAttributeUpdateSQL DDL is: " + ddl);
        return ddl;
    }
}
