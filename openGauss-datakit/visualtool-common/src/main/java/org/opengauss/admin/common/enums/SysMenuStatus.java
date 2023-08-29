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
 * SysMenuStatus.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/enums/SysMenuStatus.java
 *
 * -------------------------------------------------------------------------
 */
package org.opengauss.admin.common.enums;

/**
 * SysMenu Status
 *
 * @className: SysMenuStatus
 * @author: xielibo
 * @date: 2023-05-24 20:40
 **/
public enum SysMenuStatus {
    ENABLE("0"),
    DISABLE("1");

    /**
     * statusCode
     */
    private final String code;

    /**
     * SysMenuStatus Constructor
     *
     * @param code statusCode
     */
    SysMenuStatus(String code) {
        this.code = code;
    }

    /**
     * get status code
     *
     * @return statusCode
     */
    public String getCode() {
        return code;
    }
}
