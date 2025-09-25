/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.common.exception.ops;

import org.springframework.http.HttpStatusCode;

import java.util.Locale;

/**
 * AgentHealthCheckException
 *
 * @author: wangchao
 * @Date: 2025/9/10 15:36
 * @since 7.0.0-RC2
 **/
public class AgentHealthCheckException extends RuntimeException {
    private final HttpStatusCode statusCode;

    /**
     * agent health check exception
     *
     * @param message message
     * @param statusCode status code
     */
    public AgentHealthCheckException(String message, HttpStatusCode statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    /**
     * agent health check exception
     *
     * @param message message
     * @param cause cause
     */
    public AgentHealthCheckException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = null;
    }

    /**
     * status code
     *
     * @return status code
     */
    public HttpStatusCode getStatusCode() {
        return statusCode;
    }

    /**
     * error details
     *
     * @return details
     */
    public String getDetailedMessage() {
        if (statusCode != null) {
            return String.format(Locale.getDefault(), "HTTP %d: %s", statusCode.value(), getMessage());
        }
        return getMessage();
    }
}
