package org.opengauss.admin.web.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.opengauss.admin.common.annotation.Log;
import org.opengauss.admin.common.core.controller.BaseController;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.domain.entity.SysSettingEntity;
import org.opengauss.admin.common.enums.BusinessType;
import org.opengauss.admin.system.service.ISysSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


/**
 * System Setting Controller
 *
 * @author wangyl
 */
@RestController
@RequestMapping("/system/setting")
@Api(tags = "setting")
public class SysSettingController extends BaseController {
    @Autowired
    private ISysSettingService sysSettingService;

    /**
     * update system setting
     */
    @Log(title = "setting", businessType = BusinessType.UPDATE)
    @ApiOperation(value = "update", notes = "update")
    @PreAuthorize("@ss.hasPermi('system:setting:update')")
    @PutMapping
    public AjaxResult update(@RequestBody @Validated SysSettingEntity setting) {
        boolean res = sysSettingService.updateSetting(setting);
        return res ? AjaxResult.success() : AjaxResult.error();
    }

    /**
     * list system setting
     */
    @ApiOperation(value = "list", notes = "list")
    @GetMapping
    public AjaxResult list() {
        SysSettingEntity setting = sysSettingService.getSetting(getUserId());
        return AjaxResult.success(setting);
    }

    @GetMapping("/checkUploadPath")
    public AjaxResult checkSysUploadPath(@RequestParam String path) {
        boolean result = sysSettingService.hasUploadPath(path);
        // return false is not ok
        return AjaxResult.success("ok", !result);
    }

}
