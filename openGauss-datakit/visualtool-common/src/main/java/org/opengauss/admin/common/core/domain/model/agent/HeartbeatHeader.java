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

import lombok.Data;

/**
 * HeartbeatHeader
 *
 * @author: wangchao
 * @Date: 2025/4/17 16:01
 * @Description: HeartbeatHeader
 * @since 7.0.0-RC2
 **/
@Data
public class HeartbeatHeader {
    private String agentName;
    private String instanceId;
    private String agentAddress;
    private String target;

    public HeartbeatHeader(String agentName, String instanceId, String agentAddress, String target) {
        this.agentName = agentName;
        this.instanceId = instanceId;
        this.agentAddress = agentAddress;
        this.target = target;
    }

    /**
     * Convert to heartbeat header string.
     *
     * @return heartbeat header string
     */
    public String toHeartbeatHeader() {
        return agentName + ":" + agentAddress + " : " + instanceId + "->" + target;
    }
}
