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

package com.nctigba.observability.instance.model.dto;

import lombok.Data;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;

import java.util.Date;

/**
 * NctigbaEnvDTO
 *
 * @author luomeng
 * @since 2024/4/8
 */
@Data
public class NctigbaEnvDTO {
    String id;
    String hostId;
    String type;
    String username;
    String path;
    Integer port;
    String nodeId;
    OpsHostEntity host;
    String status;
    String param;
    Date updateTime;
}
