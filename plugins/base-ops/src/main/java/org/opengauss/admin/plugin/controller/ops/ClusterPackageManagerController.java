/*
 * Copyright (c) 2024 Huawei Technologies Co.,Ltd.
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
 * ClusterPackageManagerController.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/controller/ops/ClusterPackageManagerController.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.controller.ops;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.dto.ops.OpsPackageDownloadDTO;
import org.opengauss.admin.common.core.dto.ops.OpsPackageUploadDTO;
import org.opengauss.admin.common.core.dto.ops.PackageDto;
import org.opengauss.admin.common.core.page.TableDataInfo;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.base.BaseController;
import org.opengauss.admin.plugin.constant.OpsConstants;
import org.opengauss.admin.plugin.domain.entity.ops.OpsPackageManagerEntity;
import org.opengauss.admin.plugin.domain.model.ops.OpsPackagePathDictVO;
import org.opengauss.admin.plugin.domain.model.ops.OpsPackageVO;
import org.opengauss.admin.plugin.service.ops.IOpsPackageManagerV2Service;
import org.opengauss.admin.plugin.service.ops.IOpsPackagePathDictService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Objects;

/**
 * ClusterPackageManagerController
 *
 * @author wangchao
 * @date 2024/06/15 09:26
 */
@Slf4j
@RestController
@RequestMapping("/installPackageManager/v2")
public class ClusterPackageManagerController extends BaseController {
    @Resource
    private IOpsPackageManagerV2Service opsPackageManagerV2Service;
    @Resource
    private IOpsPackagePathDictService opsPackagePathDictService;

    @Value("${installPackage.urlPrefix}")
    private String pkgUrlPrefix;

    @GetMapping("/check/online")
    public AjaxResult checkOnline() {
        return AjaxResult.success(opsPackagePathDictService.checkCurrentEnvironmentIsOnline());
    }

    @PostMapping("/page/package")
    public TableDataInfo pagePackage(@RequestBody PackageDto dto) {
        LambdaQueryWrapper<OpsPackageManagerEntity> queryWrapper = getOpsPackageQueryWrapper(dto);
        IPage<OpsPackageManagerEntity> page = opsPackageManagerV2Service.page(startPage(), queryWrapper);
        return getDataTable(page);
    }

    @PostMapping("/list/package")
    public List<OpsPackageManagerEntity> listPackage(@Valid @RequestBody PackageDto dto) {
        return opsPackageManagerV2Service.list(getOpsPackageQueryWrapper(dto));
    }

    private LambdaQueryWrapper<OpsPackageManagerEntity> getOpsPackageQueryWrapper(PackageDto dto) {
        LambdaQueryWrapper<OpsPackageManagerEntity> lambdaQuery = Wrappers.lambdaQuery(OpsPackageManagerEntity.class);
        LambdaQueryWrapper<OpsPackageManagerEntity> queryWrapper = lambdaQuery
                .like(StrUtil.isNotEmpty(dto.getName()), OpsPackageManagerEntity::getName, dto.getName())
                .eq(StrUtil.isNotEmpty(dto.getOs()), OpsPackageManagerEntity::getOs, dto.getOs())
                .eq(StrUtil.isNotEmpty(dto.getOsVersion()), OpsPackageManagerEntity::getOsVersion, dto.getOsVersion())
                .eq(StrUtil.isNotEmpty(dto.getCpuArch()), OpsPackageManagerEntity::getCpuArch, dto.getCpuArch())
                .eq(StrUtil.isNotEmpty(dto.getOpenGaussVersionNum()), OpsPackageManagerEntity::getPackageVersionNum,
                        dto.getOpenGaussVersionNum())
                .eq(Objects.nonNull(dto.getOpenGaussVersion()), OpsPackageManagerEntity::getPackageVersion,
                        dto.getOpenGaussVersion())
                .isNotNull(OpsPackageManagerEntity::getPackagePath)
                .orderByDesc(OpsPackageManagerEntity::getUpdateTime);
        return queryWrapper;
    }

    @GetMapping("/list/version/number")
    public AjaxResult listVersionNumber() {
        return AjaxResult.success(opsPackageManagerV2Service.listVersionNumber());
    }

    /**
     * check and return package version info
     *
     * @param packageDto packageDto
     * @return AjaxResult
     */
    @GetMapping("/check/version/number")
    public AjaxResult checkVersionNumber(@ModelAttribute PackageDto packageDto) {
        try {
            Assert.isTrue(StrUtil.isNotEmpty(packageDto.getOs()), "os 不能为空");
            Assert.isTrue(StrUtil.isNotEmpty(packageDto.getCpuArch()), "cpuArch 不能为空");
            Assert.isTrue(StrUtil.isNotEmpty(packageDto.getOpenGaussVersionNum()), "packageVersionNum 不能为空");
            Assert.isTrue(Objects.nonNull(packageDto.getOpenGaussVersion()), "packageVersion 不能为空");
            Assert.isTrue(opsPackagePathDictService.checkOsExists(packageDto.getOs()),
                    "not support os param value " + packageDto.simple());
            Assert.isTrue(opsPackagePathDictService.checkCpuArchExists(packageDto.getCpuArch()),
                    "not support cpuArch param value " + packageDto.simple());

            List<OpsPackageVO> list = opsPackageManagerV2Service.queryOpsPackageList(packageDto);
            if (CollectionUtils.isNotEmpty(list)) {
                return AjaxResult.success(list);
            } else {
                OpsPackagePathDictVO packageTmp = opsPackagePathDictService.queryPackagePathDict(packageDto);
                if (Objects.nonNull(packageTmp)) {
                    OpsPackageVO opsPackageVO = opsPackagePathDictService.buildOpsPackage(pkgUrlPrefix,
                            packageDto.getOpenGaussVersionNum(), packageTmp);
                    return AjaxResult.success(opsPackageVO);
                } else {
                    return AjaxResult.error("安装包字典不存当前版本 :" + packageDto.simple());
                }
            }
        } catch (IllegalArgumentException | OpsException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * Delete package
     *
     * @param packageIds packageIds
     * @return AjaxResult
     */
    @PostMapping("/delete/package")
    public AjaxResult del(@RequestBody List<String> packageIds) {
        opsPackageManagerV2Service.delPackage(packageIds);
        return AjaxResult.success();
    }

    /**
     * Check package list
     *
     * @param packageIds packageIds
     * @return result
     */
    @PostMapping("/check/package")
    public AjaxResult checkingPackageList(@RequestBody List<String> packageIds) {
        try {
            Assert.isTrue(CollectionUtils.isNotEmpty(packageIds), "packageIds 不能为空");
            opsPackageManagerV2Service.checkingPackageList(packageIds);
            return AjaxResult.success();
        } catch (IllegalArgumentException | OpsException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * check package
     *
     * @param packageId packageId
     * @return boolean
     */
    @PostMapping("/check/pkg")
    public AjaxResult checkPackage(@RequestParam(name = "packageId") String packageId) {
        try {
            Assert.isTrue(StrUtil.isNotEmpty(packageId), "packageId 不能为空");
            return AjaxResult.success(opsPackageManagerV2Service.checkingPackage(packageId));
        } catch (IllegalArgumentException | OpsException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    @PostMapping("/save/online")
    public AjaxResult online(@Valid @RequestBody OpsPackageDownloadDTO dto) {
        Assert.isTrue(opsPackagePathDictService.checkCurrentEnvironmentIsOnline(),
                "current environment is not online");
        Assert.isTrue(opsPackagePathDictService.checkOsExists(dto.getOs()),
                "not support os param value " + dto.getOs());
        Assert.isTrue(opsPackagePathDictService.checkCpuArchExists(dto.getCpuArch()),
                "not support cpuArch param value " + dto.getCpuArch());
        Assert.isTrue(!opsPackageManagerV2Service.hasName("", dto.getName()),
                "package name [" + dto.getName() + "] already exists");
        OpsPackageManagerEntity entity = new OpsPackageManagerEntity();
        entity.setOs(dto.getOs());
        entity.setOsVersion(dto.getOsVersion());
        entity.setCpuArch(dto.getCpuArch());
        entity.setPackageVersion(dto.getOpenGaussVersion().name());
        entity.setPackageVersionNum(dto.getOpenGaussVersionNum());
        entity.setPackageUrl(dto.getDownloadUrl());
        entity.setName(dto.getName());
        entity.setType("openGauss");
        entity.setRemark(OpsConstants.PACKAGE_REMARK);
        opsPackageManagerV2Service.saveOnlinePackage(entity, getUserId(), dto.getWsBusinessId());
        return AjaxResult.success();
    }

    @PostMapping("/update/online")
    public AjaxResult online(@RequestParam("packageId") String packageId, @RequestParam("wsBusinessId") String wsBusinessId) {
        Assert.isTrue(opsPackagePathDictService.checkCurrentEnvironmentIsOnline(), "current environment is not online");
        OpsPackageManagerEntity packageEntity = opsPackageManagerV2Service.getById(packageId);
        Assert.isTrue(Objects.nonNull(packageEntity), "package [" + packageId + "] not exists");
        opsPackageManagerV2Service.updateOnlinePackage(packageEntity, getUserId(), wsBusinessId);
        return AjaxResult.success();
    }


    @PostMapping("/update/upload")
    public AjaxResult upload(@RequestParam("packageId") String packageId, @RequestParam("uploadFile") MultipartFile uploadFile) {
        Assert.isTrue(StrUtil.isNotEmpty(packageId), "packageId can not be empty");
        Assert.isTrue(Objects.nonNull(uploadFile), "uploadFile param can not be empty");
        OpsPackageManagerEntity pkg = opsPackageManagerV2Service.getById(packageId);
        Assert.isTrue(Objects.nonNull(pkg), "package [" + packageId + "] not exists");
        pkg.setFile(uploadFile);
        opsPackageManagerV2Service.updateUploadPackage(pkg, getUserId());
        return AjaxResult.success();
    }

    @PostMapping("/save/upload")
    public AjaxResult upload(@ModelAttribute OpsPackageUploadDTO dto) {
        Assert.isTrue(StrUtil.isNotEmpty(dto.getOs()), "os param can not be empty");
        Assert.isTrue(StrUtil.isNotEmpty(dto.getOsVersion()), "osVersion param can not be empty");
        Assert.isTrue(StrUtil.isNotEmpty(dto.getCpuArch()), "cpuArch param can not be empty");
        Assert.isTrue(StrUtil.isNotEmpty(dto.getName()), "package name param can not be empty");
        Assert.isTrue(Objects.nonNull(dto.getPackageVersion()), "package version param can not be empty");
        Assert.isTrue(StrUtil.isNotEmpty(dto.getPackageVersionNum()), "package version num param can not be empty");
        Assert.isTrue(opsPackagePathDictService.checkOsExists(dto.getOs()),
                "not support os param value " + dto.getOs());
        Assert.isTrue(opsPackagePathDictService.checkCpuArchExists(dto.getCpuArch()),
                "not support cpuArch param value " + dto.getCpuArch());
        OpsPackageManagerEntity pkg = new OpsPackageManagerEntity();
        BeanUtils.copyProperties(dto, pkg);
        pkg.setType("openGauss");
        pkg.setPackageVersion(dto.getPackageVersion().name());
        pkg.setFile(dto.getUploadFile());
        pkg.setRemark(OpsConstants.PACKAGE_REMARK);
        opsPackageManagerV2Service.saveUploadPackage(pkg, getUserId());
        return AjaxResult.success();
    }
}
