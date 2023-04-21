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
 * MetaVo.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/core/vo/MetaVo.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.common.core.vo;

import lombok.Data;

/**
 * Menu Router Info
 *
 * @author xielibo
 */
@Data
public class MetaVo {
    /**
     * title
     */
    private String title;

    /**
     * icon
     */
    private String icon;

    private Integer order;

    private boolean requiresAuth;

    private boolean hideChildrenInMenu;

    /**
     * hideInMenu
     */
    private boolean hideInMenu;


    public MetaVo() {
    }

    public MetaVo(String title, String icon) {
        this.title = title;
        this.icon = icon;
    }

    public MetaVo(String title, String icon, boolean noCache) {
        this.title = title;
        this.icon = icon;
        this.requiresAuth = false;
    }

    public MetaVo(String title, String icon, boolean noCache,Integer order,boolean hideChildrenInMenu,boolean hideInMenu) {
        this.title = title;
        this.icon = icon;
        this.order = order;
        this.requiresAuth = false;
        this.hideChildrenInMenu = hideChildrenInMenu;
        this.hideInMenu = hideInMenu;
    }
}
