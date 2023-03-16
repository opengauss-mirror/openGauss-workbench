package com.nctigba.datastudio.service;

import com.nctigba.datastudio.model.dto.DataListDTO;
import com.nctigba.datastudio.model.dto.DbConnectionCreateDTO;
import com.nctigba.datastudio.model.entity.DatabaseConnectionDO;
import com.nctigba.datastudio.model.query.DatabaseMetaarrayIdSchemaQuery;

import java.util.List;

public interface DbConnectionService {

    String test(DbConnectionCreateDTO request) throws Exception;

    void deleteDatabaseConnectionList(String id) throws Exception;

    DatabaseConnectionDO databaseAttributeConnection(String id) throws Exception;

    List<DatabaseConnectionDO> databaseConnectionList(String webUser) throws Exception;

    DatabaseConnectionDO updateDatabaseConnection(DbConnectionCreateDTO request) throws Exception;

    List<DataListDTO> dataList(String id);

    List<DataListDTO> schemaObjectList(DatabaseMetaarrayIdSchemaQuery schema);

    DatabaseConnectionDO addDatabaseConnection(DbConnectionCreateDTO request) throws Exception;

    DatabaseConnectionDO loginDatabaseConnection(DbConnectionCreateDTO request) throws Exception;

}
