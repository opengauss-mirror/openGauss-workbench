/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package org.opengauss.admin.plugin.config.runner;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import lombok.extern.slf4j.Slf4j;

import org.opengauss.admin.plugin.domain.MigrationHostPortalInstall;
import org.opengauss.admin.plugin.enums.PortalInstallStatus;
import org.opengauss.admin.plugin.service.MigrationHostPortalInstallHostService;
import org.opengauss.third.party.tools.ThirdPartyToolManager;
import org.opengauss.third.party.tools.enums.ThirdPartyToolEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Refresh portal third-party-tool install info to file system.
 *
 * @since 2026/6/18
 */
@Slf4j
@Component
public class PortalThirdPartyToolInstallInfoRefreshRunner implements ApplicationRunner {
    @Autowired
    private MigrationHostPortalInstallHostService migrationHostPortalInstallHostService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            LambdaQueryWrapper<MigrationHostPortalInstall> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.ne(MigrationHostPortalInstall::getInstallStatus, PortalInstallStatus.NOT_INSTALL.getCode());
            List<MigrationHostPortalInstall> portalList = migrationHostPortalInstallHostService.list(queryWrapper);
            for (MigrationHostPortalInstall portalInstall : portalList) {
                ThirdPartyToolManager.save(ThirdPartyToolEnum.MIGRATION_PORTAL, portalInstall.toPortalInstallInfo());
            }
        } catch (IOException e) {
            log.error("Failed to refresh portal third-party-tool install info to file system. "
                    + "Please fix this error and restart Datakit", e);
        } catch (Exception e) {
            log.error("Failed to refresh portal third-party-tool install info. ", e);
        }
    }
}
