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
 * OpsClusterEntity.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/domain/entity/ops/OpsClusterEntity.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.domain.entity.ops;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.opengauss.admin.plugin.domain.BaseEntity;
import org.opengauss.admin.plugin.enums.ops.DeployTypeEnum;
import org.opengauss.admin.plugin.enums.ops.InstallModeEnum;
import org.opengauss.admin.plugin.enums.ops.OpenGaussVersionEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author lhf
 * @date 2022/8/12 09:01
 **/
@Data
@TableName("ops_cluster")
@EqualsAndHashCode(callSuper = true)
public class OpsClusterEntity extends BaseEntity {
    @TableId
    private String clusterId;
    private OpenGaussVersionEnum version;
    private String versionNum;
    private InstallModeEnum installMode;
    private DeployTypeEnum deployType;
    private String clusterName;
    private String installPackagePath;
    private String databaseUsername;
    private String databasePassword;

    private String installPath;
    private String logPath;
    private String tmpPath;
    private String omToolsPath;
    private String corePath;
    private Integer port;
    private Boolean enableDcf;
    private Integer dcfPort;
    private String envPath;
    private String xmlConfigPath;
}
