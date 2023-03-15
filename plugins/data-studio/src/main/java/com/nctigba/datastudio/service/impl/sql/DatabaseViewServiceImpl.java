package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.model.dto.DatabaseCreateViewDTO;
import com.nctigba.datastudio.model.dto.DatabaseSelectViewDTO;
import com.nctigba.datastudio.model.dto.DatabaseViewDdlDTO;
import com.nctigba.datastudio.service.DatabaseViewService;
import com.nctigba.datastudio.util.DebugUtils;
import com.nctigba.datastudio.util.LocaleString;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static com.nctigba.datastudio.constants.SqlConstants.*;

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
        try{
            Map<String, Object> resultMap = returnViewDDLData(request);
            String viewType;
            String ddl = null;
            if (resultMap.get("relkind").equals("m")) {
                viewType = MATERIALIZED_VIEW_SQL;
            } else {
                viewType = COMMON_VIEW_SQL;
            }
            ddl = CREATE_SQL + viewType + VIEW_KEYWORD_SQL + resultMap.get("schemaname") + POINT + resultMap.get("matviewname") + "\n" + CONNECTIVES_SQL + "\n" + resultMap.get("definition");

            log.info("returnViewDDL response is: " + ddl);
            return ddl;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.getMessage(),e);
        }

    }

    @Override
    public Map<String, Object> returnViewDDLData(DatabaseViewDdlDTO request) throws Exception {
        log.info("returnViewDDLData request is: " + request);
        try(Connection connection = connectionConfig.connectDatabase(request.getUuid());){
            Statement statement = connection.createStatement();
            String selectsql = SELECT_VIEW_DDL_SQL + request.getSchema() + SELECT_VIEWNAME_DDL_WHERE_SQL + request.getViewName() + SELECT_VIEW_DDL_WHERE_SQL;
            try(ResultSet count = statement.executeQuery(SELECT_KEYWORD_SQL + COUNT_SQL +FROM_CLASS_SQL+
                    request.getSchema() + FROM_CLASS_WHERE_SQL + request.getViewName() + RELKIND_SQL+ "v" + QUOTES_SEMICOLON)){
                count.next();
                int a = count.getInt("count");
                if(a==0){
                    throw new CustomException(LocaleString.transLanguage("2010"));
                }
            }
            ResultSet resultSet = statement.executeQuery(selectsql);
            log.info("returnViewDDLData sql is: " + selectsql);
            log.info("returnViewDDLData response is: " + resultSet);
            Map<String, Object> resultMap = new HashMap<>();
            ResultSetMetaData metaData = resultSet.getMetaData();
            String column;
            while (resultSet.next()) {
                for (int i = 0; i < metaData.getColumnCount(); i++) {
                    column = metaData.getColumnName(i + 1);
                    resultMap.put(column,resultSet.getString(column));
                }
            }
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.getMessage(),e);
        }

    }

    public String viewTypeData(DatabaseViewDdlDTO request) throws Exception {
        log.info("viewAttributeData request is: " + request);
        try (Connection connection = connectionConfig.connectDatabase(request.getUuid());) {
            Statement statement = connection.createStatement();
            String selectsql = SELECT_VIEW_TYPE_SQL + request.getSchema() + SELECT_VIEWNAME_DDL_WHERE_SQL + request.getViewName() + SELECT_VIEW_DDL_WHERE_SQL;
            try (ResultSet resultSet = statement.executeQuery(selectsql)) {
                log.info("count sql is: " + resultSet);
                log.info("viewAttributeData sql is: " + selectsql);
                String type;
                if (resultSet.next()) {
                        type = resultSet.getString("relkind");
                } else {
                    throw new CustomException(LocaleString.transLanguage("2010"));
                }
                log.info("resultMap response is: " + type);
                return type;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.getMessage(),e);
        }

    }
    @Override
    public void createView(DatabaseCreateViewDTO request) {
        log.info("createView request is: " + request);
        try(Connection connection = connectionConfig.connectDatabase(request.getUuid());
            Statement statement = connection.createStatement();){
            String ddl = splicingViewDDL(request);
            statement.execute(ddl);
            log.info("createView response is: " + ddl);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.getMessage(),e);
        }
    }

    @Override
    public void dropView(DatabaseViewDdlDTO request) {
        log.info("dropView request is: " + request);
        try(Connection connection = connectionConfig.connectDatabase(request.getUuid());
            Statement statement = connection.createStatement();){

            String type = viewTypeData(request);
            if (type.equals("m")) {
                String materializedSql = DROP_SQL + MATERIALIZED_VIEW_SQL + VIEW_KEYWORD_SQL + IF_EXISTS_SQL + request.getSchema() + POINT + request.getViewName();
                statement.execute(materializedSql);
                log.info("dropView sql is: " + materializedSql);
            } else {
                String sql = DROP_SQL + VIEW_KEYWORD_SQL + request.getSchema() + POINT + request.getViewName();
                statement.execute(sql);
                log.info("dropView sql is: " + sql);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.getMessage(),e);
        }
    }

    @Override
    public Map<String, Object> selectView(DatabaseSelectViewDTO request) {
        log.info("selectView request is: " + request);
        String ddl = TABLE_DATA_SQL + request.getSchema() + POINT + request.getViewName() + LIMIT_SQL;
        try(Connection connection = connectionConfig.connectDatabase(request.getUuid());
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(ddl);){
            Map<String, Object> resultMap = DebugUtils.parseResultSet(resultSet);
            log.info("selectView sql is: " + ddl);
            log.info("selectView response is: " + resultMap);
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.getMessage(),e);
        }
    }
}
