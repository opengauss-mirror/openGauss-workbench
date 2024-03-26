/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
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
 * SystemUploadPathInit.java
 *
 * IDENTIFICATION
 * openGauss-datakit/visualtool-api/src/main/java/org/opengauss/admin/web/core/dbinit/SystemUploadPathInit.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.web.core.dbinit;

import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.config.SystemConfig;
import org.opengauss.admin.common.core.domain.entity.SysSettingEntity;
import org.opengauss.admin.system.service.ISysSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.io.File;
import java.io.IOException;

/**
 * The class is used to initialize the system upload path.
 *
 * @since 2024/3/22 10:10
 */
@Slf4j
@Component
public class SystemUploadPathInit implements CommandLineRunner {
    @Autowired
    private ISysSettingService sysSettingService;

    /**
     * run
     *
     * @param args args
     */
    @Override
    public void run(String... args) {
        Integer adminId = 1;
        SysSettingEntity setting = sysSettingService.getSetting(adminId);
        if (ObjectUtils.isEmpty(setting)) {
            log.error("Unable to obtain the administrator user. "
                    + "Failed to initialize the system upload path.");
            return;
        }

        String defaultUploadPath = "/ops/files/";
        if (!setting.getUploadPath().equals(defaultUploadPath)) {
            mkdirFile(setting.getUploadPath());
            return;
        }

        mkdirFile(SystemConfig.getUploadPath());

        setting.setUploadPath(SystemConfig.getUploadPath());
        sysSettingService.saveOrUpdate(setting);
    }

    /**
     * mkdirs file by file path
     *
     * @param filePath file path
     */
    private void mkdirFile(String filePath) {
        try {
            File pathFile = new File(filePath);
            if (!pathFile.exists() && !pathFile.mkdirs()) {
                throw new IOException(String.format("Folder %s create failed.", filePath));
            }
        } catch (IOException e) {
            log.error("Default system upload path create failed: ", e);
        }
    }
}
