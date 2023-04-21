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
 * RouterVo.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/core/vo/RouterVo.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.common.core.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Router Info
 *
 * @author xielibo
 */
@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RouterVo {
    /**
     * name
     */
    private String name;

    /**
     * route path
     */
    private String path;

    /**
     * redirect，when noRedirect is set the route is not clickable in breadcrumbs
     */
    private String redirect;

    /**
     * component
     */
    private String component;

    /**
     * when you declare more than one route under the children of a route, it will automatically become a nested mode - such as a component page
     */
    private Boolean alwaysShow;

    private Boolean isPluginMenu;

    private String menuTheme;

    private MetaVo meta;

    private Integer menuClassify;

    /**
     * childrens
     */
    private List<RouterVo> children;

    /**
     * route query
     */
    @TableField(exist = false)
    private Map<String, Object> query;
}
