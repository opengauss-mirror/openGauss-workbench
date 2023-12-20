/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  SqlValidator.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/core/SqlValidator.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl.core;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.parser.ParserException;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.nctigba.observability.sql.exception.HisDiagnosisException;
import com.nctigba.observability.sql.enums.TaskStateEnum;
import com.nctigba.observability.sql.mapper.DiagnosisTaskMapper;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.service.impl.ClusterManager;
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

    private final DiagnosisTaskMapper mapper;
    private final ClusterManager clusterManager;

    /**
     * Run diagnosis task before sql check
     *
     * @param task task info
     * @throws ParserException if sql parser error
     */
    public void beforeStart(DiagnosisTaskDO task) throws ParserException {
        task.addRemarks("before:sql check");
        mapper.updateById(task);
        try {
            formatSql(task.getSql());
            task.addRemarks("sql check succ");
            mapper.updateById(task);
        } catch (ParserException e) {
            task.addRemarks(TaskStateEnum.SQL_PARSE_ERROR, e);
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
                    task.addRemarks(TaskStateEnum.DATABASE_CONNECT_ERROR, i);
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