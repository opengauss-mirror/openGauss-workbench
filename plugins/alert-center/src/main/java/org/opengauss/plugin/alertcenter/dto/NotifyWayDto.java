/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.alertcenter.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import org.opengauss.plugin.alertcenter.entity.NotifyWay;

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
