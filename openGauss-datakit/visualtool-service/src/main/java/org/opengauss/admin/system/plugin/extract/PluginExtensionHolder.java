/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.system.plugin.extract;

import static org.opengauss.admin.common.constant.PluginExtensionConstants.OPS_PLUGIN_ID;
import static org.opengauss.admin.common.constant.PluginExtensionConstants.OPS_EXTENSION_BUS;
import static org.opengauss.admin.common.constant.PluginExtensionConstants.DATA_MIGRATION_PLUGIN_ID;
import static org.opengauss.admin.common.constant.PluginExtensionConstants.DATA_MIGRATION_EXTENSION_BUS;
import static org.opengauss.admin.common.constant.PluginExtensionConstants.ALERT_MONITOR_PLUGIN_ID;
import static org.opengauss.admin.common.constant.PluginExtensionConstants.ALERT_MONITOR_EXTENSION_BUS;
import static org.opengauss.admin.common.constant.PluginExtensionConstants.WEBDS_PLUGIN_ID;
import static org.opengauss.admin.common.constant.PluginExtensionConstants.WEBDS_EXTENSION_BUS;
import static org.opengauss.admin.common.constant.PluginExtensionConstants.COMPATIBILITY_ASSESSMENT_PLUGIN_ID;
import static org.opengauss.admin.common.constant.PluginExtensionConstants.COMPATIBILITY_ASSESSMENT_EXTENSION_BUS;
import static org.opengauss.admin.common.constant.PluginExtensionConstants.CONTAINER_MANAGEMENT_PLUGIN_ID;
import static org.opengauss.admin.common.constant.PluginExtensionConstants.CONTAINER_MANAGEMENT_EXTENSION_BUS;

import java.util.Map;

/**
 * PluginExtensionHolder
 *
 * @author wangchao
 * @date 2025-12-11
 * @since 7.0.0-RC3
 */
public class PluginExtensionHolder {
    /**
     * plugin id and extension bus relation mapping
     */
    private static final Map<String, String> PLUGIN_EXTENSION_BUS_MAPPING = Map.of(OPS_PLUGIN_ID, OPS_EXTENSION_BUS,
            DATA_MIGRATION_PLUGIN_ID, DATA_MIGRATION_EXTENSION_BUS,
            ALERT_MONITOR_PLUGIN_ID, ALERT_MONITOR_EXTENSION_BUS,
            WEBDS_PLUGIN_ID, WEBDS_EXTENSION_BUS,
            COMPATIBILITY_ASSESSMENT_PLUGIN_ID, COMPATIBILITY_ASSESSMENT_EXTENSION_BUS,
            CONTAINER_MANAGEMENT_PLUGIN_ID, CONTAINER_MANAGEMENT_EXTENSION_BUS);

    /**
     * get plugin extension bus by plugin id
     *
     * @param pluginId pluginId
     * @return extension bus
     */
    public static String getPluginExtensionBus(String pluginId) {
        return PLUGIN_EXTENSION_BUS_MAPPING.getOrDefault(pluginId, pluginId);
    }
}
