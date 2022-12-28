package org.opengauss.admin.common.enums;

/**
 * user status
 *
 * @author xielibo
 */
public enum UserStatus {

    OK("0", "ok"),

    DISABLE("1", "disable"),

    DELETED("2", "delete");

    private final String code;
    private final String info;

    UserStatus(String code, String info) {
        this.code = code;
        this.info = info;
    }

    public String getCode() {
        return code;
    }

    public String getInfo() {
        return info;
    }
}
