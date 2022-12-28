package org.opengauss.admin.common.enums;

/**
 * plugin theme
 *
 * @author xielibo
 */
public enum SysPluginTheme {

    DARK("dark", "dark"),

    LIGHT("light", "light");

    private final String code;
    private final String info;

    SysPluginTheme(String code, String info) {
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
