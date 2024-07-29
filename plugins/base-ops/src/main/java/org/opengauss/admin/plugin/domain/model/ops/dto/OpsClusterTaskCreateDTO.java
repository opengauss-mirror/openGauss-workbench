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
 * OpsClusterTaskCreateDTO.java
 *
 * IDENTIFICATION
 * plugins/base-ops/src/main/java/org/opengauss/admin/plugin/domain/model/ops/dto/OpsClusterTaskCreateDTO.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.domain.model.ops.dto;

import lombok.Data;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterTaskEntity;
import org.springframework.beans.BeanUtils;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

/**
 * @author wangchao
 * @date 2024/06/15 09:26
 */
@Data
public class OpsClusterTaskCreateDTO extends OpsClusterTaskCommonDTO {
    @NotNull(message = "clusterNodes can not be null")
    @Valid
    private List<OpsClusterTaskNodeCreateDTO> clusterNodes;

    public OpsClusterTaskEntity toEntity() {
        OpsClusterTaskEntity target = new OpsClusterTaskEntity();
        BeanUtils.copyProperties(this, target);
        target.setDatabasePort(getDatabasePort());
        return target;
    }

    private Integer getDatabasePort() {
        return Optional.of(getPort()).orElse(5432L).intValue();
    }
}
