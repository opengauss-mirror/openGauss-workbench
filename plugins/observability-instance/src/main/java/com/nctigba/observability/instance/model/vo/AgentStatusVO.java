/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package com.nctigba.observability.instance.model.vo;

import cn.hutool.core.util.StrUtil;
import com.nctigba.observability.instance.constants.CommonConstants;
import com.nctigba.observability.instance.enums.AgentStatusEnum;
import com.nctigba.observability.instance.model.entity.NctigbaEnvDO;
import lombok.Data;

import java.util.Date;

/**
 * AgentStatusVO
 *
 * @since 2024/2/19 16:06
 */
@Data
public class AgentStatusVO {
    String id;
    String status;
    String errStatusMsg;
    Date updateTime;

    /**
     * get agent status
     *
     * @param env NctigbaEnvDO
     * @return AgentStatusVO
     */
    public static AgentStatusVO of(NctigbaEnvDO env) {
        AgentStatusVO agentStatus = new AgentStatusVO();
        agentStatus.setId(env.getId());
        agentStatus.setUpdateTime(env.getUpdateTime());
        String status0 = env.getStatus();
        if (StrUtil.isBlank(status0) || env.getUpdateTime() == null) {
            agentStatus.setStatus(AgentStatusEnum.UNKNOWN.getStatus());
            return agentStatus;
        }
        boolean isStop = AgentStatusEnum.MANUAL_STOP.getStatus().equals(status0);
        boolean isRunning =
            AgentStatusEnum.STARTING.getStatus().equals(status0) || AgentStatusEnum.STOPPING.getStatus().equals(
                status0);
        long updateTime0 = env.getUpdateTime().getTime();
        boolean isRunTimeout = isRunning
            && new Date().getTime() - updateTime0 > 3 * CommonConstants.MONITOR_CYCLE * 1000L;
        boolean isTimeout = new Date().getTime() - updateTime0 > 2 * CommonConstants.MONITOR_CYCLE * 1000L;
        if (StrUtil.isBlank(status0) || (!isStop && !isRunTimeout && isTimeout) || isRunTimeout) {
            agentStatus.setStatus(AgentStatusEnum.UNKNOWN.getStatus());
        } else {
            agentStatus.setStatus(status0);
            agentStatus.setErrStatusMsg(env.getErrStatusMsg());
        }
        return agentStatus;
    }
}
