package org.opengauss.admin.common.enums;

/**
 * @className: SysMenuPluginComponent
 * @description: SysMenuPluginOpenWay
 * @author: xielibo
 * @date: 2022-09-05 9:00 PM
 **/
public enum SysMenuPluginOpenWay {

    PAGE_OPEN(1, "page open"),
    WIN_OPEN(2, "win open");

    private final Integer code;
    private final String info;

    SysMenuPluginOpenWay(Integer code, String info) {
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
