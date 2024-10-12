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
 * OpsClusterTaskVO.java
 *
 * IDENTIFICATION
 * plugins/base-ops/src/main/java/org/opengauss/admin/plugin/domain/model/ops/OpsClusterTaskVO.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.domain.model.ops;

import lombok.Data;
import org.opengauss.admin.common.enums.ops.OpsClusterTaskStatusEnum;
import org.opengauss.admin.plugin.enums.ops.DeployTypeEnum;
import org.opengauss.admin.plugin.enums.ops.OpenGaussVersionEnum;

import java.util.List;

/**
 * OpsClusterTaskVO
 *
 * @author wangchao
 * @date 2024/06/15 09:26
 */
@Data
public class OpsClusterTaskVO {
    private String clusterId;
    private String hostIp;
    private String hostId;
    private String displayHostIp;
    private String hostUserId;
    private String hostUsername;
    private String os;
    private String cpuArch;
    private OpenGaussVersionEnum version;
    private String versionNum;
    private String packageName;
    private String packageId;
    private String clusterName;
    private String databaseUsername;
    private String databasePassword;
    private Integer databasePort;
    private String installPackagePath;
    private String installPath;
    private String logPath;
    private String tmpPath;
    private String omToolsPath;
    private String corePath;
    private String envPath;
    private Boolean enableCmTool;
    private Boolean enableDcf;
    private Boolean enableGenerateEnvironmentVariableFile;
    private String xmlConfigPath;
    private DeployTypeEnum deployType;
    private Integer clusterNodeNum;
    private OpsClusterTaskStatusEnum status;
    private List<OpsClusterTaskNodeVO> clusterNodes;
}
