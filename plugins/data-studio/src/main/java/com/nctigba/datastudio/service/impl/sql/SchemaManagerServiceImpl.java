/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  SchemaManagerServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/service/impl/sql/SchemaManagerServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.compatible.SchemaObjectSQLService;
import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.model.query.SchemaManagerQuery;
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
import static com.nctigba.datastudio.utils.DebugUtils.comGetUuidType;

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
    public List<String> queryAllUsers(SchemaManagerQuery request) throws SQLException {
        log.info("SchemaManagerService queryAllUsers request: " + request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(schemaObjectSQLService.get(
                        comGetUuidType(request.getUuid())).queryAllUsersDDL())
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
    public Map<String, String> querySchema(SchemaManagerQuery request) throws SQLException {
        log.info("SchemaManagerService querySchema request: " + request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(schemaObjectSQLService.get(
                        comGetUuidType(request.getUuid())).querySchemaDDL(request.getOid()))
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
    public void createSchema(SchemaManagerQuery request) throws SQLException {
        log.info("SchemaManagerService createSchema request: " + request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
            String schemaName = request.getSchemaName();
            String owner = request.getOwner();
            statement.execute(schemaObjectSQLService.get(comGetUuidType(request.getUuid()))
                    .createSchemaSQL(schemaName, owner));
            String description = request.getDescription();
            if (StringUtils.isNotEmpty(description)) {
                statement.execute(schemaObjectSQLService.get(comGetUuidType(request.getUuid()))
                        .createCommentSchemaSQL(schemaName, description));
            }
        }
        log.info("SchemaManagerService createSchema end ");
    }

    @Override
    public void updateSchema(SchemaManagerQuery request) throws SQLException {
        log.info("SchemaManagerService updateSchema request: " + request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(schemaObjectSQLService.get(
                        comGetUuidType(request.getUuid())).querySchemaDDL(request.getOid()))
        ) {
            String oldSchemaName = request.getSchemaName();
            String oldOwner = request.getOwner();
            while (resultSet.next()) {
                oldSchemaName = resultSet.getString(NSP_NAME);
                oldOwner = resultSet.getString(ROL_NAME);
            }
            String schemaName = request.getSchemaName();
            if (!oldSchemaName.equals(schemaName)) {
                statement.execute(schemaObjectSQLService.get(comGetUuidType(request.getUuid()))
                        .updateSchemaNameSQL(oldSchemaName, schemaName));
            }
            String owner = request.getOwner();
            if (!oldOwner.equals(owner)) {
                statement.execute(schemaObjectSQLService.get(comGetUuidType(request.getUuid()))
                        .updateSchemaOwnerSQL(oldSchemaName, owner));
            }
            statement.execute(schemaObjectSQLService.get(comGetUuidType(request.getUuid()))
                    .updateSchemaCommentSQL(schemaName, request.getDescription()));
        }
        log.info("SchemaManagerService updateSchema end ");
    }

    @Override
    public void deleteSchema(SchemaManagerQuery request) throws SQLException {
        log.info("SchemaManagerService deleteSchema request: " + request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
            statement.execute(schemaObjectSQLService.get(comGetUuidType(request.getUuid()))
                    .deleteSchemaSQL(request.getSchemaName()));
        }
        log.info("SchemaManagerService deleteSchema end ");
    }
}
