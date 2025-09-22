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
 * WdrGeneratorBody.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/domain/model/ops/WdrGeneratorBody.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.domain.model.ops;

import org.opengauss.admin.plugin.enums.ops.WdrScopeEnum;
import org.opengauss.admin.plugin.enums.ops.WdrTypeEnum;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * @author lhf
 * @date 2022/10/13 15:22
 **/
@Data
public class WdrGeneratorBody {
    @NotBlank(message = "The cluster ID cannot be empty")
    private String clusterId;
    @NotNull(message = "The report scope cannot be empty")
    private WdrScopeEnum scope;
    private String hostId;
    @NotNull(message = "The report type cannot be empty")
    private WdrTypeEnum type;
    @NotNull(message = "start Snapshot ID cannot be empty")
    private String startId;
    @NotNull(message = "end Snapshot ID cannot be empty")
    private String endId;
}
