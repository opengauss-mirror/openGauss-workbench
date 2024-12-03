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
 * OpenGaussClusterDto.java
 *
 * IDENTIFICATION
 * plugins/container-management-plugin/src/main/java/org/opengauss/admin/container/beans/OpenGaussClusterDto.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.container.beans;

import lombok.Data;
import org.opengauss.admin.container.beans.kubernetes.DbBasicInfo;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 创建、更新操作OpenGauss集群，接口输入的基本信息
 *
 * @since 2024-08-29
 */
@Data
public class OpenGaussClusterDto implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * k8s集群ID
     */
    private String k8sId;

    /**
     * 资源池类型
     */
    private String resourceType;

    /**
     * 所属CMDB系统ID
     */
    private String cmdbSystemId;

    /**
     * 架构
     */
    private String architecture;

    /**
     * 操作系统
     */
    private String os;

    /**
     * openGauss集群名称
     */
    private String openGaussName;

    /**
     * 集群架构
     */
    private String archType;

    /**
     * openGauss集群大版本
     */
    private String version;

    /**
     * openGauss集群镜像
     */
    private String image;

    /**
     * openGauss集群端口
     */
    private Integer port;

    /**
     * 字符集
     */
    private String charset;

    /**
     * openGauss集群实例数
     */
    private Integer instance;

    /**
     * request资源规格
     */
    private Map<String, Object> requestResource;

    /**
     * limit资源规格
     */
    private Map<String, Object> limitResource;

    /**
     * 磁盘容量
     */
    private Integer diskCapacity;

    /**
     * 是否是测试集群
     */
    private Boolean isTest;

    /**
     * 备注
     */
    private String remark;

    /**
     * 用户自定义库、账户、密码
     */
    private List<DbBasicInfo> dbList;

    /**
     * 操作者
     */
    private String operator;
}

