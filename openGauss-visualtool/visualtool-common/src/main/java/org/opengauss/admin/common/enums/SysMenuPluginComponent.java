package org.opengauss.admin.common.enums;

/**
 * @className: SysMenuPluginComponent
 * @description: SysMenuPluginComponent
 * @author: xielibo
 * @date: 2022-09-05 9:00 PM
 **/
public enum SysMenuPluginComponent {
    PAGE_IFRAME("iframe/index", "page iframe open"),
    WIN_IFRAME("iframe/index", "win iframe open");

    private final String code;
    private final String info;

    SysMenuPluginComponent(String code, String info) {
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
