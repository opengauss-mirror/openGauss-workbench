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
 *  NetStatUnknown.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/InstanceExporter/
 *  src/main/java/com/nctigba/observability/instance/agent/metric/system/network/NetStatUnknown.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.agent.metric.system.network;

import cn.hutool.core.util.NumberUtil;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Linux file /proc/net/dev
 * <p>
 * <strong>bytes</strong> The total number of bytes of data transmitted or
 * received by the interface.
 * </p>
 * <p>
 * <strong>packets</strong> The total number of packets of data transmitted or
 * received by the interface.
 * </p>
 * <p>
 * <strong>errs</strong> The total number of transmit or receive errors detected
 * by the device driver.
 * </p>
 * <p>
 * <strong>drop</strong> The total number of packets dropped by the device
 * driver.
 * </p>
 * <p>
 * <strong>fifo</strong> The number of FIFO buffer errors.
 * </p>
 * <p>
 * <strong>frame</strong> The number of packet framing errors.
 * </p>
 * <p>
 * <strong>colls</strong> The number of collisions detected on the interface.
 * </p>
 * <p>
 * <strong>compressed</strong> The number of compressed packets transmitted or
 * received by the device driver. (This appears to be unused in the 2.2.15
 * kernel.)
 * </p>
 * <p>
 * <strong>carrier</strong> The number of carrier losses detected by the device
 * driver.
 * </p>
 * <p>
 * <strong>multicast</strong> The number of multicast frames transmitted or
 * received by the device driver.
 * </p>
 *
 * @see <a href=
 * "https://git.kernel.org/pub/scm/linux/kernel/git/stable/linux.git/tree/net/core/net-procfs.c">git.kernel.org/pub/scm/linux/kernel/git/stable/linux.git/tree/net/core/net-procfs.c</a>
 * @since 2023/12/1
 */
@Service
@Slf4j
@Data
public class NetStatUnknown implements DBMetric {
    private MetricType type = MetricType.GAUGE;
    private String groupName = "agent_netstat";
    private String[] names = {
            "agent_netstat_Tcp_CurrEstab",
            "agent_netstat_Tcp_InSegs",
            "agent_netstat_Tcp_OutSegs"};
    private String[] helps = names;
    private String[] labelNames = {"host"};

    /**
     * @inheritDoc
     */
    @Override
    public List<List<MetricResult>> collectData(CollectTargetDTO target, CollectParamDTO param) {
        List<List<MetricResult>> result = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            result.add(new ArrayList<>());
        }

        try {
            Map<String, Map<String, Long>> snmp = getNetStats(target.getTargetConfig().getNodeId(), "/proc/net/snmp");
            log.debug("agent_netstat netStats:{}", snmp);

            snmp.forEach((key, map) -> {
                map.forEach((name, value) -> {
                    String metricName = groupName + "_" + key + "_" + name;
                    int index = Arrays.asList(getNames()).indexOf(metricName);
                    if (index >= 0) {
                        String[] labelValues = {target.getTargetConfig().getHostId()};
                        result.get(index).add(new MetricResult(labelValues, value));
                    }
                });
            });
        } catch (IOException e) {
            e.printStackTrace();
            throw new CollectException(this, e);
        }
        return result;
    }


    private Map<String, Map<String, Long>> getNetStats(
            String nodeId, String fileName) throws IOException {
        Map<String, Map<String, Long>> values = new HashMap<String, Map<String, Long>>();
        List<String> keys = new ArrayList<String>();
        FileUtils.readFileLine(nodeId, fileName, (i, line) -> {
            log.debug("agent_netstat line:{}", line);
            String[] part = StringUtils.splitByBlank(line);
            if (keys.isEmpty()) {
                Collections.addAll(keys, part);
                return;
            }
            try {
                Map map = new HashMap<String, Long>();
                for (int j = 1; j < part.length; j++) {
                    map.put(keys.get(j), NumberUtil.parseLong(part[j]));
                }
                String key = keys.get(0);
                values.put(key.substring(0, key.length() - 1), map);
            } finally {
                keys.clear();
            }
        });
        return values;
    }

    private Map<String, Map<String, Long>> getSNMP6Stats(
            String nodeId, String fileName) throws IOException {
        Map<String, Map<String, Long>> values = new HashMap<String, Map<String, Long>>();
        FileUtils.readFileLine(nodeId, fileName, line -> {
            // Expect to have "6" in metric name, skip line otherwise
            if (!line.contains("6")) {
                return;
            }
            String[] part = StringUtils.splitByBlank(line);
            if (part.length < 2) {
                return;
            }
            String[] keys = part[0].split("6");
            String protocol = keys[0] + 6;
            if (!values.containsKey(protocol)) {
                values.put(protocol, new HashMap<String, Long>());
            }
            values.get(protocol).put(keys[1], NumberUtil.parseLong(part[1]));
        });
        return values;
    }
}