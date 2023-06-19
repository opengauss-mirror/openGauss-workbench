/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.compatible;

import com.nctigba.datastudio.model.dto.DatabaseConstraintDTO;
import com.nctigba.datastudio.model.dto.DatabaseConstraintPkDTO;
import com.nctigba.datastudio.model.dto.DatabaseCreUpdColumnDTO;
import com.nctigba.datastudio.model.dto.DatabaseCreateTableDTO;
import com.nctigba.datastudio.model.dto.DatabaseIndexDTO;
import com.nctigba.datastudio.model.query.SelectDataQuery;
import com.nctigba.datastudio.util.DebugUtils;
import com.nctigba.datastudio.util.LocaleString;
import org.opengauss.admin.common.exception.CustomException;

import java.util.List;

public interface TableColumnSQLService {
    String type();

    default String tableConstraintSQL(SelectDataQuery request) {
        throw new CustomException(DebugUtils.getMessage());
    }

    default String tablePKConstraintSQL(String schema, String tableName) {
        throw new CustomException(DebugUtils.getMessage());
    }

    default void editConstraint(DatabaseConstraintDTO request) throws Exception {
        throw new CustomException(DebugUtils.getMessage());
    }

    default void editPkConstraint(DatabaseConstraintPkDTO request) {
        throw new CustomException(DebugUtils.getMessage());
    }

    default String tableIndexSQL(SelectDataQuery request) {
        throw new CustomException(DebugUtils.getMessage());
    }

    default String tableColumnSQL(String oid, String schema, String tableName) {
        throw new CustomException(DebugUtils.getMessage());
    }

    default List<String> tableColumnAddSQL(DatabaseCreUpdColumnDTO.CreUpdColumnDataDTO data, String schema,
        String tableName) {
        throw new CustomException(DebugUtils.getMessage());
    }

    default String tableColumnDropSQL(DatabaseCreUpdColumnDTO.CreUpdColumnDataDTO data, String schema,
        String tableName) {
        throw new CustomException(DebugUtils.getMessage());
    }

    default List<String> tableColumnUpdateSQL(DatabaseCreUpdColumnDTO.CreUpdColumnDataDTO data, String schema,
        String tableName, String uuid) {
        throw new CustomException(DebugUtils.getMessage());
    }

    default void editIndex(DatabaseIndexDTO request) throws Exception {
        throw new CustomException(DebugUtils.getMessage());
    }

    default String createTable(DatabaseCreateTableDTO request) throws Exception {
        throw new CustomException(DebugUtils.getMessage());
    }
}
