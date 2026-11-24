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
