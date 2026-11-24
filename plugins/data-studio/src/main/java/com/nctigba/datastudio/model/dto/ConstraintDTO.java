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

package com.nctigba.datastudio.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

/**
 * ConstraintDTO
 *
 * @since 2023-6-26
 */
@NoArgsConstructor
@Data
@Generated
public class ConstraintDTO {
    private String attName;
    private String conName;
    private String oldConName;
    private String conType;
    private String constraintDef;
    @JsonProperty("conDeferrable")
    private Boolean conDeferrable;
    private String nspName;
    private String tbName;
    private String colName;
    private String description;
    private int type;
}
