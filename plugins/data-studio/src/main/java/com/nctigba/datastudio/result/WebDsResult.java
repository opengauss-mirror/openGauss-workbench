/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.result;

import static com.nctigba.datastudio.constants.CommonConstants.FIVE_HUNDRED;
import static com.nctigba.datastudio.constants.CommonConstants.TWO_HUNDRED;

/**
 * WebDsResult
 *
 * @since 2023-6-26
 */
public class WebDsResult {
    private String type;

    private String code;

    private String msg;

    private Object data;

    private String error;

    /**
     * ok
     *
     * @param type type
     * @param statusMsg statusMsg
     * @return WebDsResult
     */
    public static WebDsResult ok(String type, String statusMsg) {
        WebDsResult result = new WebDsResult();
        result.setType(type);
        result.setCode(TWO_HUNDRED);
        result.setMsg(statusMsg);
        return result;
    }

    /**
     * error
     *
     * @param type type
     * @param statusMsg statusMsg
     * @return WebDsResult
     */
    public static WebDsResult error(String type, String statusMsg) {
        WebDsResult result = new WebDsResult();
        result.setType(type);
        result.setCode(FIVE_HUNDRED);
        result.setMsg(statusMsg);
        return result;
    }

    /**
     * add data
     *
     * @param object object
     * @return WebDsResult
     */
    public WebDsResult addData(Object object) {
        this.setData(object);
        return this;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}