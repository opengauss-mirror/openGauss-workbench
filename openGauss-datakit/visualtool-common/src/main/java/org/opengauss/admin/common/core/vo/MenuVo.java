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
 * MenuVo.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/core/vo/MenuVo.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.common.core.vo;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

/**
 * @className: MenuVo
 * @description: Menu value object
 * @author: xielibo
 * @date: 2022-09-24 6:02 PM
 **/
@Builder
@Data
public class MenuVo {

    /**
     * menu ID
     */
    private Integer menuId;

    /**
     * menu Name
     */
    private String menuName;

    /**
     * parent ID
     */
    private Integer parentId;

    @Tolerate
    public MenuVo(){}

}
