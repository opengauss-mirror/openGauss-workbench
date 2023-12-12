/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
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
