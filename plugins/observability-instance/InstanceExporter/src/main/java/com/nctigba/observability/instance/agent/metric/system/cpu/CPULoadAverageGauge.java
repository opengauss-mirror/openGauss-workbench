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
 *  CPULoadAverageGauge.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/InstanceExporter/
 *  src/main/java/com/nctigba/observability/instance/agent/metric/system/cpu/CPULoadAverageGauge.java
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
import com.nctigba.observability.instance.agent.util.FileUtils;
import com.nctigba.observability.instance.agent.util.StringUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * CPU average load gauge metric
 *
 * @since 2023/12/1
 */
@Service
@Slf4j
@Data
public class CPULoadAverageGauge implements OSMetric {
    private String[] keys = {"1", "5", "15"};
    private MetricType type = MetricType.GAUGE;
    private String groupName = "agent_load";
    private String[] labelNames = {"host"};

    /**
     * @inheritDoc
     */
    @Override
    public String[] getNames() {
        List<String> names = new ArrayList<>();
        for (int i = 0; i < keys.length; i++) {
            names.add(groupName + keys[i]);
        }
        return names.toArray(new String[0]);
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getHelps() {
        List<String> helps = new ArrayList<>();
        for (int i = 0; i < keys.length; i++) {
            helps.add(keys[i] + "m load average");
        }
        return helps.toArray(new String[0]);
    }

    /**
     * @inheritDoc
     */
    @Override
    public List<List<MetricResult>> collectData(CollectTargetDTO target, CollectParamDTO param) {
        List<List<MetricResult>> result = new ArrayList<>();
        try {
            FileUtils.readFileLine(target.getTargetConfig().getNodeId(), "/proc/loadavg", s -> {
                var part = StringUtils.splitByBlank(s);
                String[] labels = {target.getTargetConfig().getHostId()};

                List<MetricResult> resultItem = new ArrayList<>();
                resultItem.add(new MetricResult(labels, Double.valueOf(part[0])));
                result.add(resultItem);

                resultItem = new ArrayList<>();
                resultItem.add(new MetricResult(labels, Double.valueOf(part[1])));
                result.add(resultItem);

                resultItem = new ArrayList<>();
                resultItem.add(new MetricResult(labels, Double.valueOf(part[2])));
                result.add(resultItem);
            });
        } catch (IOException | CMDException e) {
            e.printStackTrace();
            throw new CollectException(this, e);
        }
        return result;
    }
}