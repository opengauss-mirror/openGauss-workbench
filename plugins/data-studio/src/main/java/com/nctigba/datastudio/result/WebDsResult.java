package com.nctigba.datastudio.result;

public class WebDsResult {
    private String type;

    private String code;

    private String msg;

    private Object data;

    private String error;

    public static WebDsResult ok(String type, String statusMsg) {
        WebDsResult result = new WebDsResult();
        result.setType(type);
        result.setCode("200");
        result.setMsg(statusMsg);
        return result;
    }

    public static WebDsResult error(String type, String statusMsg) {
        WebDsResult result = new WebDsResult();
        result.setType(type);
        result.setCode("500");
        result.setMsg(statusMsg);
        return result;
    }

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