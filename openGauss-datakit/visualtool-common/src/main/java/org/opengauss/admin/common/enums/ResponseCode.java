/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 * ResponseCode.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/enums/ResponseCode.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.common.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * @description: error codes
 * @param:
 * @return:
 * @author: xielibo
 * @date: 2020/11/2
 */
public enum ResponseCode {
    //success
    SUCCESS(200, "success"),

    //request params is bad
    BAD_REQUEST(400, "request params is bad"),

    //unauthorized
    UNAUTHORIZED(401, "unauthorized"),

    //authorization expired
    FORBIDDEN(403, "authorization expired"),

    //system error
    ERROR(500, "system error"),

    //platform
    INTEGRATION_PLUGIN_STOP_ERROR(50101, "plugin stop error"),
    INTEGRATION_PLUGIN_START_ERROR(50102, "plugin start error"),
    INTEGRATION_PLUGIN_UNINSTALL_ERROR(50103, "plugin uninstall error"),
    INTEGRATION_PLUGIN_INSTALL_ERROR(50104, "plugin install error"),
    ROLE_EXISTS_ERROR(50105, "role is exists"),
    ROLE_PERMISSIONS_EXISTS_ERROR(50106, "role permissions is exists"),
    USER_PHONE_EXISTS_ERROR(50107, "user phone is exists"),
    USER_EMAIL_EXISTS_ERROR(50108, "user email is exists"),
    USER_RESET_PASS_ORIGIN_PASS_ERROR(50109, "origin pass is error"),
    USER_PASS_SAME_ERROR(50110, "New password can't be same as old password!"),
    MENU_NAME_IS_EXISTS_ERROR(50111, "menu name is exists"),
    MENU_NOT_ADD_SELF_AS_SUBMENU_ERROR(50112, "A menu cannot add itself as a submenu"),
    MENU_HAS_SUBMENU_NOT_DELETE(50113, "This menu has a submenu, it is not allowed to delete"),
    MENU_ASSIGNED_NOT_DELETE(50114, "This menu has already been assigned and cannot be deleted"),
    USER_ACCOUNT_EXISTS_ERROR(50115, "user account is exists"),
    PLUGIN_MENU_HAS_OTHER_PLUGIN_SUBMENU_UNINSTALL_ERROR(50116, "This plugin menu has submenus for other plugins"),
    ROLE_NAME_IS_NOT_EMPTY_ERROR(50117, "Role name cannot be empty"),
    ROLE_NAME_MAX_LENGTH_ERROR(50118, "Role name length cannot exceed 25 characters"),
    ROLE_REMARK_MAX_LENGTH_ERROR(50119, "Role remark length cannot exceed 200 characters"),
    USER_NAME_MAX_LENGTH_ERROR(50120, "User account length cannot exceed 30 characters"),
    USER_NICKNAME_MAX_LENGTH_ERROR(50121, "User nickname length cannot exceed 30 characters"),
    USER_REMARK_MAX_LENGTH_ERROR(50122, "User remark length cannot exceed 200 characters"),
    USER_TELEPHONE_MAX_LENGTH_ERROR(50123, "User telephone length cannot exceed 11 characters"),

    WHITELIST_TITLE_MAX_LENGTH_ERROR(50124, "Whitelist title length cannot exceed 100 characters"),
    WHITELIST_IPS_MAX_LENGTH_ERROR(50125, "Whitelist IP list length cannot exceed 200 characters"),

    WHITELIST_TITLE_EXISTS_ERROR(50126, "Whitelist title already exists"),
    WHITELIST_HAS_DUPLICATE_IP_ERROR(50127, "Duplicate IP addresses exist in the whitelist"),
    WHITELIST_IPS_EXISTS_ERROR(50128, "Some IP addresses in current whitelist "
        + "already exists in the existing whitelist"),
    INTEGRATION_UNLOAD_PLUGIN_INFO_QUERY_ERROR(50129, "query unload plugins information error"),
    INTEGRATION_PLUGIN_URL_QUERY_ERROR(50130, "query plugin url error"),
    INTEGRATION_PLUGIN_VERSION_ERROR(50131,
            "The uploaded pluginJar package version should be consistent with the current datakit version: "),

    USER_PASSWORD_CANNOT_BE_EMPTY_ERROR(50132, "The password for new system user cannot be empty");


    private static Map<Integer, ResponseCode> codeMap = new HashMap<Integer, ResponseCode>();

    private Integer code;

    private String msg;

    private String value;

    /**
     *
     */
    private ResponseCode() {
    }

    /**
     * @param value
     */
    private ResponseCode(String value) {
        this.value = value;
    }

    /**
     * @param code
     * @param msg
     */
    private ResponseCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * @return
     */
    public Integer code() {
        return this.code;
    }

    /**
     * @return
     */
    public String msg() {
        return this.msg;
    }

    /**
     * @return
     */
    public String value() {
        return this.value;
    }

    /**
     * @param codeValue
     * @return
     */
    public static ResponseCode getInstance(Integer codeValue) {
        return getCodeMap().get(codeValue);
    }

    /**
     * @return
     */
    private static Map<Integer, ResponseCode> getCodeMap() {
        if (codeMap == null || codeMap.size() == 0) {
            ResponseCode[] codeList = ResponseCode.values();
            for (ResponseCode c : codeList) {
                codeMap.put(c.code(), c);
            }
        }
        return codeMap;
    }


}
