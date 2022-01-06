package com.nctigba.observability.sql.service.diagnosis.caller;

import java.util.ArrayList;

import com.nctigba.observability.sql.model.diagnosis.Task;

public interface Caller {
	/**
	 * beforeStart<br/>
	 * if exception,cancel diagnosis
	 */
	default void beforeStart(Task task) {
	}
	/**
	 * when sql start call this,<br/>
	 * must add @async
	 */
	default void start(Task task) {
	}

	/**
	 * after sql success
	 * @param rsList explain analyze result
	 */
	default void afterSql(Task task, ArrayList<String> rsList) {
	}
}