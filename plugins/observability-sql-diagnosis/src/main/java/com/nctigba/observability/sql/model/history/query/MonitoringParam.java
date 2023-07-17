/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.model.history.query;

import lombok.Data;
import lombok.Generated;

/**
 * MonitoringParam
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Data
@Generated
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
