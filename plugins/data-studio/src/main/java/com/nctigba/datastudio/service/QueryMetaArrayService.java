package com.nctigba.datastudio.service;

import com.nctigba.datastudio.model.query.DatabaseMetaarrayColumnQuery;
import com.nctigba.datastudio.model.query.DatabaseMetaarrayQuery;
import com.nctigba.datastudio.model.query.DatabaseMetaarraySchemaQuery;

import java.util.List;

public interface QueryMetaArrayService {
    List<String> databaseList(String uuid) throws Exception;

    List<String> objectList(DatabaseMetaarrayQuery request) throws Exception;

    List<String> tableColumnList(DatabaseMetaarrayColumnQuery request) throws Exception;

    List<String> schemaList(DatabaseMetaarraySchemaQuery request) throws Exception;

}
