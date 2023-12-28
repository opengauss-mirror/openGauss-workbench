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
 *  SshClientConfig.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/InstanceExporter/
 *  src/main/java/com/nctigba/observability/instance/agent/pool/SshClientConfig.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.agent.pool;

import lombok.Data;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * SSH Client config entity
 *
 * @since 2023/12/28
 */
@Data
public class SshClientConfig {
    private String machineIP;
    private int machinePort;
    private String machineUser;
    private String machinePassword;
    private GenericObjectPoolConfig objectPoolConfig;
}