package com.nctigba.observability.sql.model.history.result;

import com.fasterxml.jackson.annotation.JsonValue;
import com.nctigba.observability.sql.util.LocaleString;

public enum TaskType {
	MANUAL,
	AUTO;

	@JsonValue
	public String getValue() {
		return LocaleString.format("TaskType." + this.name());
	}
}