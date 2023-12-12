/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.compatible.opengauss;

import com.nctigba.datastudio.compatible.SchemaObjectSQLService;
import com.nctigba.datastudio.utils.DebugUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import static com.nctigba.datastudio.constants.CommonConstants.OPENGAUSS;
import static com.nctigba.datastudio.constants.SqlConstants.ALTER_SCHEMA_NAME_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.ALTER_SCHEMA_OWNER_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.CREATE_SCHEMA_COMMENT_DDL_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.CREATE_SCHEMA_DDL_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.DROP_SCHEMA_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.QUERY_SCHEMA_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.QUERY_USERS_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.UPDATE_DESCRIPTION_SQL;

/**
 * SchemaObjectSQLService achieve
 *
 * @since 2023-06-26
 */
@Slf4j
@Service
public class SchemaObjectSQLServiceImpl implements SchemaObjectSQLService {
    @Override
    public String type() {
        return OPENGAUSS;
    }

    @Override
    public String queryAllUsersDDL() {
        return QUERY_USERS_SQL;
    }


    @Override
    public String querySchemaDDL(String oid) {
        log.info("querySchemaDDL request is :" + oid);
        String ddl = String.format(QUERY_SCHEMA_SQL, oid);
        log.info("querySchemaDDL DDL is :" + ddl);
        return ddl;
    }

    @Override
    public String createSchemaSQL(String schemaName, String owner) {
        log.info("createSchemaSQL request schemaName is :" + schemaName + ",owner is:" + owner);
        String ddl = String.format(CREATE_SCHEMA_DDL_SQL, DebugUtils.needQuoteName(schemaName), owner);
        log.info("createSchemaSQL DDL is :" + ddl);
        return ddl;
    }


    @Override
    public String createCommentSchemaSQL(String schemaName, String description) {
        log.info("createCommentSchemaSQL request schemaName is :" + schemaName + ",description is:" + description);
        String ddl = String.format(CREATE_SCHEMA_COMMENT_DDL_SQL, DebugUtils.needQuoteName(schemaName), description);
        log.info("createCommentSchemaSQL DDL is :" + ddl);
        return ddl;
    }

    @Override
    public String updateSchemaNameSQL(String oldSchemaName, String schemaName) {
        log.info("updateSchemaNameSQL request schemaName is :" + schemaName);
        String ddl = String.format(ALTER_SCHEMA_NAME_SQL, DebugUtils.needQuoteName(oldSchemaName),
                DebugUtils.needQuoteName(schemaName));
        log.info("updateSchemaNameSQL DDL is :" + ddl);
        return ddl;
    }


    @Override
    public String updateSchemaOwnerSQL(String schemaName, String description) {
        log.info("updateSchemaOwnerSQL request schemaName is :" + schemaName + ",description is:" + description);
        String ddl = String.format(ALTER_SCHEMA_OWNER_SQL, DebugUtils.needQuoteName(schemaName), description);
        log.info("updateSchemaOwnerSQL DDL is :" + ddl);
        return ddl;
    }


    @Override
    public String updateSchemaCommentSQL(String schemaName, String description) {
        log.info("updateSchemaCommentSQL request schemaName is :" + schemaName + ",description is:" + description);
        String ddl;
        if (StringUtils.isNotEmpty(description)) {
            ddl = String.format(UPDATE_DESCRIPTION_SQL, DebugUtils.needQuoteName(schemaName), description);
        } else {
            ddl = String.format(UPDATE_DESCRIPTION_SQL, DebugUtils.needQuoteName(schemaName), "");
        }
        log.info("updateSchemaCommentSQL DDL is :" + ddl);
        return ddl;
    }

    @Override
    public String deleteSchemaSQL(String schemaName) {
        log.info("deleteSchemaSQL request schemaName is :" + schemaName);
        String ddl = String.format(DROP_SCHEMA_SQL, DebugUtils.needQuoteName(schemaName));
        log.info("deleteSchemaSQL DDL is :" + ddl);
        return ddl;
    }
}
