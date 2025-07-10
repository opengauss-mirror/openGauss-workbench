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
 * OpsClusterContext.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/domain/model/ops/OpsClusterContext.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.domain.model.ops;

import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterNodeEntity;
import lombok.Data;
import org.opengauss.admin.plugin.enums.ops.ClusterRoleEnum;
import org.opengauss.admin.plugin.enums.ops.OpenGaussSupportOSEnum;

import java.util.List;

/**
 * @author lhf
 * @date 2022/9/29 17:31
 **/
@Data
public class OpsClusterContext implements Cloneable {
    private WsSession retSession;
    private RetBuffer retBuffer;
    private List<String> opNodeIds;

    private List<HostInfoHolder> hostInfoHolders;

    private OpsClusterEntity opsClusterEntity;

    private OpenGaussSupportOSEnum os;

    private List<OpsClusterNodeEntity> opsClusterNodeEntityList;

    private ClusterRoleEnum role;

    /**
     * Legal Cloning Methods
     *
     * @return OpsClusterContext
     */
    @Override
    public OpsClusterContext clone() {
        try {
            return (OpsClusterContext) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new OpsException("Failed to clone OpsClusterContext" + e);
        }
    }
}
