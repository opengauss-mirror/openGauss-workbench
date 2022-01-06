package com.nctigba.observability.sql.service.diagnosis.caller;

import java.sql.SQLException;
import java.util.List;

import org.opengauss.admin.common.exception.CustomException;
import org.springframework.stereotype.Service;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.parser.ParserException;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.nctigba.observability.sql.mapper.DiagnosisTaskMapper;
import com.nctigba.observability.sql.model.diagnosis.Task;
import com.nctigba.observability.sql.model.diagnosis.TaskState;
import com.nctigba.observability.sql.service.ClusterManager;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SqlValidator implements Caller {
	private final DiagnosisTaskMapper mapper;
	private final ClusterManager clusterManager;

	@Override
	public void beforeStart(Task task) {
		task.addRemarks("before:sql check");
		mapper.updateById(task);
		try {
			task.setSql(formatSql(task.getSql()));
			task.addRemarks("sql check succ");
			mapper.updateById(task);
		} catch (ParserException e) {
			task.addRemarks(TaskState.sqlParseErr, e);
			mapper.updateById(task);
			throw new CustomException("sql parse err", e);
		}
		task.addRemarks("sql check success");
		mapper.updateById(task);
		for (int i = 0; i < 3; i++) {
			try (var conn = clusterManager.getConnectionByNodeId(task.getNodeId());) {
				task.addRemarks("db conn check succ");
				mapper.updateById(task);
				return;
			} catch (SQLException e) {
				e.printStackTrace();
				task.addRemarks("db conn fail " + i + "times");
				if (i == 2) {
					task.addRemarks(TaskState.databaseConnectErr, i);
					mapper.updateById(task);
					throw new CustomException("db connect err", e);
				}
			}
		}
	}

	public static final String formatSql(String sql) {
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