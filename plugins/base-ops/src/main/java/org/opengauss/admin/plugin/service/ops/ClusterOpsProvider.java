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
 * ClusterOpsProvider.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/ops/ClusterOpsProvider.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.ops;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterNodeEntity;
import org.opengauss.admin.plugin.domain.model.ops.InstallContext;
import org.opengauss.admin.plugin.domain.model.ops.OpsClusterContext;
import org.opengauss.admin.plugin.domain.model.ops.SshCommandConstants;
import org.opengauss.admin.plugin.domain.model.ops.UnInstallContext;
import org.opengauss.admin.plugin.enums.ops.OpenGaussSupportOSEnum;
import org.opengauss.admin.plugin.enums.ops.OpenGaussVersionEnum;
import org.opengauss.admin.plugin.enums.ops.WdrScopeEnum;
import com.jcraft.jsch.Session;

import java.util.List;

/**
 * Cluster Installation Service Provider Specification
 *
 * @author lhf
 * @date 2022/8/12 09:09
 **/
public interface ClusterOpsProvider {
    /**
     * Installed openGauss version
     *
     * @return openGauss version
     */
    OpenGaussVersionEnum version();

    OpenGaussSupportOSEnum os();

    default String dependencyCommand(){
        return SshCommandConstants.INSTALL_DEPENDENCY;
    }

    /**
     * perform installation
     *
     * @param installContext installation context
     */
    void install(InstallContext installContext);

    /**
     * perform uninstall
     *
     * @param unInstallContext unload context
     */
    void uninstall(UnInstallContext unInstallContext);

    /**
     * reboot
     *
     * @param opsClusterContext
     */
    void restart(OpsClusterContext opsClusterContext);

    /**
     * start up
     *
     * @param opsClusterContext
     */
    void start(OpsClusterContext opsClusterContext);

    /**
     * stop
     *
     * @param opsClusterContext
     */
    void stop(OpsClusterContext opsClusterContext);

    /**
     * Whether to enable WdrSnapshot
     *
     * @param session
     * @param clusterEntity
     * @param opsClusterNodeEntities
     * @param scope
     * @param dataPath
     */
    void enableWdrSnapshot(Session session, OpsClusterEntity clusterEntity, List<OpsClusterNodeEntity> opsClusterNodeEntities, WdrScopeEnum scope, String dataPath);
}
