package com.nctigba.common.web.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestResult<T> implements Serializable {
	private static final long serialVersionUID = 8020580562955884092L;
	private static final String SUCCESS_CODE = "200";
	private String code;
	private String msg;
	private  transient  T data;

	public static <T> RestResult<T> success() {
		return RestResult.<T>builder().code(SUCCESS_CODE).build();
	}

	public static <T> RestResult<T> success(T data) {
		return RestResult.<T>builder().code(SUCCESS_CODE).data(data).build();
	}

	public static <T> RestResult<T> success(String msg, T data) {
		return RestResult.<T>builder().code(SUCCESS_CODE).msg(msg).data(data).build();
	}
}