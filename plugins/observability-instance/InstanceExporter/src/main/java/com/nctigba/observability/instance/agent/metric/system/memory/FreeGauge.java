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
 *  FreeGauge.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/InstanceExporter/
 *  src/main/java/com/nctigba/observability/instance/agent/metric/system/memory/FreeGauge.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.agent.metric.system.memory;

import com.nctigba.observability.instance.agent.exception.CMDException;
import com.nctigba.observability.instance.agent.exception.CollectException;
import com.nctigba.observability.instance.agent.metric.DBMetric;
import com.nctigba.observability.instance.agent.metric.MetricResult;
import com.nctigba.observability.instance.agent.metric.MetricType;
import com.nctigba.observability.instance.agent.model.dto.CollectParamDTO;
import com.nctigba.observability.instance.agent.model.dto.CollectTargetDTO;
import com.nctigba.observability.instance.agent.util.CmdUtils;
import com.nctigba.observability.instance.agent.util.StringUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * memory metrics use free command
 *
 * @since 2023/12/1
 */
@Service
@Slf4j
@Data
public class FreeGauge implements DBMetric {
    private MetricType type = MetricType.GAUGE;
    private String groupName = "agent_free";
    private String[] names = {
            "agent_free_Mem_total_bytes",
            "agent_free_Mem_used_bytes",
            "agent_free_Mem_free_bytes",
            "agent_free_Mem_shared_bytes",
            "agent_free_Mem_cache_bytes",
            "agent_free_Mem_available_bytes",
            "agent_free_Swap_total_bytes",
            "agent_free_Swap_used_bytes",
            "agent_free_Swap_free_bytes"};
    private String[] helps = names;
    private String[] labelNames = {"host"};

    /**
     * @inheritDoc
     */
    @Override
    public List<List<MetricResult>> collectData(CollectTargetDTO target, CollectParamDTO param) {
        List<List<MetricResult>> result = new ArrayList<>();

        try {
            CmdUtils.readFromCmd(target.getTargetConfig().getNodeId(), "free", (index, line) -> {
                if (index == 0) {
                    // total used free shared buff/cache available
                    return;
                } else if (index == 1 || index == 2) {
                    // Mem: 15896504 9520972 496816 1156408 5878716 4745916
                    // Swap: 8278012 209664 8068348
                    String[] part = StringUtils.splitByBlank(line);
                    for (int i = 1; i < part.length; i++) {
                        List<MetricResult> result1 = new ArrayList<>();
                        String[] labelValues = {target.getTargetConfig().getHostId()};
                        result1.add(new MetricResult(labelValues, Double.parseDouble(part[i]) * 1024));
                        result.add(result1);
                    }
                } else {
                    return;
                }
            });
        } catch (IOException | CMDException e) {
            e.printStackTrace();
            throw new CollectException(this, e);
        }
        return result;
    }
}