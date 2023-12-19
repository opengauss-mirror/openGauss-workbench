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
 *  SocketStateGauge.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/InstanceExporter/
 *  src/main/java/com/nctigba/observability/instance/agent/metric/system/network/SocketStateGauge.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.agent.metric.system.network;

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

/**
 * TCP alloc metrics
 *
 * @since 2023/12/1
 */
@Service
@Slf4j
@Data
public class SocketStateGauge implements DBMetric {
    private static final String SPLIT = ",";

    private MetricType type = MetricType.GAUGE;
    private String groupName = "agent_sockstat";
    private String[] names = new String[]{"agent_sockstat_TCP_alloc"};
    private String[] helps = names;
    private String[] labelNames = {"host"};

    /**
     * @inheritDoc
     */
    @Override
    public List<List<MetricResult>> collectData(CollectTargetDTO target, CollectParamDTO param) {
        List<List<MetricResult>> result = new ArrayList<>();
        List<MetricResult> resultItem = new ArrayList<>();
        result.add(resultItem);

        try {
            FileUtils.readFileLine(target.getTargetConfig().getNodeId(), "/proc/net/sockstat", line -> {
                String[] part = StringUtils.splitByBlank(line);
                String key = StringUtils.replaceParenthesis(part[0]) + "_";
                for (int i = 1; i * 2 <= part.length; i++) {
                    String keyPart = groupName + "_" + key + part[2 * i - 1];
                    if (keyPart.equals(names[0])) {
                        String[] labelValues = {target.getTargetConfig().getHostId()};
                        resultItem.add(new MetricResult(labelValues, Double.valueOf(part[2 * i])));
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            throw new CollectException(this, e);
        }
        return result;
    }
}