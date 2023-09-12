/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.exception;

import com.nctigba.observability.instance.util.MessageSourceUtil;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 功能描述
 *
 * @author liupengfei
 * @since 2023/6/20
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class BaseI18nException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private Integer code;

    private String message;

    private Object[] args;

    public BaseI18nException(String message) {
        this.message = message;
    }

    public BaseI18nException(String message, Integer code) {
        this.message = message;
        this.code = code;
    }

    public BaseI18nException(String message, Integer code, Object[] args) {
        this.message = message;
        this.code = code;
        this.args = args;
    }

    public BaseI18nException(String message, Throwable e) {
        super(message, e);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return MessageSourceUtil.getMsg(message, args);
    }

    public Integer getCode() {
        return code;
    }

}
