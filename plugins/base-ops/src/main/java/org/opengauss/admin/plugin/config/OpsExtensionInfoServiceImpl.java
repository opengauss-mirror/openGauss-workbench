/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.plugin.config;

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
