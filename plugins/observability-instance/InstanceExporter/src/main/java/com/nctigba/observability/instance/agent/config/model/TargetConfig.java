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
 *  TargetConfig.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/InstanceExporter/
 *  src/main/java/com/nctigba/observability/instance/agent/config/model/TargetConfig.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.agent.config.model;

import com.nctigba.observability.instance.agent.enums.DbTypeEnum;
import lombok.Data;

/**
 * Target config from yml file
 *
 * @since 2023/12/1
 */
@Data
public class TargetConfig {
    private String nodeId;
    private String hostId;
    private String exporterPort;
    private String machineIP;
    private String machinePort;
    private String machineUser;
    private String machinePassword;
    private String dbIp;
    private String dbPort;
    private String dbDatabase;
    private String dbUserName;
    private String dbUserPassword;
    private String dbType = DbTypeEnum.OPENGAUSS.name();
}