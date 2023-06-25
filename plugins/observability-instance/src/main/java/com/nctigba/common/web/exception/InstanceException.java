/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package com.nctigba.common.web.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class InstanceException extends BaseI18nException {
    private static final long serialVersionUID = 1L;

    public InstanceException(String message, Integer code) {
        super(message, code);
    }

    public InstanceException(String message, Integer code, Object[] args) {
        super(message, code, args);
    }

    public InstanceException(String message) {
        super(message, 500);
    }

    public InstanceException(String message, Throwable cause) {
        super(message, cause);
    }

    public InstanceException(InstanceExceptionMsgEnum msgEnum, Object... objs) {
        this(msgEnum.getMsg(), msgEnum.getCode(), objs);
    }

    public InstanceException(InstanceExceptionMsgEnum msgEnum) {
        this(msgEnum.getMsg(), msgEnum.getCode());
    }
}