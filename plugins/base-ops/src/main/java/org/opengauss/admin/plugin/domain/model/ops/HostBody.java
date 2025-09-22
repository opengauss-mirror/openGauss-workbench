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
 * HostBody.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/domain/model/ops/HostBody.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.domain.model.ops;

import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import jakarta.validation.constraints.NotEmpty;

/**
 * @author lhf
 * @date 2022/8/11 20:07
 **/
@Slf4j
@Data
public class HostBody {
    @NotEmpty(message = "The IP address cannot be empty")
    private String publicIp;
    @NotEmpty(message = "The Intranet IP address cannot be empty")
    private String privateIp;
    @NotEmpty(message = "The password cannot be empty")
    private String password;
    @NotEmpty(message = "Please select AZ")
    private String azId;
    private String remark;
    private Integer port;

    public OpsHostUserEntity toRootUser(String hostId) {
        OpsHostUserEntity hostUserEntity = new OpsHostUserEntity();
        hostUserEntity.setUsername("root");
        hostUserEntity.setPassword(password);
        hostUserEntity.setHostId(hostId);
        return hostUserEntity;
    }
}
