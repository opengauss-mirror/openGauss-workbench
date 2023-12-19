/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  ForeignTableQuery.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/model/query/ForeignTableQuery.java
 *
 *  -------------------------------------------------------------------------
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
