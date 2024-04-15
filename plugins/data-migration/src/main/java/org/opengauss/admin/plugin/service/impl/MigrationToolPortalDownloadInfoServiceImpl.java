package org.opengauss.admin.plugin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.utils.StringUtils;
import org.opengauss.admin.plugin.domain.MigrationToolPortalDownloadInfo;
import org.opengauss.admin.plugin.mapper.MigrationToolPortalDownloadInfoMapper;
import org.opengauss.admin.plugin.service.MigrationToolPortalDownloadInfoService;
import org.opengauss.admin.system.service.ops.IHostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;

/**
 * MigrationToolPortalDownloadInfoServiceImpl
 *
 * @author duanguoqiang
 * @date 2024/1/31
 */
@Service
public class MigrationToolPortalDownloadInfoServiceImpl extends ServiceImpl<MigrationToolPortalDownloadInfoMapper, MigrationToolPortalDownloadInfo> implements MigrationToolPortalDownloadInfoService {
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private IHostService hostService;

    /**
     * get portal download information list
     *
     * @author duanguoqiang
     * @param hostId the host ID
     * @return java.util.List<org.opengauss.admin.plugin.domain.MigrationToolPortalDownloadInfo>
     * @since 0.0
     */
    @Override
    public List<MigrationToolPortalDownloadInfo> getPortalDownloadInfoList(String hostId) {
        OpsHostEntity opsHostEntity = hostService.getMappedHostEntityById(hostId);
        if (StringUtils.isEmpty(opsHostEntity.getOsVersion())) {
            hostService.updateHostOsVersion(opsHostEntity);
        }
        String osVersion = opsHostEntity.getOsVersion();
        String os = opsHostEntity.getOs();
        String cpuArch = opsHostEntity.getCpuArch();

        LambdaQueryWrapper<MigrationToolPortalDownloadInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MigrationToolPortalDownloadInfo::getHostOs, os).eq(MigrationToolPortalDownloadInfo::getHostCpuArch, cpuArch).eq(MigrationToolPortalDownloadInfo::getHostOsVersion, osVersion);
        List<MigrationToolPortalDownloadInfo> portalDownloadInfos = list(queryWrapper);

        if (ObjectUtils.isEmpty(portalDownloadInfos)) {
            queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MigrationToolPortalDownloadInfo::getHostOs, "centos")
                    .eq(MigrationToolPortalDownloadInfo::getHostCpuArch, "x86_64")
                    .eq(MigrationToolPortalDownloadInfo::getHostOsVersion, "7");
            portalDownloadInfos = list(queryWrapper);
        }

        return portalDownloadInfos;
    }
}
