/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.compatible;

import com.nctigba.datastudio.model.query.SelectDataQuery;
import com.nctigba.datastudio.util.DebugUtils;
import com.nctigba.datastudio.util.LocaleString;
import org.opengauss.admin.common.exception.CustomException;

import java.util.List;
import java.util.Map;

public interface TableObjectSQLService {
    String type();

    default String tableDataSQL(String schema, String tableName) {
        throw new CustomException(DebugUtils.getMessage());
    }

    default Map<String, String> tableDdl(SelectDataQuery request) {
        throw new CustomException(DebugUtils.getMessage());
    }

    default String tableDataCountSQL(String schema, String tableName) {
        throw new CustomException(DebugUtils.getMessage());
    }

    default String tableAnalyseSQL(String schema, String tableName) {
        throw new CustomException(DebugUtils.getMessage());
    }

    default String tableTruncateSQL(String schema, String tableName) {
        throw new CustomException(DebugUtils.getMessage());
    }

    default String tableVacuumSQL(String schema, String tableName) {
        throw new CustomException(DebugUtils.getMessage());
    }

    default String tableReindexSQL(String schema, String tableName) {
        throw new CustomException(DebugUtils.getMessage());
    }

    default String tableRenameSQL(String schema, String tableName, String newName) {
        throw new CustomException(DebugUtils.getMessage());
    }

    default String tableCommentSQL(String schema, String tableName, String comment) {
        throw new CustomException(DebugUtils.getMessage());
    }

    default String tableAlterSchemaSQL(String schema, String tableName, String newSchema) {
        throw new CustomException(DebugUtils.getMessage());
    }

    default String tableDropSQL(String schema, String tableName) {
        throw new CustomException(DebugUtils.getMessage());
    }

    default String tableAlterTablespaceSQL(String schema, String tableName, String tablespace) {
        throw new CustomException(DebugUtils.getMessage());
    }

    default String tableSequenceSQL(String schema, String tableName) {
        throw new CustomException(DebugUtils.getMessage());
    }

    default List<Map<String, Object>> tableAttributeSQL(String uuid, String oid, String tableType) {
        throw new CustomException(DebugUtils.getMessage());
    }
}