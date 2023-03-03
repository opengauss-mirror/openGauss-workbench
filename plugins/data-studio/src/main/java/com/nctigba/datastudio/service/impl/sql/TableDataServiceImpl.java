package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.model.query.SelectDataQuery;
import com.nctigba.datastudio.service.TableDataService;
import com.nctigba.datastudio.util.DebugUtils;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.OID;
import static com.nctigba.datastudio.constants.CommonConstants.PG_GET_TABLEDEF;
import static com.nctigba.datastudio.constants.CommonConstants.RESULT;
import static com.nctigba.datastudio.constants.SqlConstants.*;

@Slf4j
@Service
public class TableDataServiceImpl implements TableDataService {
    @Autowired
    private ConnectionConfig connectionConfig;

    @Override
    public Map<String, Object> tableData(SelectDataQuery request) throws Exception {
        log.info("tableData request is: " + request);
        try(Connection connection = connectionConfig.connectDatabase(request.getUuid());
            Statement statement = connection.createStatement();ResultSet resultSet = statement.executeQuery(TABLE_DATA_SQL +
                request.getSchema() + POINT + request.getTableName() + LIMIT_SQL + SEMICOLON);
        ) {
            Map<String, Object> resultMap = DebugUtils.parseResultSet(resultSet);
            log.info("tableData resultMap is: " + resultMap);
            return resultMap;
        }
    }

    @Override
    public Map<String, Object> tableColumn(SelectDataQuery request) throws Exception {
        log.info("tableColumn request is: " + request);
        try(Connection connection = connectionConfig.connectDatabase(request.getUuid());
            Statement statement = connection.createStatement();){
            String oid = "";
            try(ResultSet oidResult = statement.executeQuery(GET_CLASS_OID_SQL + request.getTableName()
                    + NSPNAME_CONDITION + request.getSchema() + QUOTES_SEMICOLON);) {
                while (oidResult.next()) {
                    oid = oidResult.getString(OID);
                }
            }try(ResultSet resultSet = statement.executeQuery(GET_COLUMN_SQL + oid + ATTRELID_CONDITION_SQL + oid + SEMICOLON);){
                Map<String, Object> resultMap = DebugUtils.parseResultSet(resultSet);
                log.info("tableColumn resultMap is: " + resultMap);
                return resultMap;
            }
        }
    }

    @Override
    public Map<String, Object> tableIndex(SelectDataQuery request) throws Exception {
        log.info("tableIndex request is: " + request);
        try(Connection connection = connectionConfig.connectDatabase(request.getUuid());
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(GET_INDEX_SQL + request.getTableName()
                    + NSPNAME_CONDITION + request.getSchema() + INDEX_GROUP_BY_SQL);){
            Map<String, Object> resultMap = DebugUtils.parseResultSet(resultSet);
            log.info("tableIndex resultMap is: " + resultMap);
            return resultMap;
        }
    }

    @Override
    public Map<String, Object> tableConstraint(SelectDataQuery request) throws Exception {
        log.info("tableConstraint request is: " + request);
        try(Connection connection = connectionConfig.connectDatabase(request.getUuid());
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(GET_CONSTRAINT_SQL + request.getSchema()
                    + RELNAME_CONDITION + request.getTableName() + CONSTRAINT_GROUP_BY_SQL);){
            Map<String, Object> resultMap = DebugUtils.parseResultSet(resultSet);
            log.info("tableConstraint resultMap is: " + resultMap);
            return resultMap;
        }
    }

    @Override
    public Map<String, String> tableDdl(SelectDataQuery request) throws Exception {
        log.info("tableDdl request is: " + request);
        Map<String, String> resultMap = new HashMap<>();
        try(Connection connection = connectionConfig.connectDatabase(request.getUuid());
            Statement statement = connection.createStatement();){
            try(ResultSet count = statement.executeQuery(SELECT_KEYWORD_SQL + COUNT_SQL +FROM_CLASS_SQL+
                    request.getSchema() + FROM_CLASS_WHERE_SQL + request.getTableName() + RELKIND_SQL+ "r" + QUOTES_SEMICOLON);){
                count.next();
                int a = count.getInt("count");
                if(a==0){
                    throw new CustomException("The table does not exist");
                }
            }
            try(ResultSet resultSet = statement.executeQuery(TABLE_DEF_SQL +
                    request.getSchema() + POINT + request.getTableName() + QUOTES_PARENTHESES_SEMICOLON);){
                while (resultSet.next()) {
                    resultMap.put(RESULT, resultSet.getString(PG_GET_TABLEDEF));
                }
                log.info("tableDdl map is: " + resultMap);
                return resultMap;
            }
        }
    }
}
