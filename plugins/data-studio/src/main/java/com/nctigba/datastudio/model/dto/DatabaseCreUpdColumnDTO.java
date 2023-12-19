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
 *  DatabaseCreUpdColumnDTO.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/model/dto/DatabaseCreUpdColumnDTO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.model.dto;

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
        private String oldType;
        private Boolean isEmpty;
        private String defaultValue;
        private String oldDefaultValue;
        private Boolean isOnly;
        private String precision;
        private String oldPrecision;
        private String scope;
        private String oldScope;
        private String comment;
        private Integer operationType;
    }
}