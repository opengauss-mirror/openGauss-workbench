/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.compatible.opengauss;

import com.nctigba.datastudio.compatible.ViewObjectSQLService;
import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.model.dto.DatabaseCreateViewDTO;
import com.nctigba.datastudio.model.dto.DatabaseSelectViewDTO;
import com.nctigba.datastudio.model.dto.DatabaseViewDdlDTO;
import com.nctigba.datastudio.util.LocaleString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.OPENGAUSS;
import static com.nctigba.datastudio.constants.SqlConstants.COMMON_VIEW_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.CREATE_VIEW_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.DROP_MATERIALIZED_VIEW_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.DROP_VIEW_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.MATERIALIZED_VIEW_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.SELECT_VIEW_DDL_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.SELECT_VIEW_TYPE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.VIEW_DATA_SQL;

@Slf4j
@Service
public class ViewObjectSQLServiceImpl implements ViewObjectSQLService {
    @Autowired
    private ConnectionConfig connectionConfig;

    @Override
    public String type() {
        return OPENGAUSS;
    }

    @Override
    public String splicingViewDDL(DatabaseCreateViewDTO request) {
        log.info("splicingViewDDL request is: " + request);
        String viewType;
        if (request.getViewType().equals("MATERIALIZED")) {
            viewType = MATERIALIZED_VIEW_SQL;
        } else {
            viewType = COMMON_VIEW_SQL;
        }
        String ddl = String.format(CREATE_VIEW_SQL, viewType, request.getSchema(), request.getViewName(), request.getSql());
        log.info("splicingViewDDL response is: " + ddl);
        return ddl;
    }

    public Map<String, Object> returnViewDDLData(DatabaseViewDdlDTO request) {
        log.info("returnViewDDLData request is: " + request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
            String selectSql =
                    String.format(SELECT_VIEW_DDL_SQL, request.getSchema(), request.getViewName());
            try (
                    ResultSet count = statement.executeQuery(selectSql)
            ) {
                count.next();
                if (StringUtils.isBlank(String.valueOf(count.getString("relkind")))) {
                    throw new CustomException(LocaleString.transLanguage("2010"));
                }
            } catch (Exception e) {
                log.info(e.toString());
                throw new RuntimeException(e);
            }
            try (
                    ResultSet resultSet = statement.executeQuery(selectSql)
            ) {
                log.info("returnViewDDLData sql is: " + selectSql);
                log.info("returnViewDDLData response is: " + resultSet);
                Map<String, Object> resultMap = new HashMap<>();
                ResultSetMetaData metaData = resultSet.getMetaData();
                String column;
                while (resultSet.next()) {
                    for (int i = 0; i < metaData.getColumnCount(); i++) {
                        column = metaData.getColumnName(i + 1);
                        resultMap.put(column, resultSet.getString(column));
                    }
                }
                return resultMap;
            } catch (Exception e) {
                log.info(e.toString());
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            log.info(e.toString());
            throw new RuntimeException(e);
        }
    }

    @Override
    public String returnDatabaseViewDDL(DatabaseViewDdlDTO request) {
        log.info("returnViewDDL request is: " + request);
        try {
            Map<String, Object> resultMap = returnViewDDLData(request);
            String viewType;
            String ddl;
            if (resultMap.get("relkind").equals("m")) {
                viewType = MATERIALIZED_VIEW_SQL;
            } else {
                viewType = COMMON_VIEW_SQL;
            }
            ddl = String.format(CREATE_VIEW_SQL, viewType, resultMap.get("schemaname"), resultMap.get("matviewname"), resultMap.get("definition"));
            log.info("returnViewDDL response is: " + ddl);
            return ddl;
        } catch (Exception e) {
            log.info(e.toString());
            throw new RuntimeException(e);
        }
    }

    public String viewTypeData(DatabaseViewDdlDTO request) {
        log.info("viewAttributeData request is: " + request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
            String selectsql = String.format(SELECT_VIEW_TYPE_SQL, request.getSchema(), request.getViewName());
            try (
                    ResultSet resultSet = statement.executeQuery(selectsql)
            ) {
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
            log.info(e.toString());
            throw new RuntimeException(e);
        }
    }

    @Override
    public String returnDropViewSQL(DatabaseViewDdlDTO request) {
        log.info("dropView request is: " + request);
        String type = viewTypeData(request);
        if (type.equals("m")) {
            String materializedSql = String.format(DROP_MATERIALIZED_VIEW_SQL, request.getSchema(), request.getViewName());
            log.info("dropView sql is: " + materializedSql);
            return materializedSql;
        } else {
            String sql = String.format(DROP_VIEW_SQL, request.getSchema(), request.getViewName());
            log.info("dropView sql is: " + sql);
            return sql;
        }
    }

    @Override
    public String returnSelectViewSQL(DatabaseSelectViewDTO request) {
        log.info("selectView request is: " + request);
        String ddl = String.format(VIEW_DATA_SQL, request.getSchema(), request.getViewName());
        log.info("selectViewddl is: " + ddl);
        return ddl;
    }
}
