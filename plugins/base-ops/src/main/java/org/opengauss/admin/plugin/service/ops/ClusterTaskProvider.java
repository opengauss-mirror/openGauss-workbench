/*
 * Copyright (c) 2024 Huawei Technologies Co.,Ltd.
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

import org.opengauss.admin.plugin.domain.model.ops.InstallContext;
import org.opengauss.admin.plugin.domain.model.ops.OpsClusterContext;
import org.opengauss.admin.plugin.enums.ops.OpenGaussSupportOSEnum;
import org.opengauss.admin.plugin.enums.ops.OpenGaussVersionEnum;

/**
 * Cluster Installation Service Provider Specification
 *
 * @author wangchao
 * @date 2024/6/22 9:41
 **/
public interface ClusterTaskProvider {
    /**
     * Installed openGauss version
     *
     * @return openGauss version
     */
    OpenGaussVersionEnum version();

    /**
     * get Installation dependency
     *
     * @param os OpenGaussSupportOSEnum
     * @return installation dependency
     */
    default String dependencyCommand(OpenGaussSupportOSEnum os){
        return os.getDependencyCommand();
    }

    /**
     * perform installation
     *
     * @param installContext installation context
     */
    void install(InstallContext installContext);

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
}
