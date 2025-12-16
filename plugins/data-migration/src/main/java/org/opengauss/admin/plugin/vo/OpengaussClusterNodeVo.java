/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.plugin.vo;

import lombok.Data;

/**
 * openGauss cluster node vo
 *
 * @since 2025/12/1
 */
@Data
public class OpengaussClusterNodeVo {
    private String nodeId;
    private String publicIp;
    private String privateIp;
    private String hostname;
    private String hostId;
    private Integer dbPort;
    private String dbName;
    private String dbUser;
    private String dbUserPassword;
    private Integer hostPort;
    private Boolean isSystemAdmin;
}
