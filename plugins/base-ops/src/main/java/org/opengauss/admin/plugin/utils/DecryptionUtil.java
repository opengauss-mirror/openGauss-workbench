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
 * EncryptionController.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-api/src/main/java/org/opengauss/admin/web/controller/ops/EncryptionController.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.utils;

import com.gitee.starblues.bootstrap.annotation.AutowiredType;

import lombok.extern.slf4j.Slf4j;

import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.domain.model.ops.EnterpriseInstallConfig;
import org.opengauss.admin.plugin.domain.model.ops.ImportClusterBody;
import org.opengauss.admin.plugin.domain.model.ops.InstallBody;
import org.opengauss.admin.plugin.domain.model.ops.InstallContext;
import org.opengauss.admin.plugin.domain.model.ops.LiteInstallConfig;
import org.opengauss.admin.plugin.domain.model.ops.MinimalistInstallConfig;
import org.opengauss.admin.plugin.enums.ops.OpenGaussVersionEnum;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Function;

/**
 * DecryptionUtil
 *
 * @since 2025/6/26
 */
@Slf4j
@Component
public class DecryptionUtil {
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private EncryptionUtils encryptionUtils;

    /**
     * decrypt database password
     *
     * @param installBody installBody
     */
    public void decryptDatabasePassword(InstallBody installBody) {
        OpenGaussVersionEnum openGaussVersion = installBody.getInstallContext().getOpenGaussVersion();
        decryptDatabasePassword(installBody.getInstallContext(), openGaussVersion, true);
    }

    /**
     * decrypt database password
     *
     * @param importClusterBody importClusterBody
     */
    public void decryptDatabasePassword(ImportClusterBody importClusterBody) {
        OpenGaussVersionEnum openGaussVersion = importClusterBody.getOpenGaussVersion();
        decryptDatabasePassword(importClusterBody, openGaussVersion, false);
    }

    private void decryptDatabasePassword(Object installConfig, OpenGaussVersionEnum openGaussVersion,
        boolean isInstallBody) {
        switch (openGaussVersion) {
            case ENTERPRISE:
                EnterpriseInstallConfig enterpriseConfig = getInstallConfig(installConfig, isInstallBody,
                    InstallContext::getEnterpriseInstallConfig, ImportClusterBody::getEnterpriseInstallConfig);
                enterpriseConfig.setDatabasePassword(encryptionUtils.decrypt(enterpriseConfig.getDatabasePassword()));
                break;
            case MINIMAL_LIST:
                MinimalistInstallConfig minimalistConfig = getInstallConfig(installConfig, isInstallBody,
                    InstallContext::getMinimalistInstallConfig, ImportClusterBody::getMinimalistInstallConfig);
                minimalistConfig.setDatabasePassword(encryptionUtils.decrypt(minimalistConfig.getDatabasePassword()));
                break;
            case LITE:
                LiteInstallConfig liteConfig = getInstallConfig(installConfig, isInstallBody,
                    InstallContext::getLiteInstallConfig, ImportClusterBody::getLiteInstallConfig);
                liteConfig.setDatabasePassword(encryptionUtils.decrypt(liteConfig.getDatabasePassword()));
                break;
            default:
                throw new OpsException("unsupported version：[" + openGaussVersion + "]");
        }
    }

    private <T> T getInstallConfig(Object installConfig, boolean isInstallBody,
        Function<InstallContext, T> installBodyMapper, Function<ImportClusterBody, T> importClusterBodyMapper) {
        if (isInstallBody && installConfig instanceof InstallContext installCfg) {
            return installBodyMapper.apply(installCfg);
        } else if (!isInstallBody && installConfig instanceof ImportClusterBody importClusterBody) {
            return importClusterBodyMapper.apply(importClusterBody);
        } else {
            throw new IllegalArgumentException("Invalid installConfig type or mismatched flag");
        }
    }
}
