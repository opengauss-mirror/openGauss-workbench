package com.nctigba.datastudio.service;

import com.nctigba.datastudio.model.dto.CreateDatabaseDTO;
import com.nctigba.datastudio.model.dto.DatabaseNameDTO;
import com.nctigba.datastudio.model.dto.RenameDatabaseDTO;
import com.nctigba.datastudio.model.entity.DatabaseConnectionDO;

import java.util.Map;

public interface CreateDatabaseService {
    void createDatabase(CreateDatabaseDTO database) throws Exception;

    DatabaseConnectionDO connectionDatabase(DatabaseConnectionDO database) throws Exception;

    void deleteDatabase(DatabaseNameDTO database) throws Exception;

    void renameDatabase(RenameDatabaseDTO database) throws Exception;

    Map<String, Object> databaseAttribute(DatabaseNameDTO database) throws Exception;

    Map<String, Object> databaseAttributeUpdate(DatabaseNameDTO database) throws Exception;

}
