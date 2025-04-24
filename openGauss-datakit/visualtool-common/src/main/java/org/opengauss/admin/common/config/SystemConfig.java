/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
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
 * SystemConfig.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/config/SystemConfig.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

/**
 * system config
 *
 * @author xielibo
 */
@Component
@ConfigurationProperties(prefix = "system")
public class SystemConfig {

    /**
     * files storage path
     */
    private static String defaultStoragePath;


    public static String getStoragePath() {
        return defaultStoragePath;
    }

    public void setDefaultStoragePath(String storagePath) {
        SystemConfig.defaultStoragePath = storagePath;
    }

    /**
     * plugin path
     */
    public static String getPluginPath() {
        return Paths.get(getStoragePath(), "../visualtool-plugin").normalize().toString();
    }

    /**
     * import path
     */
    public static String getImportPath() {
        return getStoragePath() + "/import";
    }

    /**
     * avatar files path
     */
    public static String getAvatarPath() {
        return getStoragePath() + "/avatar";
    }

    /**
     * download path
     */
    public static String getDownloadPath() {
        return getStoragePath() + "/download/";
    }

    /**
     * upload path
     */
    public static String getUploadPath() {
        return getStoragePath() + "/upload/";
    }

    /**
     * icon files path
     */
    public static String getIconsPath() {
        return getStoragePath() + "/icons";
    }
}
