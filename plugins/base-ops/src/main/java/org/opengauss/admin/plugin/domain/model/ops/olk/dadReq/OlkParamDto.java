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
 * OlkParamDto.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/domain/model/ops/olk/dadReq/OlkParamDto.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.domain.model.ops.olk.dadReq;

import lombok.Data;

@Data
public class OlkParamDto {
    private String xmx = "16G";
    private String maxMemory = "50GB";
    private String maxTotalMemory = "50GB";
    private String maxMemoryPer = "10GB";
    private String maxTotalMemoryPer = "10GB";
}
