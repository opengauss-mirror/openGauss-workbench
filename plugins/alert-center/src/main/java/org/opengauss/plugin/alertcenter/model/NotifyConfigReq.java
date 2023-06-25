/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.alertcenter.model;

import lombok.Data;
import lombok.experimental.Accessors;
import org.opengauss.plugin.alertcenter.entity.NotifyConfig;

/**
 * @author wuyuebin
 * @date 2023/6/2 03:09
 * @description
 */
@Data
@Accessors(chain = true)
public class NotifyConfigReq extends NotifyConfig {
    private Long notifyWayId;
}
