/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.compatible.SchemaObjectSQLService;
import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.model.query.SchemaManagerRequest;
import com.nctigba.datastudio.service.SchemaManagerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.DESCRIPTION;
import static com.nctigba.datastudio.constants.CommonConstants.NSP_NAME;
import static com.nctigba.datastudio.constants.CommonConstants.OID;
import static com.nctigba.datastudio.constants.CommonConstants.OWNER;
import static com.nctigba.datastudio.constants.CommonConstants.ROL_NAME;
import static com.nctigba.datastudio.constants.CommonConstants.SCHEMA_NAME;
import static com.nctigba.datastudio.dao.ConnectionMapDAO.conMap;

/**
 * SchemaManagerServiceImpl
 *
 * @since 2023-6-26
 */
@Slf4j
@Service
public class SchemaManagerServiceImpl implements SchemaManagerService {
    @Autowired
    private ConnectionConfig connectionConfig;

    private Map<String, SchemaObjectSQLService> schemaObjectSQLService;

    /**
     * set schema object sql service
     *
     * @param SQLServiceList SQLServiceList
     */
    @Resource
    public void setSchemaObjectSQLService(List<SchemaObjectSQLService> SQLServiceList) {
        schemaObjectSQLService = new HashMap<>();
        for (SchemaObjectSQLService service : SQLServiceList) {
            schemaObjectSQLService.put(service.type(), service);
        }
    }

    @Override
    public List<String> queryAllUsers(SchemaManagerRequest request) throws SQLException {
        log.info("SchemaManagerService queryAllUsers request: " + request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(schemaObjectSQLService.get(conMap.get(request.getUuid())
                        .getType()).queryAllUsersDDL())
        ) {
            List<String> list = new ArrayList<>();
            while (resultSet.next()) {
                list.add(resultSet.getString(1));
            }
            log.info("SchemaManagerService queryAllUsers list: " + list);
            return list;
        }
    }

    @Override
    public Map<String, String> querySchema(SchemaManagerRequest request) throws SQLException {
        log.info("SchemaManagerService querySchema request: " + request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(schemaObjectSQLService.get(conMap.get(request.getUuid())
                        .getType()).querySchemaDDL(request.getOid()))
        ) {
            Map<String, String> map = new HashMap<>();
            while (resultSet.next()) {
                map.put(OID, resultSet.getString(1));
                map.put(SCHEMA_NAME, resultSet.getString(2));
                map.put(OWNER, resultSet.getString(3));
                map.put(DESCRIPTION, resultSet.getString(4));
            }
            log.info("SchemaManagerService querySchema resultMap: " + map);
            return map;
        }
    }

    @Override
    public void createSchema(SchemaManagerRequest request) throws SQLException {
        log.info("SchemaManagerService createSchema request: " + request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
            String schemaName = request.getSchemaName();
            String owner = request.getOwner();
            statement.execute(schemaObjectSQLService.get(conMap.get(request.getUuid()).getType())
                    .createSchemaSQL(schemaName, owner));
            String description = request.getDescription();
            if (StringUtils.isNotEmpty(description)) {
                statement.execute(schemaObjectSQLService.get(conMap.get(request.getUuid()).getType())
                        .createCommentSchemaSQL(schemaName, description));
            }
        }
        log.info("SchemaManagerService createSchema end ");
    }

    @Override
    public void updateSchema(SchemaManagerRequest request) throws SQLException {
        log.info("SchemaManagerService updateSchema request: " + request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(schemaObjectSQLService.get(conMap.get(request.getUuid())
                        .getType()).querySchemaDDL(request.getOid()))
        ) {
            String oldSchemaName = request.getSchemaName();
            String oldOwner = request.getOwner();
            while (resultSet.next()) {
                oldSchemaName = resultSet.getString(NSP_NAME);
                oldOwner = resultSet.getString(ROL_NAME);
            }
            String schemaName = request.getSchemaName();
            if (!oldSchemaName.equals(schemaName)) {
                statement.execute(schemaObjectSQLService.get(conMap.get(request.getUuid()).getType())
                        .updateSchemaNameSQL(oldSchemaName, schemaName));
            }
            String owner = request.getOwner();
            if (!oldOwner.equals(owner)) {
                statement.execute(schemaObjectSQLService.get(conMap.get(request.getUuid()).getType())
                        .updateSchemaOwnerSQL(oldSchemaName, owner));
            }
            statement.execute(schemaObjectSQLService.get(conMap.get(request.getUuid()).getType())
                    .updateSchemaCommentSQL(schemaName, request.getDescription()));
        }
        log.info("SchemaManagerService updateSchema end ");
    }

    @Override
    public void deleteSchema(SchemaManagerRequest request) throws SQLException {
        log.info("SchemaManagerService deleteSchema request: " + request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
            statement.execute(schemaObjectSQLService.get(conMap.get(request.getUuid()).getType())
                    .deleteSchemaSQL(request.getSchemaName()));
        }
        log.info("SchemaManagerService deleteSchema end ");
    }
}
