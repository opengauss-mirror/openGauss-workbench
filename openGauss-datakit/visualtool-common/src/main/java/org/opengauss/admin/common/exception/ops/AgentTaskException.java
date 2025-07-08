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

package org.opengauss.admin.common.exception.ops;

/**
 * AgentTaskException
 *
 * @author: wangchao
 * @Date: 2025/4/21 16:34
 * @Description: AgentTaskException
 * @since 7.0.0-RC2
 **/
public class AgentTaskException extends RuntimeException {
    private String message;

    /**
     * AgentTaskException
     *
     * @param message message
     */
    public AgentTaskException(String message) {
        super(message);
        this.message = message;
    }

    /**
     * AgentTaskException
     *
     * @param message message
     * @param e error stacktrace
     */
    public AgentTaskException(String message, Throwable e) {
        super(message, e);
        this.message = message;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("AgentTaskException{");
        sb.append("message='").append(message).append('\'');
        sb.append(", cause=").append(getCause());
        sb.append(", stackTrace=").append(getFilteredStackTrace());
        sb.append('}');
        return sb.toString();
    }

    private String getFilteredStackTrace() {
        StackTraceElement[] stackTrace = getStackTrace();
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : stackTrace) {
            if (!element.getClassName().contains("org.opengauss")) {
                sb.append(element).append(System.lineSeparator());
            }
        }
        return sb.toString();
    }
}
