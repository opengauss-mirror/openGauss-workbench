package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.model.query.SelectDataQuery;
import com.nctigba.datastudio.service.TableDataService;
import com.nctigba.datastudio.util.DebugUtils;
import lombok.extern.slf4j.Slf4j;
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
import static com.nctigba.datastudio.constants.SqlConstants.ATTRELID_CONDITION_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.CONSTRAINT_GROUP_BY_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.GET_CLASS_OID_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.GET_COLUMN_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.GET_CONSTRAINT_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.GET_INDEX_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.INDEX_GROUP_BY_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.LIMIT_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.NSPNAME_CONDITION;
import static com.nctigba.datastudio.constants.SqlConstants.POINT;
import static com.nctigba.datastudio.constants.SqlConstants.QUOTES_PARENTHESES_SEMICOLON;
import static com.nctigba.datastudio.constants.SqlConstants.QUOTES_SEMICOLON;
import static com.nctigba.datastudio.constants.SqlConstants.RELNAME_CONDITION;
import static com.nctigba.datastudio.constants.SqlConstants.SEMICOLON;
import static com.nctigba.datastudio.constants.SqlConstants.TABLE_DATA_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.TABLE_DEF_SQL;

@Slf4j
@Service
public class TableDataServiceImpl implements TableDataService {
    @Autowired
    private ConnectionConfig connectionConfig;

    @Override
    public Map<String, Object> tableData(SelectDataQuery request) throws Exception {
        log.info("tableData request is: " + request);
        Connection connection = connectionConfig.connectDatabase(request.getConnName(), request.getWebUser());
        Statement statement = connection.createStatement();

        ResultSet resultSet = statement.executeQuery(TABLE_DATA_SQL +
                request.getSchema() + POINT + request.getTableName() + LIMIT_SQL + SEMICOLON);
        Map<String, Object> resultMap = DebugUtils.parseResultSet(resultSet);
        log.info("tableData resultMap is: " + resultMap);
        return resultMap;
    }

    @Override
    public Map<String, Object> tableColumn(SelectDataQuery request) throws Exception {
        log.info("tableColumn request is: " + request);
        Connection connection = connectionConfig.connectDatabase(request.getConnName(), request.getWebUser());
        Statement statement = connection.createStatement();

        ResultSet oidResult = statement.executeQuery(GET_CLASS_OID_SQL + request.getTableName()
                + NSPNAME_CONDITION + request.getSchema() + QUOTES_SEMICOLON);
        String oid = "";
        while (oidResult.next()) {
            oid = oidResult.getString(OID);
        }

        ResultSet resultSet = statement.executeQuery(GET_COLUMN_SQL + oid + ATTRELID_CONDITION_SQL + oid + SEMICOLON);
        Map<String, Object> resultMap = DebugUtils.parseResultSet(resultSet);
        log.info("tableColumn resultMap is: " + resultMap);
        return resultMap;
    }

    @Override
    public Map<String, Object> tableIndex(SelectDataQuery request) throws Exception {
        log.info("tableIndex request is: " + request);
        Connection connection = connectionConfig.connectDatabase(request.getConnName(), request.getWebUser());
        Statement statement = connection.createStatement();

        ResultSet resultSet = statement.executeQuery(GET_INDEX_SQL + request.getTableName()
                + NSPNAME_CONDITION + request.getSchema() + INDEX_GROUP_BY_SQL);
        Map<String, Object> resultMap = DebugUtils.parseResultSet(resultSet);
        log.info("tableIndex resultMap is: " + resultMap);
        return resultMap;
    }

    @Override
    public Map<String, Object> tableConstraint(SelectDataQuery request) throws Exception {
        log.info("tableConstraint request is: " + request);
        Connection connection = connectionConfig.connectDatabase(request.getConnName(), request.getWebUser());
        Statement statement = connection.createStatement();

        ResultSet resultSet = statement.executeQuery(GET_CONSTRAINT_SQL + request.getSchema()
                + RELNAME_CONDITION + request.getTableName() + CONSTRAINT_GROUP_BY_SQL);
        Map<String, Object> resultMap = DebugUtils.parseResultSet(resultSet);
        log.info("tableConstraint resultMap is: " + resultMap);
        return resultMap;
    }

    @Override
    public Map<String, String> tableDdl(SelectDataQuery request) throws Exception {
        log.info("tableDdl request is: " + request);
        Map<String, String> resultMap = new HashMap<>();
        Connection connection = connectionConfig.connectDatabase(request.getConnName(), request.getWebUser());
        Statement statement = connection.createStatement();

        ResultSet resultSet = statement.executeQuery(TABLE_DEF_SQL +
                request.getSchema() + POINT + request.getTableName() + QUOTES_PARENTHESES_SEMICOLON);
        while (resultSet.next()) {
            resultMap.put(RESULT, resultSet.getString(PG_GET_TABLEDEF));
        }
        log.info("tableDdl map is: " + resultMap);
        return resultMap;
    }
}
