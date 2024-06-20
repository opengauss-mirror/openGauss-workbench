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
import org.opengauss.admin.common.core.domain.UploadInfo;
import org.opengauss.admin.common.core.dto.ops.OpsPackageDownloadDTO;
import org.opengauss.admin.common.core.dto.ops.PackageDto;
import org.opengauss.admin.common.core.page.TableDataInfo;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.base.BaseController;
import org.opengauss.admin.plugin.domain.entity.ops.OpsPackageManagerEntity;
import org.opengauss.admin.plugin.domain.model.ops.OpsPackagePathDictVO;
import org.opengauss.admin.plugin.domain.model.ops.OpsPackageVO;
import org.opengauss.admin.plugin.enums.ops.OpenGaussVersionEnum;
import org.opengauss.admin.plugin.service.ops.IOpsPackageManagerService;
import org.opengauss.admin.plugin.service.ops.IOpsPackageManagerV2Service;
import org.opengauss.admin.plugin.service.ops.IOpsPackagePathDictService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.File;
import java.util.List;
import java.util.Objects;

/**
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
    private IOpsPackageManagerService opsPackageManagerService;
    @Resource
    private IOpsPackagePathDictService opsPackagePathDictService;

    @Value("${installPackage.urlPrefix}")
    private String installPackageUrlPrefix;

    @GetMapping("/check/online")
    public AjaxResult checkOnline() {
        return AjaxResult.success(opsPackagePathDictService.checkCurrentEnvironmentIsOnline());
    }

    @GetMapping("/page/package")
    public TableDataInfo pagePackage(@RequestBody PackageDto dto) {
        LambdaQueryWrapper<OpsPackageManagerEntity> queryWrapper = Wrappers.lambdaQuery(OpsPackageManagerEntity.class)
                .eq(StrUtil.isNotEmpty(dto.getName()), OpsPackageManagerEntity::getName, dto.getName())
                .eq(StrUtil.isNotEmpty(dto.getOs()), OpsPackageManagerEntity::getOs, dto.getOs())
                .eq(StrUtil.isNotEmpty(dto.getCpuArch()), OpsPackageManagerEntity::getCpuArch, dto.getCpuArch())
                .eq(StrUtil.isNotEmpty(dto.getOpenGaussVersionNum()), OpsPackageManagerEntity::getPackageVersionNum, dto.getOpenGaussVersionNum())
                .eq(Objects.nonNull(dto.getOpenGaussVersion()), OpsPackageManagerEntity::getPackageVersion, dto.getOpenGaussVersion().name())
                .isNotNull(OpsPackageManagerEntity::getPackagePath)
                .orderByDesc(OpsPackageManagerEntity::getUpdateTime);
        IPage<OpsPackageManagerEntity> page = opsPackageManagerV2Service.page(startPage(), queryWrapper);
        return getDataTable(page);
    }

    @GetMapping("/list/version/number")
    public AjaxResult listVersionNumber() {
        return AjaxResult.success(opsPackageManagerV2Service.listVersionNumber());
    }

    @GetMapping("/check/version/number")
    public AjaxResult checkVersionNumber(@RequestParam(value = "os") String os,
                                         @RequestParam(value = "cpuArch") String cpuArch,
                                         @RequestParam(value = "packageVersion") OpenGaussVersionEnum packageVersion,
                                         @RequestParam(value = "packageVersionNum") String packageVersionNum) {
        try {
            Assert.isTrue(StrUtil.isNotEmpty(os), "os 不能为空");
            Assert.isTrue(StrUtil.isNotEmpty(cpuArch), "cpuArch 不能为空");
            Assert.isTrue(StrUtil.isNotEmpty(packageVersionNum), "packageVersionNum 不能为空");
            Assert.isTrue(Objects.nonNull(packageVersion), "packageVersion 不能为空");
            Assert.isTrue(opsPackagePathDictService.checkOsExists(os), "not support os param value " + os);
            Assert.isTrue(opsPackagePathDictService.checkCpuArchExists(cpuArch), "not support cpuArch param value " + cpuArch);

            List<OpsPackageVO> list = opsPackageManagerV2Service.queryOpsPackageList(os, cpuArch, packageVersion, packageVersionNum);
            if (CollectionUtils.isNotEmpty(list)) {
                return AjaxResult.success(list);
            } else {
                OpsPackagePathDictVO packageDict = opsPackagePathDictService.queryPackagePathDict(os, cpuArch, packageVersion);
                Assert.isTrue(Objects.nonNull(packageDict), "安装包字典不存当前版本 :" + os + " " + cpuArch + " " + packageVersion);

                OpsPackageVO opsPackageVO = opsPackagePathDictService.buildAndCheckPackageUrlIsValid(installPackageUrlPrefix, packageVersionNum, packageDict);
                return AjaxResult.success(opsPackageVO);
            }
        } catch (IllegalArgumentException | OpsException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    @GetMapping("/check/package")
    public AjaxResult checkPackage(@RequestBody List<String> packageIds) {
        try {
            Assert.isTrue(CollectionUtils.isNotEmpty(packageIds), "packageIds 不能为空");
            opsPackageManagerV2Service.checkingPackageList(packageIds);
            return AjaxResult.success();
        } catch (IllegalArgumentException | OpsException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    @PostMapping("/save/online")
    public AjaxResult saveOnline(@RequestBody OpsPackageDownloadDTO dto) {
        Assert.isTrue(opsPackagePathDictService.checkCurrentEnvironmentIsOnline(), "current environment is not online");
        Assert.isTrue(opsPackagePathDictService.checkOsExists(dto.getOs()), "not support os param value " + dto.getOs());
        Assert.isTrue(opsPackagePathDictService.checkCpuArchExists(dto.getCpuArch()), "not support cpuArch param value " + dto.getCpuArch());
        Assert.isTrue(!opsPackageManagerV2Service.hasName(dto.getPackageId(), dto.getName()), "package name [" + dto.getName() + "] already exists");
        OpsPackageManagerEntity entity = new OpsPackageManagerEntity();
        entity.setOs(dto.getOs());
        entity.setCpuArch(dto.getCpuArch());
        entity.setPackageVersion(dto.getOpenGaussVersion().name());
        entity.setPackageVersionNum(dto.getOpenGaussVersionNum());
        entity.setPackageUrl(dto.getDownloadUrl());
        entity.setName(dto.getName());
        entity.setType("openGauss");
        UploadInfo packagePath = new UploadInfo();

        String sysUploadPath = opsPackageManagerService.getSysUploadPath(getUserId());
        packagePath.setRealPath(sysUploadPath
                + dto.getOpenGaussVersionNum() + File.separatorChar
                + dto.getOs() + File.separatorChar
                + dto.getCpuArch() + File.separatorChar
                + dto.getName());
        packagePath.setName(parseDownloadFileName(dto.getDownloadUrl()));
        entity.setPackagePath(packagePath);
        opsPackageManagerV2Service.savePackageOnline(entity, getUserId(), dto.getWsBusinessId());
        return AjaxResult.success();
    }

    private String parseDownloadFileName(String packageUrl) {
        return packageUrl.substring(packageUrl.lastIndexOf("/") + 1);
    }

    @PostMapping("/save/upload")
    public AjaxResult saveUpload(@ModelAttribute OpsPackageVO pkg) {
        Assert.isTrue(StrUtil.isNotEmpty(pkg.getOs()), "os param can not be empty");
        Assert.isTrue(StrUtil.isNotEmpty(pkg.getCpuArch()), "cpuArch param can not be empty");
        Assert.isTrue(StrUtil.isNotEmpty(pkg.getName()), "package name param can not be empty");
        Assert.isTrue(Objects.nonNull(pkg.getPackageVersion()), "package version param can not be empty");
        Assert.isTrue(StrUtil.isNotEmpty(pkg.getPackageVersionNum()), "package version num param can not be empty");
        Assert.isTrue(opsPackagePathDictService.checkOsExists(pkg.getOs()), "not support os param value " + pkg.getOs());
        Assert.isTrue(opsPackagePathDictService.checkCpuArchExists(pkg.getCpuArch()), "not support cpuArch param value " + pkg.getCpuArch());
        Assert.isTrue(!opsPackageManagerV2Service.hasName(pkg.getPackageId(), pkg.getName()), "package name [" + pkg.getName() + "] already exists");
        pkg.setType("openGauss");
        opsPackageManagerService.savePackage(pkg.toEntity(), getUserId());
        return AjaxResult.success();
    }
}
