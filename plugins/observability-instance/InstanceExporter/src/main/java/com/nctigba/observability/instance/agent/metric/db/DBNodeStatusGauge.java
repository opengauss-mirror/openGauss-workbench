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
 *  DBNodeStatusGauge.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/InstanceExporter/
 *  src/main/java/com/nctigba/observability/instance/agent/metric/db/DBNodeStatusGauge.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.agent.metric.db;

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
 * DBRunningStatusGauge
 *
 * @since 2023/11/19 23:51
 */
@Service
@Slf4j
@Data
public class DBNodeStatusGauge implements DBMetric {
    private static final String COMMAND = "gs_om -t status --detail | tail -n 3";
    private static final String NORMAL_STATUS = "Normal";

    private MetricType type = MetricType.GAUGE;
    private String groupName = "db_node_status";
    private String[] names = {"db_node_status"};
    private String[] helps = {"Database instance node status."};
    private String[] labelNames = {"host", "nodeIp", "status"};

    /**
     * @inheritDoc
     */
    @Override
    public List<List<MetricResult>> collectData(
            CollectTargetDTO target, CollectParamDTO param) throws CollectException {
        List<List<MetricResult>> result = new ArrayList<>();

        List<MetricResult> result1 = new ArrayList<>();
        result.add(result1);

        try {
            CmdUtils.readFromCmd(target.getTargetConfig().getNodeId(), COMMAND, line -> {
                String[] part = StringUtils.splitByBlank(line);
                if (!part[7].equals(NORMAL_STATUS)) {
                    result.get(0)
                            .add(new MetricResult(new String[]{target.getTargetConfig().getHostId(), part[2], part[7]},
                                    1));
                }
            });
        } catch (IOException | CMDException e) {
            throw new CollectException(this, e);
        }

        return result;
    }
}
