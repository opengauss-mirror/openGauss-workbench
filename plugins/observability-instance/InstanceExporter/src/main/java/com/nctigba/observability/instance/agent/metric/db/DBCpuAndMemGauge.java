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
 *  DBCpuAndMemGauge.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/InstanceExporter/
 *  src/main/java/com/nctigba/observability/instance/agent/metric/db/DBCpuAndMemGauge.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.agent.metric.db;

import com.nctigba.observability.instance.agent.constant.CollectConstants;
import com.nctigba.observability.instance.agent.exception.CMDException;
import com.nctigba.observability.instance.agent.exception.CollectException;
import com.nctigba.observability.instance.agent.metric.DBMetric;
import com.nctigba.observability.instance.agent.metric.MetricResult;
import com.nctigba.observability.instance.agent.metric.MetricType;
import com.nctigba.observability.instance.agent.model.dto.CollectParamDTO;
import com.nctigba.observability.instance.agent.model.dto.CollectTargetDTO;
import com.nctigba.observability.instance.agent.service.MetricCollectManagerService;
import com.nctigba.observability.instance.agent.util.CmdUtils;
import com.nctigba.observability.instance.agent.util.DbUtils;
import com.nctigba.observability.instance.agent.util.StringUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Linux file <code>/proc/stat</code>
 *
 * @since 2023/12/1
 */
@Service
@Slf4j
@Data
public class DBCpuAndMemGauge implements DBMetric {
    private static final String COMMAND_PORT_TO_PID = "netstat -nap|grep :'|grep LISTEN";
    private static final String COMMAND_TOP_ = "top -b -n 1|grep ";

    @Autowired
    MetricCollectManagerService metricCollectManager;
    @Autowired
    DbUtils dbUtil;

    private MetricType type = MetricType.GAUGE;
    private String groupName = "top_db";
    private String[] names = {"top_db_cpu", "top_db_mem"};
    private String[] helps = {"Database process cpu usage.", "Database process memory usage."};
    private String[] labelNames = CollectConstants.DB_DEFAULT_METRIC_LABELS.toArray(new String[0]);

    /**
     * @inheritDoc
     */
    @Override
    public List<List<MetricResult>> collectData(CollectTargetDTO target, CollectParamDTO param) {
        List<List<MetricResult>> result = new ArrayList<>();

        List<MetricResult> result1 = new ArrayList<>();
        result.add(result1);
        List<MetricResult> result2 = new ArrayList<>();
        result.add(result2);

        String dbPort = target.getTargetConfig().getDbPort();
        Set<String> pids = new HashSet<>();
        try {
            CmdUtils.readFromCmd(target.getTargetConfig().getNodeId(), COMMAND_PORT_TO_PID.replaceAll("'", dbPort),
                    line -> {
                        String[] part = StringUtils.splitByBlank(line);
                        String pid = part[part.length - 1];
                        pid = pid.split("/")[0];
                        pids.add(pid);
                    });
        } catch (IOException | CMDException e) {
            e.printStackTrace();
            throw new CollectException(this, e);
        }
        if (pids.size() != 0) {
            try {
                CmdUtils.readFromCmd(target.getTargetConfig().getNodeId(), COMMAND_TOP_ + pids.iterator().next(),
                        line -> {
                            // PID USER PR NI VIRT RES SHR S %CPU %MEM TIME+ COMMAND
                            // 163616 omm 20 0 3133820 525420 171716 S 0.0 3.3 507:05.43 gaussdb
                            log.debug("top cpu result:{}", line);
                            String[] part = StringUtils.splitByBlank(line);
                            result1.add(new MetricResult(
                                    new String[]{target.getTargetConfig().getHostId(),
                                            target.getTargetConfig().getNodeId()},
                                    Double.valueOf(part[8])));
                            result2.add(new MetricResult(
                                    new String[]{target.getTargetConfig().getHostId(),
                                            target.getTargetConfig().getNodeId()},
                                    Double.valueOf(part[9])));
                        });
            } catch (IOException | CMDException e) {
                e.printStackTrace();
                throw new CollectException(this, e);
            }
        }

        return result;
    }
}