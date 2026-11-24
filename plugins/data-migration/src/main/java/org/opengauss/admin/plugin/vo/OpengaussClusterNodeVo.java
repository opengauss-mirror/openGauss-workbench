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
