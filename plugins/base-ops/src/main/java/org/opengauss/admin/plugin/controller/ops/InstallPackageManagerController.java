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
 * InstallPackageManagerController.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/controller/ops/InstallPackageManagerController.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.controller.ops;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.domain.UploadInfo;
import org.opengauss.admin.common.core.page.TableDataInfo;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.base.BaseController;
import org.opengauss.admin.plugin.domain.entity.ops.OpsPackageManagerEntity;
import org.opengauss.admin.plugin.domain.model.ops.OpsPackageVO;
import org.opengauss.admin.plugin.enums.ops.OpenGaussVersionEnum;
import org.opengauss.admin.plugin.service.ops.IOpsPackageManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.util.List;
import java.util.Objects;

/**
 * @author lhf
 * @date 2022/12/11 16:04
 **/
@Slf4j
@RestController
@RequestMapping("/installPackageManager")
public class InstallPackageManagerController extends BaseController {

    @Autowired
    private IOpsPackageManagerService opsPackageManagerService;
    @Value("${installPackage.urlPrefix}")
    private String installPackageUrlPrefix;

    @PostMapping
    public AjaxResult save(@ModelAttribute OpsPackageVO pkg) {
        opsPackageManagerService.savePackage(pkg.toEntity(), getUserId());
        return AjaxResult.success();
    }

    @DeleteMapping("/{id}")
    public AjaxResult del(@PathVariable("id") String id) {
        opsPackageManagerService.delPackage(id);
        return AjaxResult.success();
    }

    @PutMapping
    public AjaxResult update(@ModelAttribute OpsPackageVO pkg) {
        opsPackageManagerService.updatePackage(pkg.toEntity(), getUserId());
        return AjaxResult.success();
    }

    @GetMapping("/detail/{id}")
    public AjaxResult detail(@PathVariable("id") String id) {
        OpsPackageManagerEntity byId = opsPackageManagerService.getById(id);
        return AjaxResult.success(byId);
    }

    @GetMapping("/page")
    public TableDataInfo page(@RequestParam(value = "name", required = false) String name, @RequestParam(value = "packageVersion", required = false) OpenGaussVersionEnum packageVersion) {
        LambdaQueryWrapper<OpsPackageManagerEntity> queryWrapper = Wrappers.lambdaQuery(OpsPackageManagerEntity.class)
                .eq(Objects.nonNull(packageVersion), OpsPackageManagerEntity::getPackageVersion, packageVersion)
                .orderByDesc(OpsPackageManagerEntity::getUpdateTime);

        if (StrUtil.isNotEmpty(name)) {
            queryWrapper.and(orWrapper -> orWrapper
                    .or().like(OpsPackageManagerEntity::getName, name)
                    .or().like(OpsPackageManagerEntity::getOs, name)
                    .or().like(OpsPackageManagerEntity::getCpuArch, name)
                    .or().like(OpsPackageManagerEntity::getPackageVersionNum, name));
        }

        IPage<OpsPackageManagerEntity> page = opsPackageManagerService.page(startPage(), queryWrapper);
        return getDataTable(page);
    }

    @GetMapping("/list")
    public AjaxResult list(@RequestParam(value = "os", required = false) String os,
                           @RequestParam(value = "osVersion", required = false) String osVersion,
                           @RequestParam(value = "cpuArch", required = false) String cpuArch,
                           @RequestParam(value = "packageVersion", required = false) OpenGaussVersionEnum packageVersion,
                           @RequestParam(value = "packageVersionNum", required = false) String packageVersionNum,
                           @RequestParam(value = "type", required = false) String type) {

        LambdaQueryWrapper<OpsPackageManagerEntity> queryWrapper = Wrappers.lambdaQuery(OpsPackageManagerEntity.class)
                .eq(StrUtil.isNotEmpty(os), OpsPackageManagerEntity::getOs, os)
                .eq(StrUtil.isNotEmpty(osVersion), OpsPackageManagerEntity::getOsVersion, osVersion)
                .eq(StrUtil.isNotEmpty(cpuArch), OpsPackageManagerEntity::getCpuArch, cpuArch)
                .eq(Objects.nonNull(packageVersion), OpsPackageManagerEntity::getPackageVersion, Objects.nonNull(packageVersion) ? packageVersion.name() : null)
                .eq(StrUtil.isNotEmpty(packageVersionNum), OpsPackageManagerEntity::getPackageVersionNum, packageVersionNum)
                .eq(StrUtil.isNotEmpty(type), OpsPackageManagerEntity::getType, type)
                .isNotNull(OpsPackageManagerEntity::getPackageUrl)
                .orderByDesc(OpsPackageManagerEntity::getPackageVersionNum);
        List<OpsPackageManagerEntity> list = opsPackageManagerService.list(queryWrapper);
        return AjaxResult.success(list);
    }

    @GetMapping("/getCpuArch")
    public AjaxResult getCpuArch(@RequestParam("installPackagePath") String installPackagePath, @RequestParam("version") OpenGaussVersionEnum version) {
        String arch = opsPackageManagerService.getCpuArchByPackagePath(installPackagePath, version);
        return AjaxResult.success(arch);
    }

    @GetMapping("/analysisPkg")
    public AjaxResult analysisPkg(@RequestParam String pkgName, @RequestParam String pkgType) {
        OpsPackageVO result = opsPackageManagerService.analysisPkg(pkgName, pkgType);
        return AjaxResult.success(result);
    }

    @Deprecated
    @PostMapping("/upload")
    public AjaxResult upload(@RequestParam MultipartFile file) {
        try {
            UploadInfo info = opsPackageManagerService.upload(file, getUserId());
            return AjaxResult.success(info.toVO());
        } catch (OpsException ex) {
            return AjaxResult.error(ex.getMessage());
        }
    }

    @DeleteMapping("/delPkgTar")
    public AjaxResult deletePkgTar(@RequestParam String path, @RequestParam(required = false) String id) {
        boolean result = opsPackageManagerService.deletePkgTar(path, id);
        return result ? AjaxResult.success() : AjaxResult.error();
    }

    @GetMapping("/sysUploadPath")
    public AjaxResult getSysUploadPath() {
        String result = opsPackageManagerService.getSysUploadPath(getUserId());
        return AjaxResult.success("ok", result);
    }

    @GetMapping("/hasName")
    public AjaxResult hasName(@RequestParam("name") String name) {
        return AjaxResult.success(opsPackageManagerService.hasName(name));
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(UploadInfo.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                UploadInfo info = JSON.parseObject(text, UploadInfo.class);
                setValue(info);
            }
        });
    }
}
