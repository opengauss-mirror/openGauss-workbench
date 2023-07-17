/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.model;

import lombok.Data;
import lombok.Generated;
import lombok.experimental.Accessors;

/**
 * @author wuyuebin
 * @date 2023/4/14 14:55
 * @description
 */
@Data
@Accessors(chain = true)
@Generated
public class AlertRecordReq {
    private String clusterNodeId;

    /**
     * When processing a GET request, the platform is unable to convert the string ("yyyy-MM-dd HH:mm:ss")
     * parameter to a date.
     */
    private String startTime; // yyyy-MM-dd HH:mm:ss
    private String endTime; // yyyy-MM-dd HH:mm:ss
    private String recordStatus;
    private String alertStatus;
    private String alertLevel;
}
