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
 * OpenGaussImage.java
 *
 * IDENTIFICATION
 * plugins/container-management-plugin/src/main/java/org/opengauss/admin/container/beans/OpenGaussImage.java
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
 * OpenGaussImage 类用于表示数据库中的镜像信息。
 *
 * @since 2024-08-29
 */
@Data
public class OpenGaussImage implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 镜像类型，opengauss：OpenGauss镜像，backup：备份镜像，exporter：监控镜像。
     */
    private String type;

    /**
     * 镜像架构
     */
    private String architecture;

    /**
     * 镜像操作系统
     */
    private String os;

    /**
     * 版本，例如5.0.0
     */
    private String version;

    /**
     * 镜像，例如5.0.0-20231114
     */
    private String image;

    /**
     * 是否启用。1代表启用，0代表禁用。禁用后，创建集群的时候小版本不再展示该镜像。
     */
    private Boolean enable;

    /**
     * 优先级，正整数类型。展示的时候按照优先级进行展示，优先级高的展示在前面。
     */
    private Integer priority;

    /**
     * 镜像描述
     */
    private String describe;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;
}
