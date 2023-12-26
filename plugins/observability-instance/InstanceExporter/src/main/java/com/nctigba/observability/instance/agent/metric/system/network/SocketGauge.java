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
 *  SocketGauge.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/InstanceExporter/
 *  src/main/java/com/nctigba/observability/instance/agent/metric/system/network/SocketGauge.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.agent.metric.system.network;

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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Metrics for tcp udp sockets
 *
 * @since 2023/12/1
 */
@Service
@Slf4j
@Data
public class SocketGauge implements DBMetric {
    private static final String SPLIT = ",";

    private MetricType type = MetricType.GAUGE;
    private String groupName = "agent_network_socket";
    private String[] names = new String[]{"agent_network_socket"};
    private String[] helps = names;
    private String[] labelNames = new String[]{"host", "proto", "state"};

    /**
     * @inheritDoc
     */
    @Override
    public List<List<MetricResult>> collectData(CollectTargetDTO target, CollectParamDTO param) {
        List<List<MetricResult>> result = new ArrayList<>();
        List<MetricResult> resultItem = new ArrayList<>();
        result.add(resultItem);

        try {
            CmdUtils.readFromCmd(target.getTargetConfig().getNodeId(), "netstat", (index, line) -> {
                log.debug("line:{}", line);
                if (index < 2) {
                    return;
                }
                String[] part = StringUtils.splitByBlank(line);
                if (part.length < 5) {
                    return;
                }
                String proto = part[0];
                if (proto.startsWith("tcp") || proto.startsWith("udp")) {
                    String[] labelValues = {target.getTargetConfig().getHostId(), proto, part[5]};

                    // existed same labelValues
                    Optional<MetricResult> match =
                            resultItem.stream().filter(z -> Arrays.equals(z.getLabelValues(), labelValues)).findFirst();
                    if (match.isPresent()) {
                        log.debug("set");
                        match.get().setValue(match.get().getValue() + 1);
                    } else {
                        log.debug("add");
                        resultItem.add(new MetricResult(labelValues, 1));
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