package org.opengauss.admin.common.enums;

/**
 * task type
 *
 * @author xielibo
 */
public enum SysTaskType {

    DATA_MIGRATION(1, "data_migration");

    private final Integer code;
    private final String info;

    SysTaskType(Integer code, String info) {
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
