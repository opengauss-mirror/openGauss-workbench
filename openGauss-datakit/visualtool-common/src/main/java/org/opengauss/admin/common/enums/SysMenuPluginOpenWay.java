/*
 * Copyright (c) 2022 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */


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
