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
 * OpsClusterTaskEntity.java
 *
 * IDENTIFICATION
 * plugins/base-ops/src/main/java/org/opengauss/admin/plugin/domain/entity/ops/OpsClusterTaskEntity.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.domain.entity.ops;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.opengauss.admin.plugin.domain.BaseEntity;
import org.opengauss.admin.common.enums.ops.ClusterEnvCheckResultEnum;
import org.opengauss.admin.common.enums.ops.OpsClusterTaskStatusEnum;
import org.opengauss.admin.plugin.domain.model.ops.OpsClusterTaskVO;
import org.opengauss.admin.plugin.enums.ops.DeployTypeEnum;
import org.opengauss.admin.plugin.enums.ops.OpenGaussVersionEnum;
import org.springframework.beans.BeanUtils;

/**
 * OpsClusterTaskEntity
 *
 * @author wangchao
 * @date 2024/6/22 9:41
 **/
@Data
@TableName("ops_cluster_task")
@EqualsAndHashCode(callSuper = true)
public class OpsClusterTaskEntity extends BaseEntity {

    /**
     * clusterId cluster task id ,and cluster id
     */
    @TableId
    @TableField(fill = FieldFill.INSERT)
    private String clusterId;

    /**
     * cluster task install servers host id
     */
    private String hostId;

    @TableField(exist = false)
    private String hostIp;
    @TableField(exist = false)
    private String displayHostIp;
    /**
     * cluster task install servers user id
     */
    private String hostUserId;

    @TableField(exist = false)
    private String hostUsername;

    /**
     * server base info : os
     */
    private String os;
    /**
     * server base info : cpu arch
     */
    private String cpuArch;

    /**
     * install package pre-filtering conditions : version
     */
    private OpenGaussVersionEnum version;
    /**
     * install package pre-filtering conditions : version num
     */
    private String versionNum;
    /**
     * install package pre-filtering conditions : package name
     */
    private String packageName;
    /**
     * install package pre-filtering  result : package id
     */
    private String packageId;


    /**
     * cluster install base info  : cluster name
     */
    private String clusterName;
    /**
     * cluster install base info  : database username
     */
    private String databaseUsername;
    /**
     * cluster install base info  : database password
     */
    private String databasePassword;
    /**
     * cluster install base info  : database port
     */
    private Integer databasePort;
    /**
     * cluster install base info  : install package path
     */
    private String installPackagePath;
    /**
     * cluster install base info  : install path
     */
    private String installPath;
    /**
     * cluster install base info  : log path
     */
    private String logPath;
    /**
     * cluster install base info  : tmp path
     */
    private String tmpPath;
    /**
     * cluster install base info  : om tools path
     */
    private String omToolsPath;
    /**
     * cluster install base info  : core path
     */
    private String corePath;

    /**
     * cluster install base info  : env path
     */
    private String envPath;
    /**
     * cluster install base info  : enable install cm tool
     */
    private Boolean enableCmTool;
    private Boolean enableDcf;
    /**
     * cluster install base info  : enable generate environment variable file
     */
    private Boolean enableGenerateEnvironmentVariableFile;
    /**
     * cluster install base info  : xml config path
     */
    private String xmlConfigPath;
    /**
     * cluster install base info  : cluster node num
     */
    private Integer clusterNodeNum;
    private DeployTypeEnum deployType;
    private OpsClusterTaskStatusEnum status;
    private ClusterEnvCheckResultEnum envCheckResult;

    public OpsClusterTaskVO toVo() {
        OpsClusterTaskVO target = new OpsClusterTaskVO();
        BeanUtils.copyProperties(this, target);
        return target;
    }
}
