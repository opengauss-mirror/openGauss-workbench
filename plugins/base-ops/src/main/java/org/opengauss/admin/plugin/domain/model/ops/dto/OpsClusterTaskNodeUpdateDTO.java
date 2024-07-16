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
 * OpsClusterTaskNodeUpdateDTO.java
 *
 * IDENTIFICATION
 * plugins/base-ops/src/main/java/org/opengauss/admin/plugin/domain/model/ops/dto/OpsClusterTaskNodeUpdateDTO.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.domain.model.ops.dto;

import lombok.Data;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterTaskNodeEntity;
import org.springframework.beans.BeanUtils;

import javax.validation.constraints.NotEmpty;

/**
 * @author wangchao
 * @date 2024/06/15 09:26
 */
@Data
public class OpsClusterTaskNodeUpdateDTO extends OpsClusterTaskNodeCreateDTO {
    @NotEmpty(message = "clusterNodeId不能为空")
    private String clusterNodeId;

    @NotEmpty(message = "clusterId不能为空")
    private String clusterId;

    public OpsClusterTaskNodeEntity toEntity() {
        OpsClusterTaskNodeEntity opsClusterTaskNodeEntity = new OpsClusterTaskNodeEntity();
        BeanUtils.copyProperties(this, opsClusterTaskNodeEntity);
        return opsClusterTaskNodeEntity;
    }
}
