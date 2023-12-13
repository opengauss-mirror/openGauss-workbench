/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  NotifySnmpDTO.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/model/dto/NotifySnmpDTO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * NotifySnmpDto
 *
 * @since 2023/8/18 17:31
 */
@Data
@Accessors(chain = true)
public class NotifySnmpDTO {
    private String snmpIp;
    private String snmpPort;
    private String snmpCommunity;
    private String snmpOid;
    private Integer snmpVersion;
    private String snmpUsername;
    private String snmpAuthPasswd;
    private String snmpPrivPasswd;
}
