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

import java.io.Serializable;
import java.util.Date;

/**
 * k8s集群的数据模型
 *
 * @since 2024-08-29
 */
@Data
public class K8sCluster implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * k8s集群ID
     */
    private String id;

    /**
     * k8s集群名字
     */
    private String name;

    /**
     * k8s API Server地址
     */
    private String apiServer;

    /**
     * k8s API Server端口
     */
    private Integer port;

    /**
     * k8s token
     */
    private String token;

    /**
     * k8s 集群域名后缀
     */
    private String domain;

    /**
     * prometheus地址
     */
    private String prometheusUrl;

    /**
     * Harbor仓库地址
     */
    private String harborAddress;

    /**
     * 资源池，通过逗号隔开
     */
    private String resourcePool;

    /**
     * 是否启用k8s集群
     */
    private Boolean isEnable;

    /**
     * 数据创建时间
     */
    private Date createTime;

    /**
     * 数据修改时间
     */
    private Date updateTime;
}
