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
 *  NetDevCounter.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/InstanceExporter/
 *  src/main/java/com/nctigba/observability/instance/agent/metric/system/network/NetDevCounter.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.agent.metric.system.network;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
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
 * "https://git.kernel.org/pub/scm/linux/kernel/git/stable/linux.git/tree/net/core/net-procfs.c">
 * git.kernel.org/pub/scm/linux/kernel/git/stable/linux.git/tree/net/core/net-procfs.c</a>
 * @since 2023/12/1
 */
@Service
@Slf4j
@Data
public class NetDevCounter implements DBMetric {
    private static final String IGNORE = "";

    private MetricType type = MetricType.COUNTER;
    private String groupName = "agent_network";
    private String[] names = new String[]{
            "agent_network_receive_bytes",
            "agent_network_receive_packets",
            "agent_network_receive_errors",
            "agent_network_receive_dropped",
            "agent_network_receive_fifo",
            "agent_network_receive_frame",
            "agent_network_receive_compressed",
            "agent_network_receive_multicast",
            "agent_network_transmit_bytes",
            "agent_network_transmit_packets",
            "agent_network_transmit_errors",
            "agent_network_transmit_dropped",
            "agent_network_transmit_fifo",
            "agent_network_transmit_colls",
            "agent_network_transmit_carrier",
            "agent_network_transmit_compressed"};
    private String[] helps = names;
    private String[] labelNames = {"host", "device"};

    /**
     * @inheritDoc
     */
    @Override
    public List<List<MetricResult>> collectData(CollectTargetDTO target, CollectParamDTO param) {
        List<List<MetricResult>> result = new ArrayList<>();

        try {
            FileUtils.readFileLine(target.getTargetConfig().getNodeId(), "/proc/net/dev", (index, line) -> {
                if (line.contains("|")) {
                    return;
                }
                String[] part = StringUtils.splitByBlank(line);
                if (StrUtil.isNotBlank(IGNORE) && ReUtil.contains(IGNORE, part[0])) {
                    return;
                }
                if (result.isEmpty()) {
                    for (int i = 0; i < names.length; i++) {
                        result.add(new ArrayList<>());
                    }
                }
                String[] labelValues = {target.getTargetConfig().getHostId(),
                        StringUtils.removeLast(part[0])};
                for (int i = 0; i < names.length; i++) {
                    result.get(i).add(new MetricResult(
                            labelValues,
                            i < part.length ? Double.valueOf(part[i + 1]) : 0));
                }
            });
        } catch (IOException | CMDException e) {
            e.printStackTrace();
            throw new CollectException(this, e);
        }
        return result;
    }
}