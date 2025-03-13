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
 * ISysPluginRepositoryService.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/
 * service/ISysPluginRepositoryService.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.system.domain.SysPluginRepository;
import java.util.List;

/**
 * ISysPluginRepositoryService
 *
 * @author zhaochen
 * @since 2025-03-20
 * @version 7.0.0-RC1
 */
public interface ISysPluginRepositoryService extends IService<SysPluginRepository> {
    /**
     * Update plugins download status
     *
     * @param pluginIds pluginIds list
     * @param isDownload download status 0-unloaded 1-loaded
     */
    void updatePluginDownloadStatus(List<String> pluginIds, Integer isDownload);

    /**
     * Gets a list of pluginIds that are not loaded
     *
     * @return List of unloaded pluginIds
     */
    List<String> getUnloadPluginIdsList();

    /**
     * Gets a list of pluginIds that are not loaded by pluginRepository
     *
     * @param uploadedPluginIds uploadedPluginIds
     * @return List of unloaded pluginIds
     */
    List<String> getUnloadPluginListByRepository(List<String> uploadedPluginIds);

    /**
     * Gets plugin version
     *
     * @return plugin version
     */
    String getCurrentVersion();

    /**
     * Gets plugin download url by pluginId and version
     *
     * @param pluginId pluginId
     * @param pluginVersion pluginVersion
     * @return plugin download url
     */
    String getPluginUrlByIdAndVersion(String pluginId, String pluginVersion);

    /**
     * save online plugin package
     *
     * @param downloadUrl downloadUrl
     * @param fileName fileName
     * @param wsBusinessId wsBusinessId
     */
    void saveOnlinePackage(String downloadUrl, String fileName, String wsBusinessId);
}
