package com.nctigba.observability.sql.service.diagnosis;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.nctigba.observability.sql.mapper.DiagnosisTaskMapper;
import com.nctigba.observability.sql.model.diagnosis.Task;
import com.nctigba.observability.sql.model.diagnosis.TaskState;
import com.nctigba.observability.sql.service.ClusterManager;
import com.nctigba.observability.sql.service.diagnosis.caller.Caller;

import cn.hutool.core.util.RandomUtil;

@Service
@Slf4j
public class SqlExecuter {
	@Autowired
	private DiagnosisTaskMapper mapper;
	@Autowired
	private List<Caller> callers;
	@Autowired
	private ClusterManager clusterManager;

	public static final String SESSIONID_SQL = "select sessionid from pg_stat_activity where %d=%d and query like 'select sessionid from pg_stat_activity where %d=%d%%'";

	@Async
	public void executeSql(Task task) {
		try {
			var rsList = new ArrayList<String>();
			try (var conn = clusterManager.getConnectionByNodeId(task.getNodeId(), task.getDbName());) {
				Long sessionid = null;
				int count = 0;
				while (count++ < 100) {
					int rand = RandomUtil.randomInt();
					var sessionidSql = String.format(SESSIONID_SQL, rand, rand, rand, rand);
					try (var st = conn.createStatement(); var rs = st.executeQuery(sessionidSql)) {
						if (rs.next()) {
							sessionid = rs.getLong(1);
							task.getData().setSessionId(sessionid);
							task.addRemarks("sessionid:" + sessionid);
							break;
						}
					}
				}
				if (sessionid == null) {
					task.getConf().setExplainAnalysis(false);
					task.getData().setSessionId(0L);
					task.addRemarks("sessionid catch fail");
				}
				task.addRemarks(TaskState.sqlRunning);
				mapper.updateById(task);
				try (var stmt = conn.createStatement(); var rs = stmt.executeQuery(task.analySql());) {
					while (rs.next()) {
						rsList.add(rs.getString(1));
					}
				}
				task.addRemarks("results:" + rsList.size());
				mapper.updateById(task);
				Thread.sleep(1000L);
			} catch (SQLException e) {
				task.addRemarks(TaskState.sqlErr, e);
				mapper.updateById(task);
				return;
			} catch (InterruptedException e) {
				log.error("Interrupted!", e);
				Thread.currentThread().interrupt();
			}
			if (TaskState.receiving.compareTo(task.getState()) > 0) {
				task.addRemarks(TaskState.receiving);
				mapper.updateById(task);
			}
			task.addRemarks("after sql caller");
			for (Caller caller : callers) {
				caller.afterSql(task, rsList);
			}
			task.setState(TaskState.finish);
			task.setEndtime(new Date());
			mapper.updateById(task);
		} finally {
			TaskCreator.SEMAPHORE.release();
		}
	}
}