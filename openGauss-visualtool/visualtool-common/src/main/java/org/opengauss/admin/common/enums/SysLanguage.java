package org.opengauss.admin.common.enums;

/**
 * @className: SysLanguage
 * @description: SysLanguage
 * @author: xielibo
 * @date: 2022-11-16 16:39
 **/
public enum SysLanguage {

    //zh
    ZH("zh-CN", "zh-CN"),
    //en
    EN("en-US", "en-US");

    private final String code;
    private final String info;

    SysLanguage(String code, String info) {
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
