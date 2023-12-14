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
 * UpgradeContext.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/domain/model/ops/UpgradeContext.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.domain.model.ops;

import lombok.Data;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterNodeEntity;
import org.opengauss.admin.plugin.enums.ops.OpenGaussSupportOSEnum;
import org.opengauss.admin.plugin.enums.ops.UpgradeTypeEnum;

/**
 * @author lhf
 * @date 2022/12/9 11:09
 **/
@Data
public class UpgradeContext {
    private String hostPublicIp;
    private String rootPassword;
    private Integer hostPort;
    private WsSession retSession;
    private String clusterConfigXmlPath;
    private UpgradeTypeEnum upgradeType;
    private String versionNum;
    private String upgradePackagePath;
    private String sepEnvFile;
    private String installUsername;
    private String installUserPassword;
    private OpsClusterEntity clusterEntity;
    private OpsClusterNodeEntity opsClusterNodeEntity;
    private OpenGaussSupportOSEnum os;
}
