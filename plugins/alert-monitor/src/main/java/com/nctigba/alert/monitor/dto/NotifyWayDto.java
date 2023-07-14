/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.dto;

import lombok.Data;
import lombok.Generated;
import lombok.experimental.Accessors;
import com.nctigba.alert.monitor.entity.NotifyWay;

/**
 * @author wuyuebin
 * @date 2023/6/1 00:55
 * @description
 */
@Data
@Generated
@Accessors(chain = true)
public class NotifyWayDto extends NotifyWay {
    private String notifyTemplateName;
}
