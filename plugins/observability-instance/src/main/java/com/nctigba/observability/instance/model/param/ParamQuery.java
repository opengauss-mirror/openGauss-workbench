package com.nctigba.observability.instance.model.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ParamQuery {
    @ApiModelProperty("Param Name")
    private String paramName;
    @ApiModelProperty("nodeId")
    private String nodeId;
    @ApiModelProperty("password")
    private String password;
    @ApiModelProperty("is Refresh (1 or 0)")
    private String isRefresh;
}
