/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.agent.exception;

/**
 * AgentException
 *
 * @author: wangchao
 * @date: 2025/5/8 11:40
 * @since 7.0.0-RC2
 **/
public class AgentException extends RuntimeException {
    /**
     * agent exception constructor
     *
     * @param message message
     */
    public AgentException(String message) {
        super(message);
    }

    /**
     * agent exception constructor
     *
     * @param message message
     * @param cause cause
     */
    public AgentException(String message, Throwable cause) {
        super(message, cause);
    }
}
