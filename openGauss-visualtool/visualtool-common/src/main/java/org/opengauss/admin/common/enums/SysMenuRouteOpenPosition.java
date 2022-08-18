package org.opengauss.admin.common.enums;

/**
 * @description: route postion
 * @author: xielibo
 * @date: 2022-09-05 9:00 PM
 **/
public enum SysMenuRouteOpenPosition {

    LEFT_MENU_BAR(1, "left menu bar"),
    INDEX_INSTANCE_DATA(2, "index instance");

    private final Integer code;
    private final String info;

    SysMenuRouteOpenPosition(Integer code, String info) {
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
