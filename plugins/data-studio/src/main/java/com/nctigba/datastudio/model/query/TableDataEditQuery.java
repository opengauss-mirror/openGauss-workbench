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
 *  TableDataEditQuery.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/model/query/TableDataEditQuery.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.model.query;

import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * TableDataEditQuery
 *
 * @since 2023-6-26
 */
@NoArgsConstructor
@Data
@Generated
@Slf4j
public class TableDataEditQuery {
    private String uuid;
    private String winId;
    private String schema;
    private String tableName;
    private List<TableDataDTO> data;

    @Data
    @Generated
    public static class TableDataDTO {
        private List<TableDataDTOColumn> line;
        private String operationType;
    }
    @Data
    @Generated
    public static class TableDataDTOColumn {
        private String columnData;
        private String oldColumnData;
        private String columnName;
        private String typeName;
        private int typeNum;
    }
}
