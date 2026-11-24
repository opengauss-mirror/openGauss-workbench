/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.admin.plugin.config;

import com.gitee.starblues.annotation.Extract;

import lombok.extern.slf4j.Slf4j;

import org.opengauss.admin.common.enums.PluginLicenseType;
import org.opengauss.admin.system.plugin.beans.PluginExtensionInfoDto;
import org.opengauss.admin.system.plugin.extract.PluginExtensionInfoExtract;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * OpsExtensionInfoServiceImpl
 *
 * @Author wangchao
 * @date 2025-10-14
 * @since 7.0.0-RC2
 */
@Slf4j
@Service
@Extract(bus = "base-ops-extension")
public class OpsExtensionInfoServiceImpl implements PluginExtensionInfoExtract {
    @Override
    public PluginExtensionInfoDto getPluginExtensionInfo() {
        PluginExtensionInfoDto dto = new PluginExtensionInfoDto();
        dto.setPluginId(PluginExtensionInfoConfig.PLUGIN_ID);
        dto.setPluginName("base-ops");
        dto.setPluginHome(PluginExtensionInfoConfig.PLUGIN_ID);
        dto.setPluginDevelopmentCompany("openGauss community");
        dto.setPhoneNumber("400-123-4567");
        dto.setEmail("");
        dto.setCompanyAddress("opengauss.org");
        dto.setAuthAddress("/license");
        dto.setPluginLicenseType(PluginLicenseType.TRIAL);
        dto.setPluginExpirationTime(new Date());
        return dto;
    }
}
