/*
 * Copyright (c) 2024 Huawei Technologies Co.,Ltd.
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
 * OpsClusterTaskNodeCreateDTO.java
 *
 * IDENTIFICATION
 * plugins/base-ops/src/main/java/org/opengauss/admin/plugin/domain/model/ops/dto/OpsClusterTaskNodeCreateDTO.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.domain.model.ops.dto;

import lombok.Data;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterTaskNodeEntity;
import org.springframework.beans.BeanUtils;

import javax.validation.constraints.NotBlank;

/**
 * @author wangchao
 * @date 2024/06/15 09:26
 */
@Data
public class OpsClusterTaskNodeCreateDTO {
    @NotBlank(message = "hostIp cannot be empty")
    private String hostId;
    @NotBlank(message = "hostUserId cannot be empty")
    private String hostUserId;
    @NotBlank(message = "nodeType cannot be empty")
    private String nodeType;
    @NotBlank(message = "dataPath cannot be empty")
    private String dataPath;
    private String azOwner;
    private String azPriority;

    public OpsClusterTaskNodeEntity toEntity() {
        OpsClusterTaskNodeEntity opsClusterTaskNodeEntity = new OpsClusterTaskNodeEntity();
        BeanUtils.copyProperties(this, opsClusterTaskNodeEntity);
        return opsClusterTaskNodeEntity;
    }
}
