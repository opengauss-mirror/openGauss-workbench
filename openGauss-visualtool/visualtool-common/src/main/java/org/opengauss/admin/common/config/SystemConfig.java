package org.opengauss.admin.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

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
        return getStoragePath() + "/upload";
    }

    /**
     * icon files path
     */
    public static String getIconsPath() {
        return getStoragePath() + "/icons";
    }
}
