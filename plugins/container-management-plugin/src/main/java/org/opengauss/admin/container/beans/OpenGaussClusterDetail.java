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
 */

package org.opengauss.admin.container.beans;

import lombok.Data;

import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * OpenGauss集群的详细信息类
 *
 * @since 2024-08-29
 */
@Data
public class OpenGaussClusterDetail {
    /**
     * openGauss集群名
     */
    private String name;

    /**
     * cr namesapce
     */
    private String namespace;

    /**
     * k8s ID
     */
    private String k8sId;

    /**
     * k8s名
     */
    private String k8sName;

    /**
     * 资源池
     */
    private String resourceType;

    /**
     * 所属系统
     */
    private String cmdbSysName;

    /**
     * 架构
     */
    private String architecture;

    /**
     * 集群架构
     */
    private String archType;

    /**
     * openGauss镜像
     */
    private String openGaussImage;

    /**
     * 备份镜像
     */
    private String backRecoveryImage;

    /**
     * 监控采集镜像
     */
    private String exporterImage;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 集群规模
     */
    private Integer instance;

    /**
     * 资源规格
     */
    private Map<String, Object> requestResource;

    /**
     * CPU爆发倍数
     */
    private Integer scaleTimes;

    /**
     * 磁盘容量大小
     */
    private Integer diskCapacity;

    /**
     * 创建者
     */
    private String creator;

    /**
     * 是否测试集群
     */
    private Boolean isTestCluster;

    /**
     * 是否添加监控
     */
    private Boolean isAddMonitor;

    /**
     * 是否添加4A
     */
    private Boolean isAdd4a;

    /**
     * 是否添加CMDB
     */
    private Boolean isAddCmdb;

    /**
     * 集群创建时间
     */
    private Date createTime;

    /**
     * 域名
     */
    private Set<String> domain;

    /**
     * 记录
     */
    private String remark;

    /**
     * 集群状态
     */
    private String clusterStatus;
}