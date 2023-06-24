/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package org.opengauss.plugin.agent.metric.system.network;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.opengauss.plugin.agent.metric.Metric;
import org.opengauss.plugin.agent.util.FileUtil;
import org.opengauss.plugin.agent.util.StringUtil;
import org.springframework.stereotype.Service;

import org.opengauss.plugin.agent.metric.OSmetric;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import io.prometheus.client.Collector.Type;

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
 *      "https://git.kernel.org/pub/scm/linux/kernel/git/stable/linux.git/tree/net/core/net-procfs.c">git.kernel.org/pub/scm/linux/kernel/git/stable/linux.git/tree/net/core/net-procfs.c</a>
 */
@Service("network_")
public class NetDev implements OSmetric {
    private static final String[] KEYS = {
            "device_name",
            "receive_bytes",
            "receive_packets",
            "receive_errors",
            "receive_dropped",
            "receive_fifo",
            "receive_frame",
            "receive_compressed",
            "receive_multicast",
            "transmit_bytes",
            "transmit_packets",
            "transmit_errors",
            "transmit_dropped",
            "transmit_fifo",
            "transmit_colls",
            "transmit_carrier",
            "transmit_compressed", };
    private static final String IGNORE = "";

    @Override
    public Map<String, Metric> getMetric(Integer dbPort) throws FileNotFoundException, IOException {
        Map<String, Metric> map = new HashMap<>();
        FileUtil.readFileLine("/proc/net/dev", (index, line) -> {
            if (line.contains("|"))
                return;
            var part = StringUtil.splitByBlank(line);
            if (StrUtil.isNotBlank(IGNORE) && ReUtil.contains(IGNORE, part[0]))
                return;
            if (map.isEmpty()) {
                var labelNames = Arrays.asList("device");
                for (int i = 1; i < KEYS.length; i++) {
                    map.put(KEYS[i], new Metric(Type.COUNTER, labelNames));
                }
            }
            var labelValues = Arrays.asList(StringUtil.removeLast(part[0]));
            for (int i = 1; i < KEYS.length; i++) {
                map.get(KEYS[i]).addValue(labelValues, i < part.length ? part[i] : null);
            }
        });
        return map;
    }
}