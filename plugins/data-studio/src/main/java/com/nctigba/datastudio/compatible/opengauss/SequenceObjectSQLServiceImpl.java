/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.compatible.opengauss;

import com.nctigba.datastudio.compatible.SequenceObjectSQLService;
import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.model.dto.DatabaseCreateSequenceDTO;
import com.nctigba.datastudio.model.dto.DatabaseDropSequenceDTO;
import com.nctigba.datastudio.model.dto.DatabaseSequenceDdlDTO;
import com.nctigba.datastudio.util.DebugUtils;
import com.nctigba.datastudio.util.LocaleString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static com.nctigba.datastudio.constants.CommonConstants.OPENGAUSS;
import static com.nctigba.datastudio.constants.SqlConstants.CACHE_KEYWORD_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.CREATE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.CYCLE_KEYWORD_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.CYCLE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.DROP_SEQUENCE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.INCREMENT_BY_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.INCREMENT_KEYWORD_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.LF;
import static com.nctigba.datastudio.constants.SqlConstants.MAXVALUE_KEYWORD_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.MAXVALUE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.MINVALUE_KEYWORD_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.MINVALUE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.NO_MINVALUE_KEYWORD_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.NO_MINVALUE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.OWNED_KEYWORD_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.POINT;
import static com.nctigba.datastudio.constants.SqlConstants.SELECT_SEQUENCE_COUNT_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.SELECT_SEQUENCE_DDL_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.SEMICOLON;
import static com.nctigba.datastudio.constants.SqlConstants.SEQUENCE_KEYWORD_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.SEQUENCE_SET_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.START_KEYWORD_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.START_WITH_SQL;
import static org.apache.commons.lang3.StringUtils.isNumeric;

/**
 * SequenceObjectSQLService achieve
 *
 * @since 2023-09-25
 */
@Slf4j
@Service
public class SequenceObjectSQLServiceImpl implements SequenceObjectSQLService {
    @Autowired
    private ConnectionConfig connectionConfig;

    @Override
    public String type() {
        return OPENGAUSS;
    }

    @Override

    public String splicingSequenceDDL(DatabaseCreateSequenceDTO request) {
        log.info("splicingSequenceDDL request is: " + request);
        String ddl =
                CREATE_SQL + SEQUENCE_KEYWORD_SQL + DebugUtils.containsSqlInjection(
                        DebugUtils.needQuoteName(request.getSchema())) + POINT
                        + DebugUtils.containsSqlInjection(DebugUtils.needQuoteName(request.getSequenceName()));
        if (isNumeric(request.getStart())) {
            ddl = ddl + LF + START_KEYWORD_SQL + DebugUtils.containsSqlInjection(request.getStart());
        }
        if (isNumeric(request.getIncrement())) {
            ddl = ddl + LF + INCREMENT_KEYWORD_SQL + DebugUtils.containsSqlInjection(request.getIncrement());
        }
        if (isNumeric(request.getMinValue())) {
            ddl = ddl + LF + MINVALUE_KEYWORD_SQL + DebugUtils.containsSqlInjection(request.getMinValue());
        } else {
            ddl = ddl + LF + NO_MINVALUE_KEYWORD_SQL;
        }
        if (isNumeric(request.getMaxValue())) {
            ddl = ddl + LF + MAXVALUE_KEYWORD_SQL + DebugUtils.containsSqlInjection(request.getMaxValue());
        }
        if (isNumeric(request.getCache())) {
            ddl = ddl + LF + CACHE_KEYWORD_SQL + DebugUtils.containsSqlInjection(request.getCache());
        }
        if (request.getIsCycle()) {
            ddl = ddl + LF + CYCLE_KEYWORD_SQL;
        }
        if (StringUtils.isNotEmpty(request.getTableName())) {
            ddl =
                    ddl + LF + OWNED_KEYWORD_SQL + DebugUtils.containsSqlInjection(
                            DebugUtils.needQuoteName(request.getTableSchema())) + POINT
                            + DebugUtils.containsSqlInjection(request.getTableName());
        }
        if (StringUtils.isNotEmpty(request.getTableColumn())) {
            ddl = ddl + POINT + DebugUtils.containsSqlInjection(DebugUtils.needQuoteName(request.getTableColumn()));
        }
        ddl = ddl + SEMICOLON;
        log.info("splicingSequenceDDL response is: " + ddl);
        return ddl;
    }

    @Override
    public String dropSequenceDDL(DatabaseDropSequenceDTO request) {
        log.info("dropSequenceDDL request is: " + request);
        String sql = String.format(DROP_SEQUENCE_SQL, DebugUtils.needQuoteName(request.getSchema()),
                DebugUtils.needQuoteName(request.getSequenceName()));
        return sql;
    }

    @Override
    public String returnSequenceDDL(DatabaseSequenceDdlDTO request) throws SQLException {
        log.info("returnSequenceDDL request is: " + request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
            String selectSql = String.format(SELECT_SEQUENCE_DDL_SQL,
                    DebugUtils.needQuoteName(request.getSchema()), DebugUtils.needQuoteName(request.getSequenceName()));
            try (
                    ResultSet countResult = statement.executeQuery(String.format(SELECT_SEQUENCE_COUNT_SQL,
                            request.getSchema(), request.getSequenceName()))
            ) {
                countResult.next();
                int count = countResult.getInt("count");
                if (count == 0) {
                    throw new CustomException(LocaleString.transLanguage("2012"));
                }
            }
            String ddl = "";
            try (
                    ResultSet resultSet = statement.executeQuery(selectSql)
            ) {
                while (resultSet.next()) {
                    ddl = SEQUENCE_SET_SQL + resultSet.getString("sequence_schema") + SEMICOLON + LF
                            + CREATE_SQL + SEQUENCE_KEYWORD_SQL + resultSet.getString("sequence_name") + LF
                            + START_WITH_SQL + resultSet.getString("start_value") + LF
                            + INCREMENT_BY_SQL + resultSet.getString("increment") + LF;
                    if (resultSet.getInt("minimum_value") == 1) {
                        ddl = ddl + NO_MINVALUE_SQL + LF
                                + MAXVALUE_SQL + resultSet.getString("maximum_value") + LF;

                    } else {
                        ddl = ddl + MINVALUE_SQL + resultSet.getString("minimum_value") + LF
                                + MAXVALUE_SQL + resultSet.getString("maximum_value") + LF;

                    }
                    if (resultSet.getString("cycle_option").equals("YES")) {
                        ddl = ddl + CYCLE_SQL + LF;
                    }
                    ddl = ddl + SEMICOLON;
                }
                log.info("returnSequenceDDL response is: " + ddl);
            }
            return ddl;
        }
    }
}