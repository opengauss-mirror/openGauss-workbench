package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.model.dto.DatabaseCreateViewDTO;
import com.nctigba.datastudio.model.dto.DatabaseSelectViewDTO;
import com.nctigba.datastudio.model.dto.DatabaseViewDdlDTO;
import com.nctigba.datastudio.service.DatabaseViewService;
import com.nctigba.datastudio.util.DebugUtils;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

import static com.nctigba.datastudio.constants.SqlConstants.COMMON_VIEW_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.CONNECTIVES_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.CREATE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.DROP_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.IF_EXISTS_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.LIMIT_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.MATERIALIZED_VIEW_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.POINT;
import static com.nctigba.datastudio.constants.SqlConstants.SELECT_VIEWNAME_DDL_WHERE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.SELECT_VIEW_DDL_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.SELECT_VIEW_DDL_WHERE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.TABLE_DATA_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.VIEW_KEYWORD_SQL;

@Slf4j
@Service
public class DatabaseViewServiceImpl implements DatabaseViewService {
    @Autowired
    private ConnectionConfig connectionConfig;

    @Override
    public String splicingViewDDL(DatabaseCreateViewDTO request) throws Exception {
        log.info("splicingViewDDL request is: " + request);
        String viewType;
        if (request.getViewType().equals("MATERIALIZED")) {
            viewType = MATERIALIZED_VIEW_SQL;
        } else {
            viewType = COMMON_VIEW_SQL;
        }
        String ddl = CREATE_SQL + viewType + VIEW_KEYWORD_SQL + request.getSchema() + POINT + request.getViewName() + "\n" + CONNECTIVES_SQL + "\n" + request.getSql();
        log.info("splicingViewDDL response is: " + ddl);
        return ddl;
    }

    @Override
    public String createViewDDL(DatabaseCreateViewDTO request) throws Exception {
        log.info("createViewDDL request is: " + request);
        String ddl = splicingViewDDL(request);
        log.info("createViewDDL response is: " + ddl);
        return ddl;
    }

    @Override
    public String returnViewDDL(DatabaseViewDdlDTO request) throws Exception {
        log.info("returnViewDDL request is: " + request);
        try {
            ResultSet resultSet = returnViewDDLData(request);
            String viewType;
            String ddl = null;
            while (resultSet.next()) {
                if (resultSet.getString("relkind").equals("m")) {
                    viewType = MATERIALIZED_VIEW_SQL;
                } else {
                    viewType = COMMON_VIEW_SQL;
                }
                ddl = CREATE_SQL + viewType + VIEW_KEYWORD_SQL + resultSet.getString("schemaname") + POINT + resultSet.getString("matviewname") + "\n" + CONNECTIVES_SQL + "\n" + resultSet.getString("definition");
            }
            log.info("returnViewDDL response is: " + ddl);
            return ddl;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.getMessage());
        }

    }

    @Override
    public ResultSet returnViewDDLData(DatabaseViewDdlDTO request) throws Exception {
        log.info("returnViewDDLData request is: " + request);
        try {
            String selectsql = SELECT_VIEW_DDL_SQL + request.getSchema() + SELECT_VIEWNAME_DDL_WHERE_SQL + request.getViewName() + SELECT_VIEW_DDL_WHERE_SQL;
            Connection connection = connectionConfig.connectDatabase(request.getUuid());
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(selectsql);
            log.info("returnViewDDLData sql is: " + selectsql);
            log.info("returnViewDDLData response is: " + resultSet);
            return resultSet;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.getMessage());
        }

    }

    @Override
    public void createView(DatabaseCreateViewDTO request) {
        log.info("createView request is: " + request);
        try {
            String ddl = splicingViewDDL(request);
            Connection connection = connectionConfig.connectDatabase(request.getUuid());
            Statement statement = connection.createStatement();
            statement.execute(ddl);
            log.info("createView response is: " + ddl);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.getMessage());
        }
    }

    @Override
    public void dropView(DatabaseViewDdlDTO request) {
        log.info("dropView request is: " + request);
        try {
            ResultSet resultSet = returnViewDDLData(request);
            Connection connection = connectionConfig.connectDatabase(request.getUuid());
            Statement statement = connection.createStatement();
            while (resultSet.next()) {
                if (resultSet.getString("relkind").equals("m")) {
                    String materializedSql = DROP_SQL + MATERIALIZED_VIEW_SQL + VIEW_KEYWORD_SQL + IF_EXISTS_SQL + request.getSchema() + POINT + request.getViewName();
                    statement.execute(materializedSql);
                    log.info("dropView sql is: " + materializedSql);
                } else {
                    String sql = DROP_SQL + VIEW_KEYWORD_SQL + request.getSchema() + POINT + request.getViewName();
                    statement.execute(sql);
                    log.info("dropView sql is: " + sql);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.getMessage());
        }
    }

    @Override
    public Map<String, Object> selectView(DatabaseSelectViewDTO request) {
        log.info("selectView request is: " + request);
        try {
            String ddl = TABLE_DATA_SQL + request.getSchema() + POINT + request.getViewName() + LIMIT_SQL;
            Connection connection = connectionConfig.connectDatabase(request.getUuid());
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(ddl);
            Map<String, Object> resultMap = DebugUtils.parseResultSet(resultSet);
            log.info("selectView sql is: " + ddl);
            log.info("selectView response is: " + resultMap);
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.getMessage());
        }
    }
}
