/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */

package com.tools.monitor.entity;

import cn.hutool.http.HttpStatus;

import com.tools.monitor.util.StringUtils;
import java.util.HashMap;

/**
 * MonitorResult
 *
 * @author liu
 * @since 2022-10-01
 */
public class MonitorResult extends HashMap<String, Object> {
    /**
     * code
     */
    public static final String CODE = "code";

    /**
     * message
     */
    public static final String MSG = "message";

    /**
     * Data object
     */
    public static final String DATA = "data";

    /**
     * Initializes a newly created MonitorResult object to represent an empty message
     */
    public MonitorResult() {
    }

    /**
     * Initialize a newly created MonitorResult object
     *
     * @param code code
     * @param msg  msg
     */
    public MonitorResult(int code, String msg) {
        super.put(CODE, code);
        super.put(MSG, msg);
    }

    /**
     * Initialize a newly created MonitorResult object
     *
     * @param code code
     * @param msg  msg
     * @param data data
     */
    public MonitorResult(int code, String msg, Object data) {
        super.put(CODE, code);
        super.put(MSG, msg);
        if (StringUtils.isNotNull(data)) {
            super.put(DATA, data);
        }
    }

    /**
     * Return success message
     *
     * @return MonitorResult
     */
    public static MonitorResult success() {
        return MonitorResult.success("操作成功");
    }

    /**
     * success
     *
     * @param data data
     * @return MonitorResult
     */
    public static MonitorResult success(Object data) {
        return MonitorResult.success("操作成功", data);
    }

    /**
     * Return success message
     *
     * @param msg msg
     * @return MonitorResult
     */
    public static MonitorResult success(String msg) {
        return MonitorResult.success(msg, null);
    }

    /**
     * Return success message
     *
     * @param msg  msg
     * @param data data
     * @return MonitorResult
     */
    public static MonitorResult success(String msg, Object data) {
        return new MonitorResult(HttpStatus.HTTP_OK, msg, data);
    }

    /**
     * Return error message
     *
     * @return MonitorResult
     */
    public static MonitorResult error() {
        return MonitorResult.error("操作失败");
    }

    /**
     * Return error message
     *
     * @param msg msg
     * @return MonitorResult
     */
    public static MonitorResult error(String msg) {
        return MonitorResult.error(msg, null);
    }

    /**
     * Return error message
     *
     * @param msg  msg
     * @param data data
     * @return MonitorResult
     */
    public static MonitorResult error(String msg, Object data) {
        return new MonitorResult(HttpStatus.HTTP_BAD_REQUEST, msg, data);
    }

    /**
     * target
     *
     * @param msg msg
     * @return MonitorResult
     */
    public static MonitorResult target(String msg) {
        return new MonitorResult(600, msg);
    }

    /**
     * Return error message
     *
     * @param code code
     * @param msg  msg
     * @return MonitorResult
     */
    public static MonitorResult error(int code, String msg) {
        return new MonitorResult(code, msg, null);
    }

    /**
     * Convenient chained call
     *
     * @param key   key
     * @param value value
     * @return MonitorResult
     */
    @Override
    public MonitorResult put(String key, Object value) {
        super.put(key, value);
        return this;
    }
}
