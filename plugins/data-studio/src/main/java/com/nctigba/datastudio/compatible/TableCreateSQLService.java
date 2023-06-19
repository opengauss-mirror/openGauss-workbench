/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.compatible;

import com.nctigba.datastudio.model.query.SelectDataQuery;
import com.nctigba.datastudio.util.DebugUtils;
import com.nctigba.datastudio.util.LocaleString;
import org.opengauss.admin.common.exception.CustomException;

import java.util.Map;

public interface TableCreateSQLService {
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
}