/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.compatible.DatabaseObjectSQLService;
import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.dao.ConnectionMapDAO;
import com.nctigba.datastudio.dao.DatabaseConnectionDAO;
import com.nctigba.datastudio.model.dto.ConnectionDTO;
import com.nctigba.datastudio.model.dto.CreateDatabaseDTO;
import com.nctigba.datastudio.model.dto.DatabaseNameDTO;
import com.nctigba.datastudio.model.dto.RenameDatabaseDTO;
import com.nctigba.datastudio.model.entity.DatabaseConnectionDO;
import com.nctigba.datastudio.model.entity.DatabaseConnectionUrlDO;
import com.nctigba.datastudio.service.CreateDatabaseService;
import com.nctigba.datastudio.service.MetaDataByJdbcService;
import com.nctigba.datastudio.utils.DebugUtils;
import com.nctigba.datastudio.utils.LocaleStringUtils;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.CustomException;
import org.opengauss.admin.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.nctigba.datastudio.constants.SqlConstants.CONFIGURE_TIME;
import static com.nctigba.datastudio.constants.SqlConstants.GET_URL_JDBC;
import static com.nctigba.datastudio.dao.ConnectionMapDAO.conMap;
import static com.nctigba.datastudio.utils.DebugUtils.comGetUuidType;

/**
 * CreateDatabaseServiceImpl
 *
 * @since 2023-6-26
 */
@Slf4j
@Service
public class CreateDatabaseServiceImpl implements CreateDatabaseService {
    @Autowired
    private ConnectionConfig connectionConfig;

    @Resource
    private DatabaseConnectionDAO databaseConnectionDAO;
    @Resource
    private MetaDataByJdbcService metaDataByJdbcService;

    private Map<String, DatabaseObjectSQLService> databaseObjectSQLService;

    /**
     * set database object sql service
     *
     * @param SQLServiceList SQLServiceList
     */
    @Resource
    public void setDatabaseObjectSQLService(List<DatabaseObjectSQLService> SQLServiceList) {
        databaseObjectSQLService = new HashMap<>();
        for (DatabaseObjectSQLService service : SQLServiceList) {
            databaseObjectSQLService.put(service.type(), service);
        }
    }


    @Override
    public void createDatabase(CreateDatabaseDTO request) throws SQLException {
        log.info("createDatabase request is: " + request);

        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
            String ddl = databaseObjectSQLService.get(comGetUuidType(request.getUuid())).createDatabase(request);
            statement.execute(ddl);
            log.info("createDatabase sql is: " + ddl);
        }
    }

    @Override
    public DatabaseConnectionDO connectionDatabase(DatabaseConnectionDO database) {
        log.info("connectionDatabase request is: " + database);
        ConnectionDTO connectionDTO = new ConnectionDTO();
        DatabaseConnectionUrlDO databaseConnectionUrlDO = new DatabaseConnectionUrlDO();
        DatabaseConnectionDO databaseConnectionDO = databaseConnectionDAO.getByIdDatabase(
                Integer.parseInt(database.getId()), database.getWebUser());
        String uuid;
        if (StringUtils.isNotEmpty(database.getConnectionid())) {
            uuid = database.getConnectionid();
        } else {
            uuid = UUID.randomUUID().toString();
        }
        databaseConnectionDO.setConnectionid(uuid);
        databaseConnectionDO.setDataName(database.getDataName());
        databaseConnectionUrlDO.setUrl(
                GET_URL_JDBC + databaseConnectionDO.getIp() + ":" + databaseConnectionDO.getPort()
                        + "/" + database.getDataName() + CONFIGURE_TIME);
        databaseConnectionUrlDO.setUserName(databaseConnectionDO.getUserName());
        databaseConnectionUrlDO.setPassword(databaseConnectionDO.getPassword());
        databaseConnectionUrlDO.setType(databaseConnectionDO.getType());
        metaDataByJdbcService.testQuerySQL(databaseConnectionUrlDO.getUrl(), databaseConnectionUrlDO.getUserName(),
                databaseConnectionUrlDO.getPassword(),
                databaseObjectSQLService.get(databaseConnectionDO.getType()).connectionDatabaseTest());
        connectionDTO.setConnectionDTO(databaseConnectionUrlDO);
        ConnectionMapDAO.setConMap(uuid, connectionDTO);
        databaseConnectionDO.setIsRememberPassword(database.getIsRememberPassword());
        log.info("connectionDatabase response is: {}", databaseConnectionDO);
        return databaseConnectionDO;
    }

    @Override
    public void deleteDatabase(DatabaseNameDTO request) throws SQLException {
        log.info("deleteDatabase request is: {}", request);
        String ddl = databaseObjectSQLService.get(comGetUuidType(request.getUuid())).deleteDatabaseSQL(request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
            statement.execute(ddl);
            log.info("deleteDatabase sql is: " + ddl);
        }
    }

    @Override
    public void renameDatabase(RenameDatabaseDTO request) throws SQLException {
        log.info("deleteDatabase request is: " + request);
        String ddl;
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid())
        ) {
            if (!request.getOldDatabaseName().equals(request.getDatabaseName())) {
                ddl = databaseObjectSQLService.get(comGetUuidType(request.getUuid())).renameDatabaseSQL(request);
                try (
                        Statement statement = connection.createStatement()
                ) {
                    statement.execute(ddl);
                    log.info("deleteDatabase sql is: " + ddl);
                }
            }
            if (Integer.parseInt(request.getConRestrictions()) >= -1) {
                ddl = databaseObjectSQLService.get(comGetUuidType(request.getUuid())).conRestrictionsSQL(request);
                try (
                        Statement statement = connection.createStatement()
                ) {
                    statement.execute(ddl);
                    log.info("deleteDatabase sql is: " + ddl);
                }
            } else {
                throw new CustomException(LocaleStringUtils.transLanguage("2013"));
            }
        }
    }

    @Override
    public Map<String, Object> databaseAttribute(DatabaseNameDTO request) throws SQLException {
        log.info("deleteDatabase request is: " + request);
        Map<String, Object> resultMap;
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
            String ddl = databaseObjectSQLService.get(comGetUuidType(request.getUuid())).databaseAttributeSQL(
                    request);
            try (
                    ResultSet resultSet = statement.executeQuery(ddl)
            ) {
                resultMap = DebugUtils.parseResultSet(resultSet);
                log.info("synonymAttribute sql is: " + ddl);
                log.info("synonymAttribute response is: " + resultMap);
            }
            return resultMap;
        }
    }

    @Override
    public Map<String, Object> databaseAttributeUpdate(DatabaseNameDTO request) throws SQLException {
        log.info("deleteDatabase request is: " + request);
        Map<String, Object> resultMap;
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
            String ddl = databaseObjectSQLService.get(
                    comGetUuidType(request.getUuid())).databaseAttributeUpdateSQL(request);
            try (
                    ResultSet resultSet = statement.executeQuery(ddl)
            ) {
                resultMap = DebugUtils.parseResultSet(resultSet);
                log.info("synonymAttribute sql is: " + ddl);
                log.info("synonymAttribute response is: " + resultMap);
            }
            return resultMap;
        }
    }
}
