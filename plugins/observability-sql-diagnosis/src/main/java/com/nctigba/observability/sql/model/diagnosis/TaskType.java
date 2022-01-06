package com.nctigba.observability.sql.model.diagnosis;

import com.fasterxml.jackson.annotation.JsonValue;
import com.nctigba.observability.sql.util.LocaleString;

public enum TaskType {
	manual,
	auto;

	@JsonValue
	public String getValue() {
		return LocaleString.format("TaskType." + this.name());
	}
}