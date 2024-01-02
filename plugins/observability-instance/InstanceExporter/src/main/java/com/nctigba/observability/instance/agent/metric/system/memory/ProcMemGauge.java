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
 *  ProcMemGauge.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/InstanceExporter/
 *  src/main/java/com/nctigba/observability/instance/agent/metric/system/memory/ProcMemGauge.java
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
import com.nctigba.observability.instance.agent.util.FileUtils;
import com.nctigba.observability.instance.agent.util.StringUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Linux file <code>/proc/meminfo</code>
 *
 * @since 2023/12/1
 */
@Service
@Slf4j
@Data
public class ProcMemGauge implements DBMetric {
    private MetricType type = MetricType.GAUGE;
    private String groupName = "agent_memory";
    private String[] names = {"agent_memory_MemAvailable_bytes", "agent_memory_MemTotal_bytes"};
    private String[] helps = names;
    private String[] labelNames = {"host"};

    /**
     * @inheritDoc
     */
    @Override
    public List<List<MetricResult>> collectData(CollectTargetDTO target, CollectParamDTO param) {
        List<List<MetricResult>> result = new ArrayList<>();

        for (int i = 0; i < names.length; i++) {
            List<MetricResult> resultTemp = new ArrayList<>();
            result.add(resultTemp);
        }

        try {
            FileUtils.readFileLine(target.getTargetConfig().getNodeId(), "/proc/meminfo", line -> {
                String[] part = StringUtils.splitByBlank(line);
                double value = Double.parseDouble(part[1]);
                String key = StringUtils.replaceParenthesis(line);
                if (key.equals("MemAvailable") || key.equals("MemTotal")) {
                    String buildKey = groupName + "_" + key + "_bytes";
                    int index = IntStream.range(0, names.length)
                            .filter(i -> names[i].equals(buildKey))
                            .findFirst()
                            .orElse(-1);
                    List<MetricResult> resultTemp = result.get(index);
                    String[] labelValues = {target.getTargetConfig().getHostId()};
                    resultTemp.add(new MetricResult(labelValues, value *= 1024));
                }
            });
        } catch (IOException | CMDException e) {
            e.printStackTrace();
            throw new CollectException(this, e);
        }
        return result;
    }
}