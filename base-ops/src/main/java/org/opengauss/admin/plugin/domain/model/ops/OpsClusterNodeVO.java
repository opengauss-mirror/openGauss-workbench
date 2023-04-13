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
 * OpsClusterNodeVO.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/domain/model/ops/OpsClusterNodeVO.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.domain.model.ops;

import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterNodeEntity;
import lombok.Data;

/**
 * @author lhf
 * @date 2022/9/26 17:39
 **/
@Data
public class OpsClusterNodeVO {
    private String nodeId;
    private String clusterRole;
    private String publicIp;
    private String privateIp;
    private String hostname;
    private String azName;
    private String azAddress;
    private String hostId;
    private Integer dbPort;
    private String dbName;
    private String dbUser;
    private String dbUserPassword;
    private Integer hostPort;
    //private String rootPassword;
    private Boolean isRemember;
    private String installUserName;

    public static OpsClusterNodeVO of(OpsClusterNodeEntity opsClusterNodeEntity) {
        OpsClusterNodeVO opsClusterNodeVO = new OpsClusterNodeVO();
        opsClusterNodeVO.setNodeId(opsClusterNodeEntity.getClusterNodeId());
        opsClusterNodeVO.setClusterRole(opsClusterNodeEntity.getClusterRole().name());
        return opsClusterNodeVO;
    }
}
