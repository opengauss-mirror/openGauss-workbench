package com.nctigba.datastudio.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
@ApiModel("Database list content")
public class DataListDTO {
    @ApiModelProperty("schema_name")
    private String schema_name;

    @ApiModelProperty("table")
    private List<String> table;

    @ApiModelProperty("view")
    private List<String> view;

    @ApiModelProperty("FUNCTION/PROCEDURE")
    private List<String> fun_pro;

    @ApiModelProperty("synonym")
    private List<String> synonym;

    @ApiModelProperty("sequence")
    private List<String> sequence;

}
