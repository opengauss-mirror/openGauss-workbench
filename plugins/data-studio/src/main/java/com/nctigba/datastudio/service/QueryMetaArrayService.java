/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service;

import com.nctigba.datastudio.model.query.DatabaseMetaarrayColumnQuery;
import com.nctigba.datastudio.model.query.DatabaseMetaarrayQuery;
import com.nctigba.datastudio.model.query.DatabaseMetaarraySchemaQuery;

import java.util.List;
import java.util.Map;

public interface QueryMetaArrayService {
    List<String> databaseList(String uuid) throws Exception;

    List<String> objectList(DatabaseMetaarrayQuery request) throws Exception;

    List<String> tableColumnList(DatabaseMetaarrayColumnQuery request) throws Exception;

    List<Map<String, String>> schemaList(DatabaseMetaarraySchemaQuery request) throws Exception;

    List<String> baseTypeList(String uuid) throws Exception;

    List<String> tablespaceList(String uuid) throws Exception;

}
