package org.opengauss.admin.plugin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.utils.StringUtils;
import org.opengauss.admin.plugin.domain.MigrationToolPortalDownloadInfo;
import org.opengauss.admin.plugin.exception.PortalInstallException;
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
@Slf4j
@Service
public class MigrationToolPortalDownloadInfoServiceImpl extends ServiceImpl<MigrationToolPortalDownloadInfoMapper, MigrationToolPortalDownloadInfo> implements MigrationToolPortalDownloadInfoService {
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private IHostService hostService;

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

        if (ObjectUtils.isEmpty(portalDownloadInfos) || ObjectUtils.isEmpty(portalDownloadInfos.get(0))) {
            String errorMsg = String.format("There is no matching portal version for %s%s-%s. Please use another host.", os, osVersion, cpuArch);
            log.error(errorMsg);
            throw new PortalInstallException(errorMsg);
        }

        return portalDownloadInfos;
    }
}
