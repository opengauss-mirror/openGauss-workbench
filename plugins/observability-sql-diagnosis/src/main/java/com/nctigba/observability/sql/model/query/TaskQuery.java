package com.nctigba.observability.sql.model.query;

import java.util.Date;

import com.nctigba.common.web.model.query.PageBaseQuery;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TaskQuery extends PageBaseQuery {
	String clusterId;
	String nodeId;
	String dbName;
	String sqlId;
	String name;
	Date startTime;
	Date endTime;
}
