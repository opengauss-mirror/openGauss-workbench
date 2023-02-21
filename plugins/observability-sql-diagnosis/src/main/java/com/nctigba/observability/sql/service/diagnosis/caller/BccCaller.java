package com.nctigba.observability.sql.service.diagnosis.caller;

import java.sql.SQLException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.nctigba.observability.sql.mapper.DiagnosisTaskMapper;
import com.nctigba.observability.sql.model.diagnosis.Task;
import com.nctigba.observability.sql.model.diagnosis.TaskState;
import com.nctigba.observability.sql.model.diagnosis.grab.GrabType;
import com.nctigba.observability.sql.service.ClusterManager;

import cn.hutool.core.date.StopWatch;
import cn.hutool.http.HttpUtil;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BccCaller implements Caller {
	private final ClusterManager clusterManager;
	private final DiagnosisTaskMapper mapper;

	@Value("${agent.port:2321}")
	private String port;

	// private final DictionaryConfigMapper dictionaryConfigMapper;
	@Override
	@Async
	public void start(Task task) {
		Integer lwpid = null;
		var node = clusterManager.getOpsNodeById(task.getNodeId());
		try (var conn = node.connection()) {
			var watch = new StopWatch();
			watch.start();
			task.addRemarks("catching pid");
			mapper.updateById(task);
			// catch pid
			while (lwpid == null && task.running()) {
				var stmt = conn.createStatement();
				var rs = stmt.executeQuery("SELECT b.lwpid from pg_stat_activity a,dbe_perf.os_threads b "
						+ "where a.state != 'idle' and a.pid=b.pid and a.sessionid = '" + task.waitSessionId() + "'");
				if (rs.next())
					lwpid = rs.getInt(1);
				watch.stop();
				watch.start();
				if (watch.getTaskCount() > 10000)
					break;
				if (lwpid == null)
					try {
						Thread.sleep(10L);
					} catch (InterruptedException e) {
					}
			}
			task.addRemarks("pid catch times：" + watch.getTaskCount());
			task.getData().setPid(lwpid);
			task.addRemarks("pid:" + lwpid);
			if (lwpid == null) {
				task.addRemarks("pid catch fail");
				mapper.updateById(task);
				return;
			}
			mapper.updateById(task);
		} catch (SQLException e) {
			task.addRemarks("db permission fail");
			mapper.updateById(task);
			return;
		} catch (Exception e) {
			task.addRemarks(TaskState.err, e);
			mapper.updateById(task);
			return;
		}
		try {
			task.addRemarks("call bcc");
			mapper.updateById(task);
			// var config = dictionaryConfigMapper.selectById(task.getNodeId() +
			// "agentPort");
			var url = "http://" + node.getPublicIp() + ":"+ port + "/ebpf/v1/ebpfMonitor";
			// call post
			for (GrabType e : GrabType.values()) {
				if (e == GrabType.profile)
					if (!task.getConf().isOnCpu())
						continue;
				if (e == GrabType.offcputime)
					if (!task.getConf().isOffCpu())
						continue;
				if (e == GrabType.osParam)
					if (!task.getConf().isParamAnalysis())
						continue;
				if (e.getAnalysis() == null)
					continue;
				String postForObject = HttpUtil.post(url,
						Map.of("tid", lwpid, "taskid", task.getId(), "monitorType", e));
				task.addRemarks("bccResult for :" + e + ":" + (postForObject == null ? "" : postForObject));
			}
		} catch (Exception e) {
			task.addRemarks("bcc connect err", e);
			mapper.updateById(task);
		}
	}
}