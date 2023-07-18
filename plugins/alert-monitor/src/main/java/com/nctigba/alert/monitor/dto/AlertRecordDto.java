/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.dto;

import com.nctigba.alert.monitor.entity.AlertRecord;
import lombok.Data;
import lombok.experimental.Accessors;

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
