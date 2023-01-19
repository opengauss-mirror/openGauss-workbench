package com.tools.monitor.entity;

/**
 * ResponseCode
 *
 * @author liu
 * @since 2022-10-01
 */
public enum ResponseCode {
    RESPONSE_SUCCESS(200, "success"),

    RESPONSE_UNAUTH(300, "unauth"),

    RESPONSE_ERROR(400, "error"),

    RESPONSE_EXCEPTION(500, "exception"),

    RESPONSE_TARGET_ERR(600, "target_err");

    private Integer code;

    private String remark;

    private ResponseCode(Integer code, String remark) {
        this.code = code;
        this.remark = remark;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
