/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.compatible.GainObjectSQLService;
import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.model.query.DatabaseMetaarrayColumnQuery;
import com.nctigba.datastudio.model.query.DatabaseMetaarrayQuery;
import com.nctigba.datastudio.model.query.DatabaseMetaarraySchemaQuery;
import com.nctigba.datastudio.service.QueryMetaArrayService;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.dao.ConnectionMapDAO.conMap;

@Slf4j
@Service
public class QueryMetaArrayServiceImpl implements QueryMetaArrayService {
    @Autowired
    private ConnectionConfig connectionConfig;


    private Map<String, GainObjectSQLService> gainObjectSQLService;

    @Resource
    public void setGainObjectSQLService(List<GainObjectSQLService> SQLServiceList) {
        gainObjectSQLService = new HashMap<>();
        for (GainObjectSQLService s : SQLServiceList) {
            gainObjectSQLService.put(s.type(), s);
        }
    }

    public List<String> databaseList(String uuid) throws Exception {
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

            } catch (Exception e) {
                log.info(e.toString());
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            log.info(e.toString());
            throw new RuntimeException(e);
        }
    }

    public List<Map<String, String>> schemaList(DatabaseMetaarraySchemaQuery request) throws Exception {
        log.info("schemaList request is: " + request);
        return gainObjectSQLService.get(conMap.get(request.getUuid()).getType()).schemaList(request);
    }

    public List<String> objectList(DatabaseMetaarrayQuery request) throws Exception {
        log.info("objectList request is: " + request);
        List<String> objectList = gainObjectSQLService.get(conMap.get(request.getUuid()).getType()).objectList(request);
        return objectList;
    }

    public List<String> tableColumnList(DatabaseMetaarrayColumnQuery request) {
        log.info("tableColumnList request is: " + request);
        List<String> columnList = gainObjectSQLService.get(conMap.get(request.getUuid()).getType()).tableColumnList(
                request);
        return columnList;
    }

    public List<String> baseTypeList(String uuid) throws Exception {
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

    public List<String> tablespaceList(String uuid) throws Exception {
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
}