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
 * HostUserBody.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/domain/model/ops/HostUserBody.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.domain.model.ops;

import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * @author lhf
 * @date 2022/8/17 18:01
 **/
@Data
public class HostUserBody {
    @NotBlank(message = "The host ID cannot be empty")
    private String hostId;
    @NotBlank(message = "The user name cannot be empty")
    private String username;
    @NotBlank(message = "The password cannot be empty")
    private String password;

    public OpsHostUserEntity toEntity() {
        OpsHostUserEntity hostUserEntity = new OpsHostUserEntity();
        hostUserEntity.setHostId(hostId);
        hostUserEntity.setUsername(username);
        hostUserEntity.setPassword(password);
        return hostUserEntity;
    }
}
