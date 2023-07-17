/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.model;

import lombok.Data;
import lombok.Generated;
import lombok.experimental.Accessors;
import com.nctigba.alert.monitor.entity.NotifyConfig;

/**
 * @author wuyuebin
 * @date 2023/6/2 03:09
 * @description
 */
@Data
@Generated
@Accessors(chain = true)
public class NotifyConfigReq extends NotifyConfig {
    private Long notifyWayId;
}
