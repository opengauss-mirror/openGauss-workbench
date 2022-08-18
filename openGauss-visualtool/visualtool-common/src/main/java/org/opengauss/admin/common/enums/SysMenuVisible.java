package org.opengauss.admin.common.enums;

/**
 * @description: SysMenuVisible
 * @author: xielibo
 * @date: 2022-09-05 9:00 PM
 **/
public enum SysMenuVisible {

    HIDE("1", "hide"),
    SHOW("0", "show");

    private final String code;
    private final String info;

    SysMenuVisible(String code, String info) {
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
