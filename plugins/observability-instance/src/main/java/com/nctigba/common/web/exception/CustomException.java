package com.nctigba.common.web.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class CustomException extends RuntimeException {
	private static final long serialVersionUID = -8284004937326482732L;

	public CustomException(String msg) {
		this(CustomExceptionEnum.INTERNAL_SERVER_ERROR, msg);
	}

	public CustomException(Integer code, String msg) {
		this(code, msg, null);
	}

	public CustomException(Throwable e) {
		this(CustomExceptionEnum.INTERNAL_SERVER_ERROR, e);
	}

	public CustomException(String msg, Throwable e) {
		this(CustomExceptionEnum.INTERNAL_SERVER_ERROR, msg, e);
	}

	public CustomException(Integer code, String msg, Throwable e) {
		super(msg, e, false, false);
		this.code = code;
	}

	public CustomException(CustomExceptionEnum enu) {
		this(enu.getCode(), enu.getMsg());
	}

	public CustomException(CustomExceptionEnum enu, String msg) {
		this(enu, msg, null);
	}

	public CustomException(CustomExceptionEnum enu, Throwable e) {
		this(enu, enu.getMsg(), e);
	}

	public CustomException(CustomExceptionEnum enu, String msg, Throwable e) {
		this(enu.getCode(), msg, e);
	}

	private Integer code;
}