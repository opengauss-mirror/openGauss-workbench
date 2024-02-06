package org.opengauss.admin.plugin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.plugin.domain.MigrationToolPortalDownloadInfo;

import java.util.List;

/**
 * @author duanguoqiang
 * @date 2024/1/31
 * @description MigrationToolPortalDownloadInfoService
 */
public interface MigrationToolPortalDownloadInfoService extends IService<MigrationToolPortalDownloadInfo> {
    List<MigrationToolPortalDownloadInfo> getPortalDownloadInfoList(String hostId);
}
