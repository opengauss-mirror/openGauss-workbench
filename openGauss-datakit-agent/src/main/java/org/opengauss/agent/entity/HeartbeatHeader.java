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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * HeartbeatReport
 *
 * @author: wangchao
 * @Date: 2025/2/27 09:07
 * @Description: HeartbeatReport
 * @since 7.0.0-Rc2
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HeartbeatHeader {
    private String agentName;
    private String instanceId;
    private String agentAddress;
    private String target;

    /**
     * Convert to heartbeat header string
     *
     * @return heartbeat header string
     */
    public String toHeartbeatHeader() {
        return agentName + ":" + agentAddress + " : " + instanceId + "->" + target;
    }
}
