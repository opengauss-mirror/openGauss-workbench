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
 *  SshConnectionGauge.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/InstanceExporter/
 *  src/main/java/com/nctigba/observability/instance/agent/metric/system/ssh/SshConnectionGauge.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.agent.metric.system.ssh;

import com.nctigba.observability.instance.agent.exception.CollectException;
import com.nctigba.observability.instance.agent.metric.MetricResult;
import com.nctigba.observability.instance.agent.metric.MetricType;
import com.nctigba.observability.instance.agent.metric.OSMetric;
import com.nctigba.observability.instance.agent.model.dto.CollectParamDTO;
import com.nctigba.observability.instance.agent.model.dto.CollectTargetDTO;
import com.nctigba.observability.instance.agent.util.CmdUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * SshConnectionGauge
 *
 * @since 2023/11/20 10:17
 */
@Service
@Slf4j
@Data
public class SshConnectionGauge implements OSMetric {
    private MetricType type = MetricType.GAUGE;
    private String[] names = {"agent_host_conn_status"};
    private String[] helps = {"The connection of the host"};
    private String groupName = "agent_host_conn_status";
    private String[] labelNames = {"host"};

    /**
     * @inheritDoc
     */
    @Override
    public List<List<MetricResult>> collectData(
            CollectTargetDTO target, CollectParamDTO param) throws CollectException {
        List<List<MetricResult>> result = new ArrayList<>();
        List<MetricResult> result1 = new ArrayList<>();
        result.add(result1);

        String[] labels = {target.getTargetConfig().getHostId()};
        boolean isConnected = CmdUtils.checkNodeCanBeConnected(target.getTargetConfig());
        if (isConnected) {
            result.get(0).add(new MetricResult(labels, 1));
        } else {
            result.get(0).add(new MetricResult(labels, 0));
        }
        return result;
    }
}
