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
