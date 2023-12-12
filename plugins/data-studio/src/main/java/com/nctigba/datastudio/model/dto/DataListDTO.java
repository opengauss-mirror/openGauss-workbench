/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.dto;

import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DataListDTO
 *
 * @since 2023-6-26
 */
@NoArgsConstructor
@Data
@Generated
public class DataListDTO {
    private String schema_name;

    private List<Map<String, String>> table;

    private List<Map<String, String>> view;

    private List<Map<String, Object>> fun_pro;

    private List<Map<String, String>> synonym;

    private List<Map<String, String>> sequence;

    private List<Map<String, String>> foreignTable;

    private List<Map<String, Object>> trigger;

}
