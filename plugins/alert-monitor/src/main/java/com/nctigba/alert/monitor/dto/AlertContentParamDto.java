/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import com.nctigba.alert.monitor.config.annotation.AlertContentParam;

/**
 * @author wuyuebin
 * @date 2023/5/2 23:39
 * @description
 */
@Data
@Accessors(chain = true)
public class AlertContentParamDto {
    @AlertContentParam(name = "alertContentParam.nodeName",
        preVal = "test/127.0.0.1:5432(MASTER)",
        group = {"alert", "notify"})
    private String nodeName;
    @AlertContentParam(name = "alertContentParam.hostname",
        preVal = "centos",
        group = {"alert", "notify"})
    private String hostname;
    @AlertContentParam(name = "alertContentParam.hostIp",
        preVal = "127.0.0.1",
        group = {"alert", "notify"})
    private String hostIp;
    @AlertContentParam(name = "alertContentParam.port",
        preVal = "5432",
        group = {"alert", "notify"})
    private String port;
    @AlertContentParam(name = "alertContentParam.alertTime",
        preVal = "2023-01-01 00:00:00",
        group = {"alert", "notify"})
    private String alertTime;
    @AlertContentParam(name = "alertContentParam.alertStatus",
        preVal = "alerting",
        isI18nPreVal = true,
        group = {"alert", "notify"})
    private String alertStatus;
    @AlertContentParam(name = "alertContentParam.level",
        preVal = "serious",
        isI18nPreVal = true,
        group = {"alert", "notify"})
    private String level;
    @AlertContentParam(name = "alertContentParam.content",
        preVal = "The CPU usage rate is over 90%.",
        group = {"notify"})
    private String content;
}
