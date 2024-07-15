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

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.common.utils.http.HttpUtils;
import org.opengauss.admin.plugin.domain.entity.ops.OpsPackagePathDictEntity;
import org.opengauss.admin.plugin.domain.model.ops.OpsPackagePathDictVO;
import org.opengauss.admin.plugin.domain.model.ops.OpsPackageVO;
import org.opengauss.admin.plugin.enums.ops.OpenGaussVersionEnum;
import org.opengauss.admin.plugin.mapper.ops.OpsPackagePathDictMapper;
import org.opengauss.admin.plugin.service.ops.IOpsPackagePathDictService;
import org.springframework.stereotype.Service;

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
    public OpsPackagePathDictVO queryPackagePathDict(String os, String cpuArch, OpenGaussVersionEnum packageVersion) {
        Optional<OpsPackagePathDictEntity> dictEntity = list().stream().filter(item -> item.getOs().equalsIgnoreCase(os)
                && item.getCpuArch().equalsIgnoreCase(cpuArch)
                && item.getVersion().equals(packageVersion.name())).findFirst();

        if (!dictEntity.isPresent()) {
            return null;
        }
        return dictEntity.get().toVO();
    }

    @Override
    public OpsPackageVO buildAndCheckPackageUrlIsValid(String installPackageUrlPrefix, String packageVersionNum,
                                                       OpsPackagePathDictVO packageDict) {
        boolean isOnLine = checkCurrentEnvironmentIsOnline();
        String downloadUrl = "";
        // 当前环境在线模式
        if (isOnLine) {
            // 构建 当前版本号 packageVersionNum last 下载链接
            downloadUrl = packageDict.buildFullPackageUrl(installPackageUrlPrefix, packageVersionNum, true);
            // 检查当前版本号 packageVersionNum 是否是 last 版本
            AjaxResult downloadUrlResult = HttpUtils.checkUrl(downloadUrl);
            if (downloadUrlResult.isOk()) {
                return createOpsPackageVo(packageVersionNum, packageDict, downloadUrl);
            }
            // 当前版本号不是last版本
            downloadUrl = packageDict.buildFullPackageUrl(installPackageUrlPrefix, packageVersionNum, false);
            downloadUrlResult = HttpUtils.checkUrl(downloadUrl);
            if (downloadUrlResult.isOk()) {
                return createOpsPackageVo(packageVersionNum, packageDict, downloadUrl);
            } else {
                throw new OpsException("package download url is not ok");
            }
        } else {
            // 当前环境离线模式： 构建已发布版本下载链接
            downloadUrl = packageDict.buildFullPackageUrl(installPackageUrlPrefix, packageVersionNum, false);
            return createOpsPackageVo(packageVersionNum, packageDict, downloadUrl);
        }
    }

    private static OpsPackageVO createOpsPackageVo(String packageVersionNum, OpsPackagePathDictVO packageDict, String downloadUrl) {
        return OpsPackageVO.builder().os(packageDict.getOs()).cpuArch(packageDict.getCpuArch())
                .packageVersion(packageDict.getVersion())
                .packageVersionNum(packageVersionNum).packageUrl(downloadUrl).build();
    }

    @Override
    public boolean checkCurrentEnvironmentIsOnline() {
        AjaxResult result = HttpUtils.checkUrl("https://opengauss.org/zh/");
        return result.isOk();
    }
}
