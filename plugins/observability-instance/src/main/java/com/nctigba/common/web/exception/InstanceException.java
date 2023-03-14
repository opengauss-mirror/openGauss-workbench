package com.nctigba.common.web.exception;

import org.opengauss.admin.common.exception.CustomException;

public class InstanceException extends CustomException {
    public InstanceException(String message, Integer code) {
        super(message, code);
    }

    public InstanceException(String message) {
        super(message,500);
    }
}