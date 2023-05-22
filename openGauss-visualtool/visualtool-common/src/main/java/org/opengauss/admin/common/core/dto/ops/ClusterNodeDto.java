/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
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
 * ClusterNodeDto.java
 *
 * IDENTIFICATION
 * data-migration/src/main/java/org/opengauss/admin/common/core/dto/ops/ClusterNodeDto.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.common.core.dto.ops;

import lombok.Data;

/**
 * ClusterNodeDto Object
 *
 * @author: xielibo
 * @date: 2023-05-17 17:01
 **/
@Data
public class ClusterNodeDto {

    /**
     * publicIp of ClusterNode
     */
    private String publicIp;
    /**
     * port of ClusterNode
     */
    private Integer port;
    /**
     * installUserId of ClusterNode
     */
    private String installUserId;
    /**
     * installUserName of ClusterNode
     */
    private String installUserName;
    /**
     * installPassword of ClusterNode
     */
    private String installPassword;
    /**
     * installPath of ClusterNode
     */
    private String installPath;
    /**
     * dataPath of ClusterNode
     */
    private String dataPath;
    /**
     * is mini version
     */
    private Boolean isMiniVersion;
}
