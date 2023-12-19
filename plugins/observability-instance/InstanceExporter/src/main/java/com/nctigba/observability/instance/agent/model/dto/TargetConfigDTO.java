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
 *  TargetConfigDTO.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/InstanceExporter/
 *  src/main/java/com/nctigba/observability/instance/agent/model/dto/TargetConfigDTO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.agent.model.dto;

import com.nctigba.observability.instance.agent.constant.CollectConstants;
import com.nctigba.observability.instance.agent.enums.DbTypeEnum;
import com.nctigba.observability.instance.agent.config.model.TargetConfig;
import lombok.Data;

/**
 * Target config for adding, editing configs
 *
 * @since 2023/12/1
 */
@Data
public class TargetConfigDTO {
    private String hostId;
    private String user;
    private String pass;
    private String nodeId;
    private Integer dbport;
    private String dbUsername;
    private String dbPassword;

    private String dbType = DbTypeEnum.OPENGAUSS.name();
    private String machineIP = CollectConstants.LOCAL_IP;
    private String machinePort = CollectConstants.SSH_PORT;
    private String dbIp = CollectConstants.LOCAL_IP;
    private String dbDataBase = CollectConstants.DEFAULT_DATABASE;


    /**
     * Convert DTO to Config entity
     *
     * @return TargetConfig
     * @since 2023/12/1
     */
    public TargetConfig toTargetConfig() {
        TargetConfig config = new TargetConfig();
        config.setNodeId(nodeId);
        config.setHostId(hostId);
        config.setMachineIP(machineIP);
        config.setMachinePort(machinePort);
        config.setMachineUser(user);
        config.setMachinePassword(pass);
        config.setDbIp(dbIp);
        config.setDbDatabase(dbDataBase);
        config.setDbPort(dbport.toString());
        config.setDbUserName(dbUsername);
        config.setDbUserPassword(dbPassword);
        config.setDbType(dbType);
        return config;
    }
}