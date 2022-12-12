package com.nctigba.observability.instance.model.monitoring;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class MonitoringParam {
    @ApiModelProperty(value = "dbId")
    private String id;

    @ApiModelProperty(value = "The table indicating parameters of the query need to be passed in by the front end in a uniform format")
    private String query;

    @ApiModelProperty(value = "start time")
    private String start;

    @ApiModelProperty(value = "end time")
    private String end;

    @ApiModelProperty(value = "step")
    private String step;

    @ApiModelProperty(value = "Specified time")
    private String time;

    @ApiModelProperty(value = "name of data")
    private String legendName;

    @ApiModelProperty(value = "sort field")
    private String field;

    @ApiModelProperty(value = "De duplication field")
    private String filter;

    @ApiModelProperty(value = "Positive sequence: asc, reverse sequence: desc")
    private String order;

    @ApiModelProperty(value = "Return data format, table: table, polyline: line")
    private String type;

    @ApiModelProperty(value = "Monitor Type")
    private String monitoringType;
}
