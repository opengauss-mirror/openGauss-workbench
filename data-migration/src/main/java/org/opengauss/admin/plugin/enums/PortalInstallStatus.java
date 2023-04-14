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
 * PortalInstallStatus.java
 *
 * IDENTIFICATION
 * data-migration/src/main/java/org/opengauss/admin/plugin/enums/PortalInstallStatus.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.plugin.enums;

/**
 * portal install status
 *
 * @author xielibo
 */
public enum PortalInstallStatus {


    // 0 ：not install  1：installing；2：Installed；10：install error

    NOT_INSTALL(0, "not install"),
    INSTALLING(1, "installing"),
    INSTALLED(2, "installed"),
    INSTALL_ERROR(10, "install error")
    ;

    private final Integer code;
    private final String command;

    PortalInstallStatus(Integer code, String command) {
        this.code = code;
        this.command = command;
    }

    public Integer getCode() {
        return code;
    }

    public String getCommand() {
        return command;
    }

}
