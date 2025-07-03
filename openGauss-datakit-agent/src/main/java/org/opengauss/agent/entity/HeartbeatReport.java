/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.agent.entity;

import static org.opengauss.agent.constant.AgentConstants.HEARTBEAT_STATUS_UP;

import com.cronutils.utils.StringUtils;

import lombok.Data;

import java.time.Instant;

/**
 * HeartbeatReport
 *
 * @author: wangchao
 * @Date: 2025/2/27 09:07
 * @Description: HeartbeatReport
 * @since 7.0.0-RC2
 **/
@Data
public class HeartbeatReport {
    private Instant timestamps;
    private String status;
    private Long agentId;
    private String additionalInfo;

    /**
     * Constructor
     *
     * @param agentId agent id
     * @param timestamps timestamps
     */
    public HeartbeatReport(Long agentId, Instant timestamps) {
        this.agentId = agentId;
        this.timestamps = timestamps;
        this.status = HEARTBEAT_STATUS_UP;
    }

    /**
     * Convert to heartbeat report
     *
     * @return heartbeat report
     */
    public String toHeartbeat() {
        if (StringUtils.isEmpty(additionalInfo)) {
            return "send heartbeat report " + status + ", at " + timestamps;
        }
        return "send heartbeat report " + status + ", at " + timestamps + ", additionalInfo: " + additionalInfo;
    }
}
