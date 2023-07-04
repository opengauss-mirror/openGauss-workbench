/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.model.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author wuyuebin
 * @date 2023/4/30 01:56
 * @description Prometheus pushes alert messages.
 */
@Data
public class AlertApiReq {
    private AlertLabels labels;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startsAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endsAt;

    private Map annotations;

    private String generatorURL;
}
