/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * NotifySnmpDto
 *
 * @since 2023/8/18 17:31
 */
@Data
@Accessors(chain = true)
public class NotifySnmpDto {
    private String snmpIp;
    private String snmpPort;
    private String snmpCommunity;
    private String snmpOid;
    private Integer snmpVersion;
    private String snmpUsername;
    private String snmpAuthPasswd;
    private String snmpPrivPasswd;
}
