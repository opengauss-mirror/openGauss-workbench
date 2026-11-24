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

package com.nctigba.datastudio.model.query;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Generated;

/**
 * TableUnderlyingInfoQuery
 *
 * @since 2023-6-26
 */
@Data
@Generated
public class TableUnderlyingInfoQuery {
    private String tableName;
    @JsonProperty("exists")
    private Boolean isExists;
    private String tableType;
    @JsonProperty("oids")
    private Boolean isOids;
    private String tableSpace;
    private Integer fillingFactor;
    private String storage;
    private String comment;
}
