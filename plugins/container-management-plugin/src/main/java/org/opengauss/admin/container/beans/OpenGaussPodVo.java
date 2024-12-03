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
 * OpenGaussPodVo.java
 *
 * IDENTIFICATION
 * plugins/container-management-plugin/src/main/java/org/opengauss/admin/container/beans/OpenGaussPodVo.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.container.beans;

import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * OpenGauss Pod的相关信息。
 *
 * @since 2024-08-29
 */
@Data
public class OpenGaussPodVo {
    /**
     * 实例名称
     */
    private String name;

    /**
     * 实例状态
     */
    private String status;

    /**
     * 主从模式
     */
    private String masterSlaveMode;

    /**
     * 规格
     */
    private Map<String, Object> requestResource;

    /**
     * CPU爆发规格
     */
    private String cpuBurst;

    /**
     * pod IP，包含IPV4和IPV6
     */
    private List<String> podIps;

    /**
     * 主机IP
     */
    private String nodeIp;

    /**
     * Pod创建时间
     */
    private Date createTime;

    /**
     * 最近重启时间
     */
    private Date lastRestartTime;
}
