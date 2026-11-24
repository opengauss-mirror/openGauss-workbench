/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
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
