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
 * SysMenuPluginComponent.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/enums/SysMenuPluginComponent.java
 *
 * -------------------------------------------------------------------------
 */


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
