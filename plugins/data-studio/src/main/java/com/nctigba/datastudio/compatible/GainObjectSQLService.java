/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.compatible;

import com.nctigba.datastudio.model.query.DatabaseMetaarrayColumnQuery;
import com.nctigba.datastudio.model.query.DatabaseMetaarrayQuery;
import com.nctigba.datastudio.model.query.DatabaseMetaarraySchemaQuery;
import com.nctigba.datastudio.util.DebugUtils;
import org.opengauss.admin.common.exception.CustomException;

import java.util.List;
import java.util.Map;

public interface GainObjectSQLService {
    String type();

    default String databaseList() throws Exception {
        throw new CustomException(DebugUtils.getMessage());
    }

    default List<Map<String, String>> schemaList(DatabaseMetaarraySchemaQuery request) throws Exception {
        throw new CustomException(DebugUtils.getMessage());
    }

    default List<String> objectList(DatabaseMetaarrayQuery request) throws Exception {
        throw new CustomException(DebugUtils.getMessage());
    }

    default List<String> tableColumnList(DatabaseMetaarrayColumnQuery request) {
        throw new CustomException(DebugUtils.getMessage());
    }

    default String databaseVersion() {
        throw new CustomException(DebugUtils.getMessage());
    }

    default String tableSql(String schema) {
        throw new CustomException(DebugUtils.getMessage());
    }

    default String viewSql(String schema) {
        throw new CustomException(DebugUtils.getMessage());
    }

    default String fun_prosSql(String schema) {
        throw new CustomException(DebugUtils.getMessage());
    }

    default String sequenceSql(String schema) {
        throw new CustomException(DebugUtils.getMessage());
    }

    default String synonymSql(String schema) {
        throw new CustomException(DebugUtils.getMessage());
    }

    default String baseTypeListSQL() {
        throw new CustomException(DebugUtils.getMessage());
    }

    default String tablespaceListSQL() {
        throw new CustomException(DebugUtils.getMessage());
    }
}
