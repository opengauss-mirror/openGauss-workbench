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

package org.opengauss.agent.event;

import lombok.Data;

import org.opengauss.admin.common.enums.agent.StoragePolicy;

import java.util.List;
import java.util.Map;

/**
 * BaseEvent
 *
 * @author: wangchao
 * @Date: 2025/3/13 09:49
 * @Description: MemoryMetricEvent
 * @since 7.0.0
 **/
@Data
public class BaseEvent {
    /**
     * event id
     */
    protected Long eventId;

    /**
     * task id
     */
    protected Long taskId;

    /**
     * agent id
     */
    protected Long agentId;

    /**
     * cluster node id
     */
    protected String clusterNodeId;

    /**
     * keep period
     */
    protected String keepPeriod;

    /**
     * collector metrics
     */
    protected List<String> collectorMetrics;

    /**
     * property
     */
    protected Map<String, String> property;

    /**
     * storage policy
     */
    protected StoragePolicy storagePolicy;
}
