/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history.core;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.parser.ParserException;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.nctigba.common.web.exception.HisDiagnosisException;
import com.nctigba.observability.sql.mapper.history.HisDiagnosisTaskMapper;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.result.TaskState;
import com.nctigba.observability.sql.service.ClusterManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * SqlValidator
 *
 * @author luomeng
 * @since 2023/9/21
 */
@Service
@RequiredArgsConstructor
public class SqlValidator {
    private final static int RECONNECT_NUM = 3;

    private final HisDiagnosisTaskMapper mapper;
    private final ClusterManager clusterManager;

    /**
     * Run diagnosis task before sql check
     *
     * @param task task info
     * @throws ParserException if sql parser error
     */
    public void beforeStart(HisDiagnosisTask task) throws ParserException {
        task.addRemarks("before:sql check");
        mapper.updateById(task);
        try {
            task.setSql(formatSql(task.getSql()));
            task.addRemarks("sql check succ");
            mapper.updateById(task);
        } catch (ParserException e) {
            task.addRemarks(TaskState.SQL_PARSE_ERROR, e);
            task.setTaskEndTime(new Date());
            task.setSpan(task.getCost());
            mapper.updateById(task);
            throw new HisDiagnosisException("sql parse err", e);
        }
        task.addRemarks("sql check success");
        mapper.updateById(task);
        for (int i = 0; i < RECONNECT_NUM; i++) {
            try (var conn = clusterManager.getConnectionByNodeId(task.getNodeId())) {
                task.addRemarks("db conn check successes:" + conn.toString());
                mapper.updateById(task);
                return;
            } catch (SQLException e) {
                e.printStackTrace();
                task.addRemarks("db conn fail " + i + "times");
                if (i == 2) {
                    task.addRemarks(TaskState.DATABASE_CONNECT_ERROR, i);
                    task.setTaskEndTime(new Date());
                    task.setSpan(task.getCost());
                    mapper.updateById(task);
                    throw new HisDiagnosisException("db connect err", e);
                }
            }
        }
    }

    private static String formatSql(String sql) {
        try {
            SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, DbType.gaussdb);
            List<SQLStatement> statementList = parser.parseStatementList();
            return SQLUtils.toSQLString(statementList, DbType.gaussdb, null, null);
        } catch (ParserException e) {
            SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, DbType.postgresql);
            List<SQLStatement> statementList = parser.parseStatementList();
            return SQLUtils.toSQLString(statementList, DbType.postgresql, null, null);
        }
    }
}