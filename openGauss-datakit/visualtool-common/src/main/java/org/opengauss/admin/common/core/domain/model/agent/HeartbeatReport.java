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

package org.opengauss.admin.common.core.domain.model.agent;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.opengauss.admin.common.constant.AgentConstants;

import java.time.Instant;

/**
 * HeartbeatReport
 *
 * @author: wangchao
 * @Date: 2025/4/17 16:01
 * @Description: HeartbeatReport
 * @since 7.0.0-RC2
 **/
@Data
@NoArgsConstructor
public class HeartbeatReport {
    private String agentId;
    private Instant timestamps;
    private String status;
    private String additionalInfo;

    /**
     * Constructor
     *
     * @param agentId agent id
     * @param timestamps timestamp
     */
    public HeartbeatReport(String agentId, Instant timestamps) {
        this(agentId, timestamps, AgentConstants.Status.HEARTBEAT_STATUS_UP);
    }

    /**
     * Constructor
     *
     * @param agentId agent id
     * @param timestamps timestamp
     * @param status status
     */
    public HeartbeatReport(String agentId, Instant timestamps, String status) {
        this.agentId = agentId;
        this.timestamps = timestamps;
        this.status = status;
    }

    /**
     * Convert to heartbeat report string.
     *
     * @return heartbeat report string
     */
    public String toHeartbeat() {
        if (StrUtil.isEmpty(additionalInfo)) {
            return "send heartbeat report " + status + ", at " + timestamps;
        }
        return "send heartbeat report " + status + ", at " + timestamps + ", additionalInfo: " + additionalInfo;
    }
}
