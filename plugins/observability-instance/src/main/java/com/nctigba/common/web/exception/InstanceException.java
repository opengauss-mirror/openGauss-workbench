package com.nctigba.common.web.exception;

import org.opengauss.admin.common.exception.CustomException;

public class InstanceException extends CustomException {
	private static final long serialVersionUID = 1L;

	public InstanceException(String message, Integer code) {
		super(message, code);
	}

	public InstanceException(String message) {
		super(message, 500);
	}

	public InstanceException(String message, Throwable cause) {
		super(message, cause);
	}
}