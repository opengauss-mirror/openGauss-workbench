/*
 * Copyright (c) 2020 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */
package org.opengauss.admin.plugin.controller.modeling;

import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.plugin.base.BaseController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author LZW
 * @date 2023/4/19 22:38
 */
@RestController
@RequestMapping("/modeling/databaseDesign")
public class ModelingDatabaseDesignController extends BaseController {
    /**
     * count for home page
     */
    @GetMapping("/count")
    public AjaxResult count() {
        AjaxResult ajax = AjaxResult.success();
        ajax.put("count", 0);
        return ajax;
    }
}