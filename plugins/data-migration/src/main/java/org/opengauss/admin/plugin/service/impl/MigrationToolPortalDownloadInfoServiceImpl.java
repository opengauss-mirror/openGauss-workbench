package org.opengauss.admin.plugin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.plugin.domain.MigrationToolPortalDownloadInfo;
import org.opengauss.admin.plugin.exception.PortalInstallException;
import org.opengauss.admin.plugin.mapper.MigrationToolPortalDownloadInfoMapper;
import org.opengauss.admin.plugin.service.MigrationToolPortalDownloadInfoService;
import org.opengauss.admin.system.service.ops.IHostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Arrays;
import java.util.Collections;
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
    private static final List<String> SUPPORT_SYSTEM_INFO_LIST = Collections.unmodifiableList(
            Arrays.asList(
                    "centos7_x86_64",
                    "openEuler20.03_x86_64",
                    "openEuler20.03_aarch64",
                    "openEuler22.03_x86_64",
                    "openEuler22.03_aarch64"
            )
    );

    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private IHostService hostService;

    /**
     * get portal installation package download information list
     *
     * @param hostId the host ID
     * @return package download information list
     */
    @Override
    public List<MigrationToolPortalDownloadInfo> getPortalDownloadInfoList(String hostId) {
        OpsHostEntity opsHostEntity = checkHost(hostId);
        LambdaQueryWrapper<MigrationToolPortalDownloadInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MigrationToolPortalDownloadInfo::getHostOs, opsHostEntity.getOs())
                .eq(MigrationToolPortalDownloadInfo::getHostCpuArch, opsHostEntity.getCpuArch())
                .eq(MigrationToolPortalDownloadInfo::getHostOsVersion, opsHostEntity.getOsVersion());

        List<MigrationToolPortalDownloadInfo> portalDownloadInfos = list(queryWrapper);
        if (ObjectUtils.isEmpty(portalDownloadInfos)) {
            throw new PortalInstallException("The online installation package information list is empty");
        }
        return portalDownloadInfos;
    }

    private OpsHostEntity checkHost(String hostId) {
        OpsHostEntity opsHost = hostService.getById(hostId);
        if (ObjectUtils.isEmpty(opsHost)) {
            log.error("Cannot get host information by host id: {}", hostId);
            throw new PortalInstallException("Host does not exist");
        }

        String os = opsHost.getOs();
        String osVersion = opsHost.getOsVersion();
        String cpuArch = opsHost.getCpuArch();
        if (ObjectUtils.isEmpty(os)) {
            throw new PortalInstallException("Failed to get the host os information");
        }
        if (ObjectUtils.isEmpty(osVersion)) {
            throw new PortalInstallException("Failed to get the host os version");
        }
        if (ObjectUtils.isEmpty(cpuArch)) {
            throw new PortalInstallException("Failed to get the host cpu architecture");
        }

        String sysInfo = os + osVersion + "_" + cpuArch;
        if (SUPPORT_SYSTEM_INFO_LIST.stream().noneMatch(item -> item.equalsIgnoreCase(sysInfo))) {
            throw new PortalInstallException(
                    "No matching installation package for this system architecture: " + os + osVersion + "_" + cpuArch);
        }
        return opsHost;
    }
}
