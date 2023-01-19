/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */

package com.tools.monitor.exception;

import lombok.Data;

/**
 * ParamsException
 *
 * @author liu
 * @since 2022-10-01
 */
@Data
public class ParamsException extends RuntimeException {
    private Integer code = 300;

    private String message = "System abnormality";

    /**
     * ParamsException
     */
    public ParamsException() {
        super("System abnormality!");
    }

    /**
     * ParamsException
     *
     * @param message message
     */
    public ParamsException(String message) {
        super(message);
        this.message = message;
    }

    /**
     * ParamsException
     *
     * @param code code
     */
    public ParamsException(Integer code) {
        super("System abnormality!");
        this.code = code;
    }

    /**
     * ParamsException
     *
     * @param code code
     * @param message message
     */
    public ParamsException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}
