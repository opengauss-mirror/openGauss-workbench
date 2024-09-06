/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
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
 * OpenGaussClusterSpec.java
 *
 * IDENTIFICATION
 * plugins/container-management-plugin/src/main/java/org/opengauss/admin/container/beans/kubernetes
 * /OpenGaussClusterSpec.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.container.beans.kubernetes;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 集群信息
 *
 * @since 2024-08-29
 */
@Data
public class OpenGaussClusterSpec implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 部署架构
     */
    private String architecture;

    /**
     * 网络类型: overlay/underlay
     */
    private String networkType;

    /**
     * 数据节点数量
     */
    private Integer dataNodeNum;

    /**
     * true：人工维护，false：operator维护
     */
    private Boolean maintenance;

    /**
     * 镜像
     */
    private ContainerImage images;

    /**
     * gaussDB 资源规格
     */
    private ResourceList dbResources;

    /**
     * gaussDB 存储模板
     */
    private DBStorageTemplate dbStorage;

    /**
     * openGauss服务端口
     */
    private Integer dbPort;

    /**
     * NodePort起始端口
     */
    private Integer ports;

    /**
     * 主机标签
     */
    private String nodeAffinity;

    /**
     * 环境变量
     */
    private List<EnvVar> env;

    /**
     * 用户自定义database、用户名、密码
     */
    private List<DbBasicInfo> databaseInfo;

    /**
     * 备份恢复
     */
    private BackRecoverySpec backRecovery;

    /**
     * switchover message
     */
    private SwitchoverMessage switchover;

    /**
     * 备机重建
     */
    private StandbyReconstructSpec standbyReconstruct;

    /**
     * 初始化(gauss账号密码、权限设置)
     */
    private Boolean isInit;

    /**
     * 节点驱逐
     */
    private InstanceMigrationSpec instanceMigration;

    /**
     * 滚动重启标志
     */
    private Boolean dbRollingRestartFlag;
}