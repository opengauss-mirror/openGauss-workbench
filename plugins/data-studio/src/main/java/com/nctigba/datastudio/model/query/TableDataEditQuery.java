/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
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
