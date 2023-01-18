package com.nctigba.observability.sql.model.dto;

import com.nctigba.observability.sql.model.diagnosis.Task;
import com.nctigba.observability.sql.model.diagnosis.Task.config;
import com.nctigba.observability.sql.model.diagnosis.TaskState;
import com.nctigba.observability.sql.model.diagnosis.TaskType;

import io.swagger.annotations.ApiParam;
import lombok.Data;

@Data
public class TaskDTO {
	@ApiParam(example = "clusterId", required = true)
	private String clusterId;
	@ApiParam(example = "nodeId", required = true)
	private String nodeId;
	@ApiParam(example = "dbName", required = true)
	private String dbName;
	@ApiParam(example = "sqlId", required = false)
	private String sqlId;
	private String name;
	private String sql;
	private Boolean onCpu;
	private Boolean offCpu;
	private Boolean explainAnalysis;

	public Task toTask() {
		return new Task().setClusterId(clusterId).setNodeId(nodeId).setSqlId(sqlId).setDbName(dbName).setName(name)
				.setSql(sql).setConf(new config().setOnCpu(onCpu).setOffCpu(offCpu).setExplainAnalysis(explainAnalysis))
				.setTasktype(TaskType.manual).setState(TaskState.create);
	}
}
