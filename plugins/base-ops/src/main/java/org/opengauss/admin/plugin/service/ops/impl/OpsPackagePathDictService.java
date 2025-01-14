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
 * OpsPackagePathDictService.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/ops/impl/OpsPackagePathDictService.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.ops.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;

import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.dto.ops.PackageDto;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.common.utils.http.HttpUtils;
import org.opengauss.admin.plugin.domain.entity.ops.OpsPackagePathDictEntity;
import org.opengauss.admin.plugin.domain.model.ops.OpsPackagePathDictVO;
import org.opengauss.admin.plugin.domain.model.ops.OpsPackageVO;
import org.opengauss.admin.plugin.mapper.ops.OpsPackagePathDictMapper;
import org.opengauss.admin.plugin.service.ops.IOpsPackagePathDictService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author wangchao
 * @date 2024/06/15 09:26
 */
@Slf4j
@Service
public class OpsPackagePathDictService extends ServiceImpl<OpsPackagePathDictMapper, OpsPackagePathDictEntity> implements IOpsPackagePathDictService {
    @Override
    public List<OpsPackagePathDictVO> listPackagePathDict() {
        return list().stream().map(OpsPackagePathDictEntity::toVO).collect(Collectors.toList());
    }

    @Override
    public boolean checkOsExists(String os) {
        return list().stream().map(OpsPackagePathDictEntity::getOs).anyMatch(os::equalsIgnoreCase);
    }

    @Override
    public boolean checkCpuArchExists(String cpuArch) {
        return list().stream().map(OpsPackagePathDictEntity::getCpuArch).anyMatch(cpuArch::equalsIgnoreCase);
    }

    @Override
    public OpsPackagePathDictVO queryPackagePathDict(PackageDto packageDto) {
        Optional<OpsPackagePathDictEntity> dictEntity = list().stream()
                .filter(item -> StrUtil.equalsIgnoreCase(item.getOs(), packageDto.getOs()))
                .filter(item -> StrUtil.equalsIgnoreCase(item.getOsVersion(), packageDto.getOsVersion()))
                .filter(item -> StrUtil.equalsIgnoreCase(item.getCpuArch(), packageDto.getCpuArch()))
                .filter(item -> StrUtil.equalsIgnoreCase(item.getVersion(), packageDto.getOpenGaussVersion().name()))
                .filter(item -> canUsedPkgTmpOfVersionNum(item.getPkgTmpUseVersion(),
                        packageDto.getOpenGaussVersionNum()))
                .findFirst();
        return dictEntity.map(OpsPackagePathDictEntity::toVO).orElse(null);
    }

    private boolean canUsedPkgTmpOfVersionNum(String pkgTmpUseVersion, String versionNum) {
        return Arrays.stream(pkgTmpUseVersion.split(";"))
                .anyMatch(v -> StrUtil.equalsIgnoreCase(v, versionNum));
    }

    @Override
    public OpsPackageVO buildOpsPackage(String pkgUrlPrefix, String packageVersionNum,
                                        OpsPackagePathDictVO packageDict) {
        // 当前环境在线模式
        if (checkCurrentEnvironmentIsOnline()) {
            return buildOpsPackageOnline(pkgUrlPrefix, packageVersionNum, packageDict);
        } else {
            return buildOpsPackageOffline(pkgUrlPrefix, packageVersionNum, packageDict);
        }
    }

    private OpsPackageVO buildOpsPackageOffline(String pkgUrlPrefix, String packageVersionNum,
                                                OpsPackagePathDictVO packageDict) {
        // 当前环境离线模式： 构建已发布版本下载链接
        String downloadUrl = packageDict.buildFullPackageUrl(pkgUrlPrefix, packageVersionNum, false);
        return createOpsPackageVo(packageVersionNum, packageDict, downloadUrl);
    }

    private OpsPackageVO buildOpsPackageOnline(String pkgUrlPrefix, String pkgVersionNum,
                                               OpsPackagePathDictVO packageDict) {
        // 构建 当前版本号 packageVersionNum last 下载链接
        String downloadUrl = packageDict.buildFullPackageUrl(pkgUrlPrefix, pkgVersionNum, true);
        // 检查当前版本号 packageVersionNum 是否是 last 版本
        AjaxResult downloadUrlResult = HttpUtils.checkUrl(downloadUrl);
        if (downloadUrlResult.isOk()) {
            return createOpsPackageVo(pkgVersionNum, packageDict, downloadUrl);
        }
        // 当前版本号不是last版本
        downloadUrl = packageDict.buildFullPackageUrl(pkgUrlPrefix, pkgVersionNum, false);
        downloadUrlResult = HttpUtils.checkUrl(downloadUrl);
        if (downloadUrlResult.isOk()) {
            return createOpsPackageVo(pkgVersionNum, packageDict, downloadUrl);
        } else {
            throw new OpsException("package download url is not ok");
        }
    }

    private static OpsPackageVO createOpsPackageVo(String packageVersionNum, OpsPackagePathDictVO packageDict,
                                                   String downloadUrl) {
        return OpsPackageVO.builder().os(packageDict.getOs())
                .osVersion(packageDict.getOsVersion())
                .cpuArch(packageDict.getCpuArch())
                .packageVersion(packageDict.getVersion())
                .packageVersionNum(packageVersionNum).packageUrl(downloadUrl).build();
    }

    @Override
    public boolean checkCurrentEnvironmentIsOnline() {
        AjaxResult result = HttpUtils.checkUrl("https://opengauss.org/zh/");
        return result.isOk();
    }
}
