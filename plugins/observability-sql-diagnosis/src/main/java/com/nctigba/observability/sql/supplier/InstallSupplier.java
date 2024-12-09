/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  InstallSupplier.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/supplier/InstallSupplier.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.supplier;

import com.gitee.starblues.annotation.Supplier;
import com.nctigba.observability.sql.service.impl.AgentServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * InstallSupplier
 *
 * @author wuyuebin
 * @since 2024/12/9 14:23
 */
@Supplier("diagnosis-install")
@Slf4j
public class InstallSupplier {
    @Autowired
    private AgentServiceImpl agentServiceImpl;

    /**
     * Install agent
     *
     * @param nodeId       Install node
     * @param path         Install path
     * @param callbackPath Callback path
     * @param rootPassword Root password
     * @param port         Install port
     * @param username     Install user
     * @return AjaxResult
     */
    @Supplier.Method("agent-install")
    public AjaxResult agentInstall(String nodeId, int port, String username, String rootPassword, String path,
                                      String callbackPath) {
        try {
            agentServiceImpl.install(nodeId, port, username, rootPassword, path, callbackPath);
            return AjaxResult.success();
        } catch (Exception e) {
            log.error("agent install fail!", e);
            return AjaxResult.error("agent install fail! " + e.getMessage());
        }
    }

    /**
     * Uninstall agent
     *
     * @param nodeId       Install node
     * @param rootPassword Root password
     * @return AjaxResult
     */
    @Supplier.Method("agent-uninstall")
    public AjaxResult agentUninstall(String nodeId, String rootPassword) {
        try {
            agentServiceImpl.uninstall(nodeId, rootPassword);
            return AjaxResult.success();
        } catch (Exception e) {
            log.error("agent uninstall fail!", e);
            return AjaxResult.error("agent uninstall fail! " + e.getMessage());
        }
    }
}
