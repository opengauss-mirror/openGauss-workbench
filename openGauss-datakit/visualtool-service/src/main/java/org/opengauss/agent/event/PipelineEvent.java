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
import lombok.EqualsAndHashCode;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * PipelineEvent
 *
 * @author: wangchao
 * @Date: 2025/3/13 09:49
 * @Description: PipelineEvent
 * @since 7.0.0-RC2
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class PipelineEvent extends BaseEvent {
    private List<Map<String, Object>> dataList;

    /**
     * pipeline event info
     *
     * @return String
     */
    public String info() {
        return "[eventId=" + eventId + ", taskId=" + taskId + ", agentId=" + agentId + ", clusterNodeId="
            + clusterNodeId + ", collectorMetrics=" + Optional.ofNullable(collectorMetrics)
            .orElse(Collections.emptyList())
            .size() + ", property=" + property.size() + ", storagePolicy=" + storagePolicy + ", dataList="
            + Optional.ofNullable(dataList).orElse(Collections.emptyList()).size() + "]";
    }

    /**
     * pipeline event detail
     *
     * @return String
     */
    public String detail() {
        return "[eventId=" + eventId + ", taskId=" + taskId + ", agentId=" + agentId + ", clusterNodeId="
            + clusterNodeId + ", collectorMetrics=" + Optional.ofNullable(collectorMetrics)
            .orElse(Collections.emptyList())
            .stream()
            .filter(Objects::nonNull)
            .collect(Collectors.joining("|")) + ", property=" + Optional.ofNullable(property)
            .orElse(Collections.emptyMap()) + ", storagePolicy=" + storagePolicy + ",\n metricData="
            + Optional.ofNullable(dataList)
            .orElse(Collections.emptyList())
            .stream()
            .map(m -> "[" + m.keySet().stream().map(k -> k + "=" + m.get(k)).collect(Collectors.joining(",")) + "]")
            .collect(Collectors.joining(";")) + "]";
    }
}
