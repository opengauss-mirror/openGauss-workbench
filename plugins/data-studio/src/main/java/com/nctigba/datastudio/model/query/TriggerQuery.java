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
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * TriggerQuery
 *
 * @since 2023-10-19
 */
@NoArgsConstructor
@Data
@Generated
public class TriggerQuery {
    private String uuid;

    private String userName;

    private String oid;

    private String schema;

    private String name;

    private String newName;

    @JsonProperty("status")
    private boolean isStatus;

    private String type;

    private String tableName;

    private String newTableName;

    private String frequency;

    private String time;

    private List<String> event;

    private List<String> columnList;

    private String functionSql;

    private String function;

    private String condition;

    private String description;

    private String sql;
}
