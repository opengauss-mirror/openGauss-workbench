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
     * 平台处理get请求时，无法将string（yyyy-MM-dd HH:mm:ss）参数转化为日期
     */
    private String startTime; // yyyy-MM-dd HH:mm:ss

    private String endTime; // yyyy-MM-dd HH:mm:ss
}
