/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.nctigba.alert.monitor.config;

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
 * @author wangchao
 * @date 2025-10-14
 * @since 7.0.0-RC2
 */
@Slf4j
@Service
@Extract(bus = PluginExtensionInfoConfig.PLUGIN_ID)
public class AlertMonitorExtensionInfoServiceImpl implements PluginExtensionInfoExtract {
    @Override
    public PluginExtensionInfoDto getPluginExtensionInfo() {
        PluginExtensionInfoDto dto = new PluginExtensionInfoDto();
        dto.setPluginId(PluginExtensionInfoConfig.PLUGIN_ID);
        dto.setPluginName("alert-monitor");
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
