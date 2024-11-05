/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package org.opengauss.exception;

/**
 * ApiTestException
 *
 * @since 2024/11/5
 */
public class ApiTestException extends RuntimeException {
    /**
     * no args constructor
     */
    public ApiTestException() {
    }

    /**
     * Constructor with the "message" parameter
     *
     * @param message message
     */
    public ApiTestException(String message) {
        super(message);
    }

    /**
     * Constructor with the "cause" parameter
     *
     * @param cause cause
     */
    public ApiTestException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor with the "message" and “cause” parameters
     *
     * @param message message
     * @param cause cause
     */
    public ApiTestException(String message, Throwable cause) {
        super(message, cause);
    }
}
