/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.alertcenter.model;

import lombok.Data;

/**
 * @author wuyuebin
 * @date 2023/4/14 14:56
 * @description
 */
@Data
public class AlertStatisticsReq {
    private String clusterNodeId;

    /**
     * When processing a GET request, the platform is unable to convert the string ("yyyy-MM-dd HH:mm:ss")
     * parameter to a date.
     */
    private String startTime; // yyyy-MM-dd HH:mm:ss

    private String endTime; // yyyy-MM-dd HH:mm:ss
}
