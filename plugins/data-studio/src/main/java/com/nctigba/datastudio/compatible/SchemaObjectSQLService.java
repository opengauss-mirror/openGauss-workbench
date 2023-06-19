/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.compatible;

import com.nctigba.datastudio.model.dto.DatabaseCreateViewDTO;
import com.nctigba.datastudio.model.dto.DatabaseSelectViewDTO;
import com.nctigba.datastudio.model.dto.DatabaseViewDdlDTO;
import com.nctigba.datastudio.util.DebugUtils;
import org.opengauss.admin.common.exception.CustomException;

public interface SchemaObjectSQLService {
    String type();

    default String queryAllUsersDDL() {
        throw new CustomException(DebugUtils.getMessage());
    }

    default String querySchemaDDL(String oid) {
        throw new CustomException(DebugUtils.getMessage());
    }

    default String createCommentSchemaSQL(String schemaName, String description) {
        throw new CustomException(DebugUtils.getMessage());
    }

    default String createSchemaSQL(String schemaName,String owner) {
        throw new CustomException(DebugUtils.getMessage());
    }

    default String updateSchemaNameSQL(String oldSchemaName,String schemaName) {
        throw new CustomException(DebugUtils.getMessage());
    }

    default String updateSchemaOwnerSQL(String oldSchemaName,String owner) {
        throw new CustomException(DebugUtils.getMessage());
    }

    default String updateSchemaCommentSQL(String schemaName ,String description) {
        throw new CustomException(DebugUtils.getMessage());
    }

    default String deleteSchemaSQL(String schemaName) throws Exception {
        throw new CustomException(DebugUtils.getMessage());
    }
}
