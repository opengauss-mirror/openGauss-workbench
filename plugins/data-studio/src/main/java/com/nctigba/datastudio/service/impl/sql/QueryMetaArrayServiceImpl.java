package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.model.query.DatabaseMetaarrayColumnQuery;
import com.nctigba.datastudio.model.query.DatabaseMetaarrayQuery;
import com.nctigba.datastudio.model.query.DatabaseMetaarraySchemaQuery;
import com.nctigba.datastudio.service.QueryMetaArrayService;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.constants.SqlConstants.GET_DATABASE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.GET_SCHEMA_NAME_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.QUOTES_PARENTHESES_SEMICOLON;
import static com.nctigba.datastudio.constants.SqlConstants.QUOTES_SEMICOLON;
import static com.nctigba.datastudio.constants.SqlConstants.SELECT_COLUMN_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.SELECT_COLUMN_WHERE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.SELECT_FUNCTION_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.SELECT_OBJECT_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.SELECT_OBJECT_WHERE_IN_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.SELECT_OBJECT_WHERE_SQL;

@Slf4j
@Service
public class QueryMetaArrayServiceImpl implements QueryMetaArrayService {
    @Autowired
    private ConnectionConfig connectionConfig;


    public List<String> databaseList(String uuid) {
        log.info("schemaList request is: " + uuid);
        String sql;
        List<String> databaseList = new ArrayList<>();
        try {
            Connection connection = connectionConfig.connectDatabase(uuid);
            Statement statement = connection.createStatement();
            sql = GET_DATABASE_SQL;
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                databaseList.add(resultSet.getString("datname"));
            }
            log.info("schemaList response is: " + databaseList);
            return databaseList;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.getMessage(),e);
        }
    }

    public List<String> schemaList(DatabaseMetaarraySchemaQuery request) throws Exception {
        log.info("schemaList request is: " + request);
        List<String> schemaList = new ArrayList<>();
        try(Connection connection = connectionConfig.connectDatabase(request.getUuid());
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(GET_SCHEMA_NAME_SQL);){
            while (resultSet.next()) {
                schemaList.add(resultSet.getString("schema_name"));
            }
            log.info("schemaList response is: " + schemaList);
            return schemaList;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.getMessage(),e);
        }
    }

    public List<String> objectList(DatabaseMetaarrayQuery request) throws Exception {
        log.info("objectList request is: " + request);
        try(Connection connection = connectionConfig.connectDatabase(request.getUuid());
            Statement statement = connection.createStatement();){
            List<String> objectList = new ArrayList<>();
            if (request.getObjectType().equals("ALL")) {
                try(ResultSet resultSet = statement.executeQuery(SELECT_FUNCTION_SQL + request.getSchema() + QUOTES_PARENTHESES_SEMICOLON);
                    ResultSet resultSetView = statement.executeQuery(SELECT_OBJECT_SQL + request.getSchema() + SELECT_OBJECT_WHERE_IN_SQL + "v','m" + QUOTES_PARENTHESES_SEMICOLON);
                    ResultSet resultSetTable = statement.executeQuery(SELECT_OBJECT_SQL + request.getSchema() + SELECT_OBJECT_WHERE_SQL + "r" + QUOTES_SEMICOLON);
                ){
                    while (resultSet.next()) {
                        objectList.add(resultSet.getString("proname"));
                    }while (resultSetView.next()) {
                        objectList.add(resultSetView.getString("relname"));
                    }while (resultSetTable.next()) {
                        objectList.add(resultSetTable.getString("relname"));
                    }
                    return objectList;
                }
            } else if (request.getObjectType().equals("FUN_PRO")) {
                try(ResultSet resultSet = statement.executeQuery(SELECT_FUNCTION_SQL + request.getSchema() + QUOTES_PARENTHESES_SEMICOLON);){
                    while (resultSet.next()) {
                        objectList.add(resultSet.getString("proname"));
                    }
                    return objectList;
                }
            } else if (request.getObjectType().equals("VIEW")) {
                try(ResultSet resultSet = statement.executeQuery(SELECT_OBJECT_SQL + request.getSchema() + SELECT_OBJECT_WHERE_IN_SQL + "v','m" + QUOTES_PARENTHESES_SEMICOLON);){
                    while (resultSet.next()) {
                        objectList.add(resultSet.getString("relname"));
                    }
                    return objectList;
                }
            } else {
                try(ResultSet resultSet = statement.executeQuery(SELECT_OBJECT_SQL + request.getSchema() + SELECT_OBJECT_WHERE_SQL + request.getObjectType() + QUOTES_SEMICOLON);){
                    while (resultSet.next()) {
                        objectList.add(resultSet.getString("relname"));
                    }
                    log.info("objectList response is: " + objectList);
                    return objectList;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.getMessage(),e);
        }
    }

    public List<String> tableColumnList(DatabaseMetaarrayColumnQuery request) throws Exception {
        log.info("tableColumnList request is: " + request);
        String sql;
        List<String> columnList = new ArrayList<>();
        try(Connection connection = connectionConfig.connectDatabase(request.getUuid());
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(SELECT_COLUMN_SQL + request.getSchema() + SELECT_COLUMN_WHERE_SQL + request.getObjectName() + QUOTES_SEMICOLON);){
            while (resultSet.next()) {
                columnList.add(resultSet.getString("column_name"));
            }
            log.info("tableColumnList response is: " + columnList);
            return columnList;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.getMessage(),e);
        }
    }

}
