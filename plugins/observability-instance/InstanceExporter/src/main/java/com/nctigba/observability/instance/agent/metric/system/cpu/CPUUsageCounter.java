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
 *  CPUUsageCounter.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/InstanceExporter/
 *  src/main/java/com/nctigba/observability/instance/agent/metric/system/cpu/CPUUsageCounter.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.agent.metric.system.cpu;

import com.nctigba.observability.instance.agent.exception.CMDException;
import com.nctigba.observability.instance.agent.exception.CollectException;
import com.nctigba.observability.instance.agent.metric.MetricResult;
import com.nctigba.observability.instance.agent.metric.MetricType;
import com.nctigba.observability.instance.agent.metric.OSMetric;
import com.nctigba.observability.instance.agent.model.dto.CollectParamDTO;
import com.nctigba.observability.instance.agent.model.dto.CollectTargetDTO;
import com.nctigba.observability.instance.agent.service.MetricCollectManagerService;
import com.nctigba.observability.instance.agent.util.FileUtils;
import com.nctigba.observability.instance.agent.util.StringUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Linux file <code>/proc/stat</code>
 *
 * @since 2023/12/1
 */
@Service
@Slf4j
@Data
public class CPUUsageCounter implements OSMetric {
    private static final String[] CPU_STAT = {"user", "nice", "system", "idle", "iowait", "irq", "softirq", "steal"};

    @Autowired
    MetricCollectManagerService metricCollectManager;

    private MetricType type = MetricType.COUNTER;
    private String groupName = "agent_cpu_seconds_total";
    private String[] names = {"agent_cpu_seconds_total"};
    private String[] helps = {"Size of the cache in Bytes."};
    private String[] labelNames = {"host", "cpu", "mode"};

    /**
     * @inheritDoc
     */
    @Override
    public List<List<MetricResult>> collectData(CollectTargetDTO target, CollectParamDTO param) {
        List<List<MetricResult>> result = new ArrayList<>();

        List<MetricResult> result1 = new ArrayList<>();
        result.add(result1);
        try {
            FileUtils.readFileLine(target.getTargetConfig().getNodeId(), "/proc/stat", s -> {
                var part = StringUtils.splitByBlank(s);
                if (part[0].equalsIgnoreCase("cpu")) {
                    return;
                }
                if (part[0].startsWith("cpu")) {
                    String cpuIndex = part[0].replaceAll("cpu", "");
                    for (String stat : CPU_STAT) {
                        int index = Arrays.asList(CPU_STAT).indexOf(stat) + 1;
                        if (index < part.length) {
                            var value = Double.parseDouble(part[index]) / 100;
                            String[] labels = {target.getTargetConfig().getHostId(), cpuIndex, stat};
                            result1.add(new MetricResult(labels, value));
                        }
                    }
                }
            });
        } catch (IOException | CMDException e) {
            e.printStackTrace();
            throw new CollectException(this, e);
        }

        return result;
    }
}