/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
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