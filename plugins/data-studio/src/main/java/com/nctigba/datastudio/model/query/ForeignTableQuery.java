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
 * ForeignTableRequest
 *
 * @since 2023-10-10
 */
@NoArgsConstructor
@Data
@Generated
public class ForeignTableQuery {
    private String uuid;

    private String role;

    private String schema;

    private String foreignTable;

    @JsonProperty("exists")
    private boolean isExists;

    private String datasourceType;

    private String foreignServer;

    private String remoteHost;

    private String remotePort;

    private String remoteDatabase;

    private String remoteUsername;

    private String remotePassword;

    private String remoteSchema;

    private String remoteTable;

    private List<ColumnDTO> columnList;

    private String description;

    /**
     * ColumnDTO
     *
     * @since 2023-10-10
     */
    @Data
    @Generated
    public static class ColumnDTO {
        private String foreignColumn;

        private String farColumn;

        private String type;

        private Boolean isEmpty;

        private String defaultValue;

        private Boolean isOnly;

        private String precision;

        private String scope;

        private String comment;
    }
}
