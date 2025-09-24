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
 * SysSettingController.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-api/src/main/java/org/opengauss/admin/web/controller/SysSettingController.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.web.controller;

import org.opengauss.admin.common.annotation.Log;
import org.opengauss.admin.common.core.controller.BaseController;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.domain.entity.SysSettingEntity;
import org.opengauss.admin.common.enums.BusinessType;
import org.opengauss.admin.system.service.ISysSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * System Setting Controller
 *
 * @author wangyl
 */
@RestController
@RequestMapping("/system/setting")
public class SysSettingController extends BaseController {
    @Autowired
    private ISysSettingService sysSettingService;

    /**
     * update system setting
     */
    @Log(title = "setting", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult update(@RequestBody @Validated SysSettingEntity setting) {
        setting.setUserId(getUserId());
        boolean res = sysSettingService.updateSetting(setting);
        return res ? AjaxResult.success() : AjaxResult.error();
    }

    /**
     * list system setting
     */
    @GetMapping
    public AjaxResult list() {
        SysSettingEntity setting = sysSettingService.getSetting(getUserId());
        return AjaxResult.success(setting);
    }

    @GetMapping("/checkUploadPath")
    public AjaxResult checkSysUploadPath(@RequestParam String path) {
        boolean result = sysSettingService.hasUploadPath(path, getUserId());
        // return false is not ok
        return AjaxResult.success("ok", !result);
    }

}
