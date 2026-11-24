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

package org.opengauss.admin.plugin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.plugin.domain.MigrationToolPortalDownloadInfo;
import org.opengauss.admin.plugin.enums.PortalType;
import org.opengauss.admin.plugin.enums.PortalVersion;

import java.util.List;

/**
 * @author duanguoqiang
 * @date 2024/1/31
 * @description MigrationToolPortalDownloadInfoService
 */
public interface MigrationToolPortalDownloadInfoService extends IService<MigrationToolPortalDownloadInfo> {
    /**
     * Get portal download info list
     *
     * @param hostId     hostId
     * @param portalType portalType
     * @return portal download info list
     */
    List<MigrationToolPortalDownloadInfo> getPortalDownloadInfoList(String hostId, PortalType portalType);

    /**
     * Get portal support version
     *
     * @param hostId     hostId
     * @param portalType portalType
     * @return portal support version
     */
    List<PortalVersion> getPortalSupportVersion(String hostId, PortalType portalType);

    /**
     * Get portal download info
     *
     * @param opsHost opsHost
     * @param portalType portalType
     * @param portalVersion portalVersion
     * @return portal download info
     */
    MigrationToolPortalDownloadInfo getPortalDownloadInfo(
            OpsHostEntity opsHost, PortalType portalType, PortalVersion portalVersion);
}
