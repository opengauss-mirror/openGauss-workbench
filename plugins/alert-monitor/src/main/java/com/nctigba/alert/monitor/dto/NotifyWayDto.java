/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.dto;

import com.nctigba.alert.monitor.entity.NotifyWay;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author wuyuebin
 * @date 2023/6/1 00:55
 * @description
 */
@Data
@Accessors(chain = true)
public class NotifyWayDto extends NotifyWay {
    private String notifyTemplateName;
}
