/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.alertcenter.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import org.opengauss.plugin.alertcenter.entity.AlertRecord;

/**
 * @author wuyuebin
 * @date 2023/5/17 10:44
 * @description
 */
@Data
@Accessors(chain = true)
public class AlertRecordDto extends AlertRecord {
    private String clusterNodeName;
    private String clusterId;
    private String nodeRole;
    private String hostIpAndPort;
}
