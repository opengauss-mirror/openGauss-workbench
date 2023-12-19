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
 *  VmstatGauge.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/InstanceExporter/
 *  src/main/java/com/nctigba/observability/instance/agent/metric/system/memory/VmstatGauge.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.agent.metric.system.memory;

import com.nctigba.observability.instance.agent.exception.CollectException;
import com.nctigba.observability.instance.agent.metric.MetricResult;
import com.nctigba.observability.instance.agent.metric.MetricType;
import com.nctigba.observability.instance.agent.metric.OSMetric;
import com.nctigba.observability.instance.agent.model.dto.CollectParamDTO;
import com.nctigba.observability.instance.agent.model.dto.CollectTargetDTO;
import com.nctigba.observability.instance.agent.service.MetricCollectManagerService;
import com.nctigba.observability.instance.agent.util.CmdUtils;
import com.nctigba.observability.instance.agent.util.StringUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * <a href="https://zhuanlan.zhihu.com/p/364383421">zhuanlan.zhihu.com/p/364383421</a>
 *
 * @since 2023/12/1
 */
@Service
@Slf4j
@Data
public class VmstatGauge implements OSMetric {
    private static final String[] KEYS = new String[]{
            "r",
            "b",
            "swpd",
            "free",
            "buff",
            "cache",
            "si",
            "so",
            "bi",
            "bo",
            "in",
            "cs",
            "us",
            "sy",
            "wa",
            "st"
    };

    private MetricType type = MetricType.GAUGE;
    private String groupName = "agent_vmstat";
    private String[] labelNames = {"host"};

    @Autowired
    private MetricCollectManagerService metricCollectManager;

    /**
     * @inheritDoc
     */
    @Override
    public String[] getNames() {
        List<String> names = new ArrayList<>();
        for (int i = 0; i < KEYS.length; i++) {
            names.add(groupName + "_" + KEYS[i]);
        }
        return names.toArray(new String[0]);
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getHelps() {
        List<String> helps = new ArrayList<>();
        for (int i = 0; i < KEYS.length; i++) {
            helps.add("Vmstat tool data");
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
            CmdUtils.readFromCmd(target.getTargetConfig().getNodeId(), "vmstat", (index, line) -> {
                if (index < 2) {
                    return;
                }
                var part = StringUtils.splitByBlank(line);
                for (int i = 0; i < part.length && i < KEYS.length; i++) {
                    var value = Double.valueOf(part[i]);
                    String[] labels = {target.getTargetConfig().getHostId()};
                    List<MetricResult> resultItem = new ArrayList<>();
                    resultItem.add(new MetricResult(labels, value));
                    result.add(resultItem);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            throw new CollectException(this, e);
        }
        return result;
    }
}