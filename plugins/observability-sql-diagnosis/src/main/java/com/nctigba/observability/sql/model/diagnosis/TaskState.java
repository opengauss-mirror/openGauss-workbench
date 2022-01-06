package com.nctigba.observability.sql.model.diagnosis;

import com.fasterxml.jackson.annotation.JsonValue;
import com.nctigba.observability.sql.util.LocaleString;

public enum TaskState {
	create,
	waiting,
	sqlRunning,
	receiving,
	finish,

	databaseConnectErr,
	sqlParseErr,
	sqlErr,
	err,;

	@JsonValue
	public String getValue() {
		return LocaleString.format("TaskState." + this.name());
	}
}