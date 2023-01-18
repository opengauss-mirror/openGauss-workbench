package com.nctigba.observability.instance.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class InstanceInfo {
    @ApiModelProperty("Instance Name")
    private String name;

    @ApiModelProperty(notes = "Instance Type")
    private String type;

    @ApiModelProperty("Database version")
    private String dbVersion;

    @ApiModelProperty("Example description")
    private String remark;

    @ApiModelProperty("Node Information")
    private List<InstanceInfo> nodeList;
}
