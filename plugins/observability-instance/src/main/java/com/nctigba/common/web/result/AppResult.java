/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package com.nctigba.common.web.result;

import lombok.Data;

@Data
public class AppResult {
    private String code = "200";
    private String msg;
    private Object data;

    public static AppResult ok(String statusMsg) {
        AppResult result = new AppResult();
        result.setCode("200");
        result.setMsg(statusMsg);
        return result;
    }

    public static AppResult ok(Object object) {
        AppResult result = new AppResult();
        result.setCode("200");
        result.setMsg("success");
        result.setData(object);
        return result;
    }

    public static AppResult error(String statusMsg) {
        AppResult result = new AppResult();
        result.setCode("500");
        result.setMsg(statusMsg);
        return result;
    }

    public static AppResult error(String code, String statusMsg) {
        AppResult result = new AppResult();
        result.setCode(code);
        result.setMsg(statusMsg);
        return result;
    }

    public AppResult addData(Object object) {
        this.setData(object);
        return this;
    }
}