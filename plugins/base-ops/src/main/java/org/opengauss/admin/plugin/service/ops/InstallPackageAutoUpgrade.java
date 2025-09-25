/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
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
 * InstallPackageAutoUpgrade.java
 *
 * IDENTIFICATION
 * org.opengauss.admin.plugin.service.ops.InstallPackageAutoUpgrade.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.ops;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import org.opengauss.admin.plugin.domain.entity.ops.OpsPackageManagerEntity;
import org.opengauss.admin.plugin.domain.entity.ops.OpsPackagePathDictEntity;
import org.opengauss.admin.plugin.domain.model.ops.OpsPackagePathDictVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.annotation.Resource;

/**
 * openGauss InstallPackageAutoUpgrade
 *
 * @author: wangchao
 * @Date: 2025/4/22 16:27
 * @Description: PackageAutoUpgrade
 * @since 7.0.0-RC1
 **/
@Slf4j
@Component
public class InstallPackageAutoUpgrade implements ApplicationRunner {
    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake(1, 1);
    private static final String TAR_GZ = ".tar.gz";
    private static final String TAR_BZ_2 = ".tar.bz2";

    @Resource
    private IOpsPackageManagerV2Service opsPackageManagerV2Service;
    @Resource
    private IOpsPackagePathDictService opsPackagePathDictService;
    @Value("${install.package.upgrade.history-version}")
    private String historyVersion;
    @Value("${install.package.upgrade.old-history-version}")
    private String oldHistoryVersion;
    @Value("${installPackage.urlPrefix}")
    private String pkgUrlPrefix;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("InstallPackageAutoUpgrade run");
        List<OpsPackagePathDictVO> list = opsPackagePathDictService.listPackagePathDict();
        List<OpsPackagePathDictVO> versionDictList = list.stream()
            .filter(dict -> !StrUtil.equalsIgnoreCase(dict.getPkgTmpUseVersion(), oldHistoryVersion))
            .collect(Collectors.toList());
        upgradePackageDict(versionDictList);
        upgradePackageManager(versionDictList);
        log.info("InstallPackageAutoUpgrade finished");
    }

    private void upgradePackageDict(List<OpsPackagePathDictVO> versionDictList) {
        OpsPackagePathDictVO dictVO = versionDictList.get(0);
        if (Objects.equals(dictVO.getPkgTmpUseVersion(), historyVersion)) {
            log.info("InstallPackageAutoUpgrade version is already latest");
            return;
        }
        List<String> dictIds = versionDictList.stream().map(OpsPackagePathDictVO::getId).collect(Collectors.toList());
        opsPackagePathDictService.update(Wrappers.<OpsPackagePathDictEntity>lambdaUpdate()
            .set(OpsPackagePathDictEntity::getPkgTmpUseVersion, historyVersion)
            .in(OpsPackagePathDictEntity::getId, dictIds));
        log.info("InstallPackageAutoUpgrade version dict is upgrade to: {}", historyVersion);
    }

    private void upgradePackageManager(List<OpsPackagePathDictVO> versionDictList) {
        List<OpsPackageManagerEntity> packageManagerList = opsPackageManagerV2Service.list();
        Set<String> packageNumSet = packageManagerList.stream()
            .map(OpsPackageManagerEntity::getPackageVersionNum)
            .collect(Collectors.toSet());
        String[] historyVersions = historyVersion.split(";");
        for (String version : historyVersions) {
            if (!packageNumSet.contains(version)) {
                upgradePackageManager(versionDictList, version);
                log.info("InstallPackageAutoUpgrade package manager version: {}", version);
            }
        }
    }

    private void upgradePackageManager(List<OpsPackagePathDictVO> versionDictList, String version) {
        versionDictList.forEach(dict -> {
            OpsPackageManagerEntity entity = new OpsPackageManagerEntity();
            entity.setPackageId(SNOWFLAKE.nextIdStr());
            entity.setOs(dict.getOs());
            entity.setOsVersion(dict.getOsVersion());
            entity.setCpuArch(dict.getCpuArch());
            entity.setName(buildPackageManagerName(dict.getPackageNameTmp(), version));
            entity.setPackageVersion(dict.getVersion());
            entity.setPackageVersionNum(version);
            entity.setPackageVersionNum(version);
            entity.setType("openGauss");
            entity.setPackageUrl(dict.buildFullPackageUrl(pkgUrlPrefix, version, false));
            entity.setCreateBy("admin");
            entity.setUpdateBy("admin");
            entity.setCreateTime(new Date());
            entity.setUpdateTime(new Date());
            opsPackageManagerV2Service.save(entity);
        });
    }

    private String buildPackageManagerName(String packageNameTmp, String version) {
        if (packageNameTmp.contains(TAR_GZ)) {
            return packageNameTmp.replace("%s", version).replace(TAR_GZ, "");
        } else if (packageNameTmp.contains(TAR_BZ_2)) {
            return packageNameTmp.replace("%s", version).replace(TAR_BZ_2, "");
        } else {
            return packageNameTmp.replace("%s", version);
        }
    }
}
