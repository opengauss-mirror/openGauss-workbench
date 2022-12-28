package org.opengauss.admin.common.enums;

/**
 * plugin status
 *
 * @author xielibo
 */
public enum SysPluginStatus {

    START(1, "started"),
    DISABLE(2, "stoped");

    private final Integer code;
    private final String info;

    SysPluginStatus(Integer code, String info) {
        this.code = code;
        this.info = info;
    }

    public Integer getCode() {
        return code;
    }

    public String getInfo() {
        return info;
    }
}
