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
 * SysPluginRepositoryMapper.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/
 * mapper/SysPluginRepositoryMapper.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.opengauss.admin.system.domain.SysPluginRepository;

import java.util.List;

/**
 * SysPluginRepositoryMapper
 *
 * @author zhaochen
 * @since 2025-03-20
 */
@Mapper
public interface SysPluginRepositoryMapper extends BaseMapper<SysPluginRepository> {
    /**
     * Update plugins download status
     *
     * @param pluginIds pluginIds list
     * @param isDownload download status 0-unloaded 1-loaded
     */
    void updatePluginDownloadStatus(
        @Param("pluginIds") List<String> pluginIds,
        @Param("isDownload") Integer isDownload);

    /**
     * Gets a list of pluginIds that are not loaded
     *
     * @param uploadedPluginIds uploadedPluginIds
     * @return List of unloaded pluginIds
     */
    List<String> getUnloadPluginListByRepository(
        @Param("pluginIds") List<String> uploadedPluginIds);

    /**
     * Gets plugin download url by pluginId and version
     *
     * @param pluginId pluginId
     * @param pluginVersion pluginVersion
     * @return plugin download url
     */
    String getPluginUrlByIdAndVersion(
            @Param("pluginId") String pluginId,
            @Param("pluginVersion") String pluginVersion);
}