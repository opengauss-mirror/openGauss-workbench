package com.nctigba.observability.instance.model.param;

import lombok.Data;

@Data
public class ParamQuery {
	private String nodeId;
	private String password;
	private String isRefresh;
}