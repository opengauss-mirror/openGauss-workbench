/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.compatible.GainObjectSQLService;
import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.model.query.DatabaseMetaarrayColumnQuery;
import com.nctigba.datastudio.model.query.DatabaseMetaarrayQuery;
import com.nctigba.datastudio.model.query.DatabaseMetaarraySchemaQuery;
import com.nctigba.datastudio.model.query.UserQuery;
import com.nctigba.datastudio.service.QueryMetaArrayService;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.CustomException;
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

import static com.nctigba.datastudio.constants.CommonConstants.NAME;
import static com.nctigba.datastudio.constants.CommonConstants.OID;
import static com.nctigba.datastudio.dao.ConnectionMapDAO.conMap;

/**
 * QueryMetaArrayServiceImpl
 *
 * @since 2023-6-26
 */
@Slf4j
@Service
public class QueryMetaArrayServiceImpl implements QueryMetaArrayService {
    @Autowired
    private ConnectionConfig connectionConfig;

    private Map<String, GainObjectSQLService> gainObjectSQLService;

    /**
     * set gain object sql service
     *
     * @param SQLServiceList SQLServiceList
     */
    @Resource
    public void setGainObjectSQLService(List<GainObjectSQLService> SQLServiceList) {
        gainObjectSQLService = new HashMap<>();
        for (GainObjectSQLService service : SQLServiceList) {
            gainObjectSQLService.put(service.type(), service);
        }
    }

    @Override
    public List<String> databaseList(String uuid) {
        log.info("schemaList request is: " + uuid);
        String sql;
        List<String> databaseList = new ArrayList<>();
        try (
                Connection connection = connectionConfig.connectDatabase(uuid);
                Statement statement = connection.createStatement()
        ) {
            sql = gainObjectSQLService.get(conMap.get(uuid).getType()).databaseList();
            try (
                    ResultSet resultSet = statement.executeQuery(sql)
            ) {
                while (resultSet.next()) {
                    databaseList.add(resultSet.getString("datname"));
                }
                log.info("schemaList response is: " + databaseList);
                return databaseList;

            } catch (SQLException e) {
                log.info(e.toString());
                throw new CustomException(e.getMessage());
            }
        } catch (SQLException e) {
            log.info(e.toString());
            throw new CustomException(e.getMessage());
        }
    }

    @Override
    public List<Map<String, String>> schemaList(DatabaseMetaarraySchemaQuery request) throws SQLException {
        log.info("schemaList request is: " + request);
        return gainObjectSQLService.get(conMap.get(request.getUuid()).getType()).schemaList(request);
    }

    @Override
    public List<String> objectList(DatabaseMetaarrayQuery request) throws SQLException {
        log.info("objectList request is: " + request);
        return gainObjectSQLService.get(conMap.get(request.getUuid()).getType()).objectList(request);
    }

    @Override
    public List<String> tableColumnList(DatabaseMetaarrayColumnQuery request) throws SQLException {
        log.info("tableColumnList request is: " + request);
        return gainObjectSQLService.get(conMap.get(request.getUuid()).getType()).tableColumnList(request);
    }

    @Override
    public List<String> baseTypeList(String uuid) throws SQLException {
        try (
                Connection connection = connectionConfig.connectDatabase(uuid);
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(
                        gainObjectSQLService.get(conMap.get(uuid).getType()).baseTypeListSQL())
        ) {
            List<String> columnList = new ArrayList<>();
            while (resultSet.next()) {
                columnList.add(resultSet.getString("type"));
            }
            return columnList;
        }
    }

    @Override
    public List<String> tablespaceList(String uuid) throws SQLException {
        try (
                Connection connection = connectionConfig.connectDatabase(uuid);
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(
                        gainObjectSQLService.get(conMap.get(uuid).getType()).tablespaceListSQL())
        ) {
            List<String> columnList = new ArrayList<>();
            while (resultSet.next()) {
                columnList.add(resultSet.getString(1));
            }
            return columnList;
        }
    }

    @Override
    public UserQuery userList(String uuid) throws SQLException {
        try (
                Connection connection = connectionConfig.connectDatabase(uuid);
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(
                        gainObjectSQLService.get(conMap.get(uuid).getType()).userSql())
        ) {
            List<Map<String, String>> userList = new ArrayList<>();
            List<Map<String, String>> roleList = new ArrayList<>();
            while (resultSet.next()) {
                if (resultSet.getString(2).equals("1")) {
                    Map<String, String> userMap = new HashMap<>();
                    userMap.put(NAME, resultSet.getString(1));
                    userMap.put(OID, resultSet.getString(3));
                    userList.add(userMap);
                } else {
                    Map<String, String> roleMap = new HashMap<>();
                    roleMap.put(NAME, resultSet.getString(1));
                    roleMap.put(OID, resultSet.getString(3));
                    roleList.add(roleMap);
                }
            }
            UserQuery userQuery = new UserQuery();
            userQuery.setUser(userList);
            userQuery.setRole(roleList);
            return userQuery;
        }
    }

    @Override
    public List<String> resourceList(String uuid) throws SQLException {
        try (
                Connection connection = connectionConfig.connectDatabase(uuid);
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(
                        gainObjectSQLService.get(conMap.get(uuid).getType()).resourceListSQL())
        ) {
            List<String> resourceList = new ArrayList<>();
            while (resultSet.next()) {
                resourceList.add(resultSet.getString(1));
            }
            return resourceList;
        }
    }
}