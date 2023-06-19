/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.query;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ExportRequest {
    private String uuid;

    private String schema;

    private boolean dataFlag;

    private List<String> tableList;

    private String tableName;

    private List<String> columnList;

    private String fileType;

    private boolean titleFlag;

    private String quote;

    private String escape;

    private String replaceNull;

    private String delimiter;

    private String encoding;

    private List<Integer> functionMap;

    private List<String> viewList;

    private List<String> sequenceList;

    private List<String> schemaList;
}
