package com.nctigba.common.web.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CustomExceptionEnum {
	SUCCESS(200, "200"),
	NOT_FOUND_ERROR(404, "404"),
	INTERNAL_SERVER_ERROR(500, "500"),
	PARAM_INVALID_ERROR(400, "400"),
	UNAUTHORIZED_ERROR(401, "401"),
	FORBIDDEN_ERROR(403, "403"),
	GET_TOKEN_ERROR(502, "502"),
	TOLSQL_IS_RIGHT_PARAM(602, "602"),
	/**
	 * monitoring data is empty
	 */
	MONITORING_ACCESS_DATA_ERROR(601, "601");


	private final Integer code;
	private final String msg;
}