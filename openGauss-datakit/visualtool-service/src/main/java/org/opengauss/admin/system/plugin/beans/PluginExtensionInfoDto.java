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
 */

package org.opengauss.admin.system.plugin.beans;

import java.util.Date;
import lombok.Data;
import org.opengauss.admin.common.enums.PluginLicenseType;

/**
 * PluginExtensionInfo
 *
 * @author YanHuan
 * @since 2023-04-26
 */
@Data
public class PluginExtensionInfoDto {
    /**
     * pluginId
     */
    private String pluginId;

    /**
     * pluginLicenseType
     */
    private PluginLicenseType pluginLicenseType;

    /**
     * pluginName
     */
    private String pluginName;

    /**
     * pluginIntroduction
     */
    private String pluginIntroduction;

    /**
     * pluginHome
     */
    private String pluginHome;

    /**
     * pluginActivationTime
     */
    private Date pluginActivationTime;

    /**
     * PluginExpirationTime
     */
    private Date pluginExpirationTime;

    /**
     * pluginDevelopmentCompany
     */
    private String pluginDevelopmentCompany;

    /**
     * phoneNumber
     */
    private String phoneNumber;

    /**
     * email
     */
    private String email;

    /**
     * companyAddress
     */
    private String companyAddress;

    /**
     * userGuide
     */
    private String userGuide;

    /**
     * demoAddress
     */
    private String demoAddress;

    /**
     * authAddress
     */
    private String authAddress;

    /**
     * customize
     */
    private String customize;
}
