/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.common.exception.ops;

/**
 * AgentHealthCheckException
 *
 * @author: wangchao
 * @Date: 2025/9/10 15:36
 * @since 7.0.0-RC2
 **/
public class AgentConnectionException extends RuntimeException {
    /**
     * agent health check exception
     *
     * @param message message
     */
    public AgentConnectionException(String message) {
        super(message);
    }

    /**
     * agent health check exception
     *
     * @param message message
     * @param cause cause
     */
    public AgentConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
