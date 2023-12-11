/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
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
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.collect.config;

import com.gitee.starblues.annotation.Extract;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.enums.PluginLicenseType;
import org.opengauss.admin.system.plugin.beans.PluginExtensionInfoDto;
import org.opengauss.admin.system.plugin.extract.PluginExtensionInfoExtract;
import org.springframework.stereotype.Service;

import java.util.Date;

import static org.opengauss.collect.config.PluginExtensionInfoConfig.PLUGIN_ID;

/**
 * ExtensionInfoServiceImpl
 *
 * @author liu
 * @since 2023-09-17
 */
@Slf4j
@Service
@Extract(bus = PLUGIN_ID)
public class ExtensionInfoServiceImpl implements PluginExtensionInfoExtract {
    @Override
    public PluginExtensionInfoDto getPluginExtensionInfo() {
        PluginExtensionInfoDto dto = new PluginExtensionInfoDto();
        dto.setPluginId(PLUGIN_ID);
        dto.setPluginName("插件DEMO");
        dto.setPluginHome(PLUGIN_ID);
        dto.setPluginDevelopmentCompany("openGauss社区");
        dto.setPhoneNumber("400-123-4567");
        dto.setEmail("");
        dto.setCompanyAddress("opengauss.org");
        dto.setAuthAddress("/license");
        dto.setPluginLicenseType(PluginLicenseType.FREE);
        dto.setPluginExpirationTime(new Date());
        return dto;
    }
}
