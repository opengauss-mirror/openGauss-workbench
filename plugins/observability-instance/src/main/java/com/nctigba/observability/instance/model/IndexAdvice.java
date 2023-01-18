package com.nctigba.observability.instance.model;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * entity of index advice table
 * </p>
 *
 * @author gouxj@vastdata.com.cn
 * @since 2022/9/22 14:39
 */
@Data
public class IndexAdvice {
    @ApiModelProperty("Pattern Name")
    String schema;
    @ApiModelProperty("Table Name")
    String table;
    @ApiModelProperty("Column name")
    String column;
    @ApiModelProperty("Index Type")
    String indexType;
}
