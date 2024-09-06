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
 * OpenGaussCluster.java
 *
 * IDENTIFICATION
 * plugins/container-management-plugin/src/main/java/org/opengauss/admin/container/beans/OpenGaussCluster.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.container.beans;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * OpenGaussCluster类用于表示一个开放高斯集群的数据模型。
 *
 * @since 2024-08-29
 */
@Data
public class OpenGaussCluster implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String k8sId;

    private String name;

    private String namespace;

    private Integer port;

    private String version;

    private String image;

    private String cmdbId;

    private String appName;

    private String appCode;

    private String costCenter;

    /**
     * 架构
     */
    private String architecture;

    /**
     * 集群架构
     */
    private String archType;

    private String createUser;

    private String cpuRequest;

    private String memRequest;

    /**
     * CPU爆发倍数
     */
    private Integer cpuLimit;

    private Integer diskCapacity;

    private String resourcePool;

    private Boolean isTestCluster;

    private Boolean isAddMonitor;

    private Boolean isAddCmdb;

    private Boolean isAdd4a;

    private String remark;

    private Date createTime;

    private Date updateTime;
}
