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
 * ExportRequest
 *
 * @since 2023-6-26
 */
@Data
@Generated
@NoArgsConstructor
public class ExportRequest {
    private String uuid;

    private String schema;

    @JsonProperty("dataFlag")
    private boolean isDataFlag;

    private List<String> tableList;

    private String tableName;

    private List<String> columnList;

    private String fileType;

    @JsonProperty("titleFlag")
    private boolean isTitleFlag;

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
