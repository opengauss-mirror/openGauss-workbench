/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.query;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * ExportRequest
 *
 * @since 2023-6-26
 */
@Data
@Generated
@NoArgsConstructor
public class ExportQuery {
    private String uuid;

    private String schema;

    @JsonProperty("dataFlag")
    private boolean isDataFlag;

    private List<String> tableList;

    private String tableName;

    private List<String> columnList;

    private String columnString;

    private String fileType;

    @JsonProperty("titleFlag")
    private boolean isTitleFlag;

    private String quote;

    private String escape;

    private String replaceNull;

    private String delimiter;

    private String encoding;

    private List<Map<String, Object>> functionMap;

    private List<String> viewList;

    private List<String> sequenceList;

    private List<String> schemaList;

    private String timeFormat;

    private MultipartFile file;
}
