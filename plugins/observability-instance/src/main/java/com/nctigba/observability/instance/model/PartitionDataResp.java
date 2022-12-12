package com.nctigba.observability.instance.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class PartitionDataResp {

    @ApiModelProperty("Partition policy")
    String partStrategy;
    @ApiModelProperty("Partition key")
    String partKey;
    @ApiModelProperty("Number of data pages")
    String relPages;
    @ApiModelProperty("Tuple number")
    String relTuples;
    @ApiModelProperty("Number of visible data pages")
    String relallVisible;
    @ApiModelProperty("Interval value")
    String interval;
}
