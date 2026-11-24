/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.admin.container.beans;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * 展示层OpenGauss集群的相关信息。
 *
 * @since 2024-08-29
 */
@Data
public class OpenGaussClusterVo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 数据库ID
     */
    private Long id;

    /**
     * 集群名字
     */
    private String name;

    /**
     * CMDB系统名字
     */
    private String cmdbSysName;

    /**
     * 集群域名
     */
    private Set<String> domains;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 实例数
     */
    private Integer instance;

    /**
     * 资源规格
     */
    private Map<String, Object> resourceRequest;

    /**
     * CPU爆发倍数
     */
    private Integer cpuBurstMultiple;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 集群状态
     */
    private String clusterStatus;

    /**
     * 集群维修
     */
    private Boolean maintenance;
}
