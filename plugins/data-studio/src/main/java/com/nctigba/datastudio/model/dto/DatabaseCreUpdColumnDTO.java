/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class DatabaseCreUpdColumnDTO {
    private String uuid;
    private List<CreUpdColumnDataDTO> data;
    private String oid;
    private String schema;
    private String tableName;

    @Data
    public static class CreUpdColumnDataDTO {
        private String columnName;
        private String newColumnName;
        private String type;
        private Boolean empty;
        private String defaultValue;
        private Boolean only;
        private String precision;
        private String scope;
        private String comment;
        private Integer operationType;
    }
}