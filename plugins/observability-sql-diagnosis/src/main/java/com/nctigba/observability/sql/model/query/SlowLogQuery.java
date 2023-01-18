package com.nctigba.observability.sql.model.query;

import java.util.Date;

import com.nctigba.common.web.model.query.PageBaseQuery;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SlowLogQuery extends PageBaseQuery {
	private String nodeId;
	private String dbName;
	private Date startTime;
	private Date finishTime;
}