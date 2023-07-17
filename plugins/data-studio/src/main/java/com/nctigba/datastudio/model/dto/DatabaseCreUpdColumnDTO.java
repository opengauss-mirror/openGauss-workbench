/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DatabaseCreUpdColumnDTO
 *
 * @since 2023-6-26
 */
@NoArgsConstructor
@Data
@Generated
public class DatabaseCreUpdColumnDTO {
    private String uuid;
    private List<CreUpdColumnDataDTO> data;
    private String oid;
    private String schema;
    private String tableName;

    @Data
@Generated
    public static class CreUpdColumnDataDTO {
        private String columnName;
        private String newColumnName;
        private String type;
        @JsonProperty("empty")
        private Boolean isEmpty;
        private String defaultValue;
        @JsonProperty("only")
        private Boolean isOnly;
        private String precision;
        private String scope;
        private String comment;
        private Integer operationType;
    }
}