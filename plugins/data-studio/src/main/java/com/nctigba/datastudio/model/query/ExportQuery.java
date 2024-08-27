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
 *  ExportQuery.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/model/query/ExportQuery.java
 *
 *  -------------------------------------------------------------------------
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

    private String sql;
}
