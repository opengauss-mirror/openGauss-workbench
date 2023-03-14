package com.nctigba.observability.instance.model.monitoring;

import lombok.Data;

@Data
public class MonitoringParam {
    private String id;

    private String query;

    private String start;

    private String end;

    private String step;

    private String time;

    private String legendName;

    private String field;

    private String filter;

    private String order;

    private String type;

    private String monitoringType;
}
