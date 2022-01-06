package com.nctigba.observability.sql.model.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SlowLogRequest extends SlowLogQuery{
	private String type;
}