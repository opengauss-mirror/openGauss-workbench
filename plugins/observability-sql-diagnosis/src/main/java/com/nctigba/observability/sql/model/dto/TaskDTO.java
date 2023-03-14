package com.nctigba.observability.sql.model.dto;

import org.apache.commons.lang3.StringUtils;
import org.opengauss.admin.common.exception.CustomException;

import com.nctigba.observability.sql.model.diagnosis.Task;
import com.nctigba.observability.sql.model.diagnosis.Task.config;
import com.nctigba.observability.sql.model.diagnosis.TaskState;
import com.nctigba.observability.sql.model.diagnosis.TaskType;

import lombok.Data;

@Data
public class TaskDTO {
	private String clusterId;
	private String nodeId;
	private String dbName;
	private String sqlId;
	private String name;
	private String sql;
	private Boolean onCpu;
	private Boolean offCpu;
	private Boolean explainAnalysis;
	private Boolean paramAnalysis;

	public Task toTask() {
		if (StringUtils.isAnyEmpty(clusterId, nodeId, dbName, name, sql))
			throw new CustomException("");
		return new Task().setClusterId(clusterId).setNodeId(nodeId).setSqlId(sqlId).setDbName(dbName).setName(name)
				.setSql(sql).setConf(new config().setOnCpu(onCpu).setOffCpu(offCpu).setExplainAnalysis(explainAnalysis)
						.setParamAnalysis(paramAnalysis))
				.setTasktype(TaskType.manual).setState(TaskState.create);
	}
}
