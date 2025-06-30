package org.opengauss.admin.plugin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.plugin.domain.MigrationToolPortalDownloadInfo;
import org.opengauss.admin.plugin.enums.PortalType;

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
}
