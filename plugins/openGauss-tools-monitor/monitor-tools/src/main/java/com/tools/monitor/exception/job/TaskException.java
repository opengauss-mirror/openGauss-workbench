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

package com.tools.monitor.exception.job;

/**
 * TaskException
 *
 * @author liu
 * @since 2022-10-01
 */
public class TaskException extends Exception {
    private static final long serialVersionUID = 1L;

    private Code code;

    public TaskException(String msg, Code code) {
        this(msg, code, null);
    }

    /**
     * TaskException
     *
     * @param msg msg
     * @param code code
     * @param nestedEx nestedEx
     */
    public TaskException(String msg, Code code, Exception nestedEx) {
        super(msg, nestedEx);
        this.code = code;
    }

    /**
     * getCode
     *
     * @return Code
     */
    public Code getCode() {
        return code;
    }

    /**
     * Code
     */
    public enum Code {
        TASK_EXISTS, NO_TASK_EXISTS, TASK_ALREADY_STARTED, UNKNOWN, CONFIG_ERROR, TASK_NODE_NOT_AVAILABLE
    }
}