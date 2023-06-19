/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
@Data
@ApiModel("Database list content")
public class DataListDTO {
    @ApiModelProperty("schema_name")
    private String schema_name;

    @ApiModelProperty("table")
    private List<Map<String, String>> table;

    @ApiModelProperty("view")
    private List<Map<String, String>> view;

    @ApiModelProperty("FUNCTION/PROCEDURE")
    private List<Map<String, String>> fun_pro;

    @ApiModelProperty("synonym")
    private List<Map<String, String>> synonym;

    @ApiModelProperty("sequence")
    private List<Map<String, String>> sequence;

}
