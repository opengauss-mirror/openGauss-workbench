/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package org.opengauss.plugin.agent.metric.system.disk;

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
import io.prometheus.client.Collector.Type;

/**
 * Linux 2.6 file {@code /proc/diskstats}<br/>
 * Linux 2.4 file {@code /proc/partitions} TODO
 * 
 * @see <a href=
 *      "https://www.kernel.org/doc/Documentation/ABI/testing/procfs-diskstats">
 *      kernel.org/doc/procfs-diskstats</a>
 * @see <a href=
 *      "https://blog.csdn.net/qu1993/article/details/105702991">blog.csdn.net/qu1993/article/details/105702991</a>
 */
@Service("disk_")
public class DiskStats implements OSmetric {
    private static final String[] KEYS = {
            "major number",
            "minor mumber",
            "device name",
            "rd_ios", // reads completed
            "rd_merges", // reads merged
            // https://unix.stackexchange.com/questions/111968/how-to-get-total-disk-read-write-in-bytes-per-hdd-device-from-proc
            "rd_sectors", // sectors read
            "rd_ticks", // time spent reading(ms)
            "wr_ios", // writes completed
            "wr_merges", // writes merged
            "wr_sectors", // sectors written
            "wr_ticks", // time spent writing(ms)
            "io_pgr", // io_now
            // io_ticks, https://zhuanlan.zhihu.com/p/604547780
            "tot_ticks", // io_time_ms
            "rq_ticks", // weighted time spent doing IOs(ms)
            // Kernel 4.18+
            "discards completed successfully",
            "discards merged",
            "discarded_sectors",
            "time spent discarding",
            // Kernel 5.5+
            "flush requests completed successfully",
            "time spent flushing", };

    private static final String IGNORE = "^(ram|loop|fd|(h|s|v|xv)d[a-z]|nvme\\d+n\\d+p)\\d+$";

    @Override
    public Map<String, Metric> getMetric(Integer dbPort) throws FileNotFoundException, IOException {
        Map<String, Metric> map = new HashMap<>();
        FileUtil.readFileLine("/proc/diskstats", line -> {
            var part = StringUtil.splitByBlank(line);
            if (ReUtil.contains(IGNORE, part[2]))
                return;
            if (map.isEmpty()) {
                var labelNames = Arrays.asList("major", "minor", "device");
                for (int i = 3; i < KEYS.length; i++) {
                    map.put(KEYS[i], new Metric(Type.COUNTER, labelNames));
                }
            }
            var labelValues = Arrays.asList(part[0], part[1], part[2]);
            for (int i = 3; i < KEYS.length; i++) {
                map.get(KEYS[i]).addValue(labelValues, i < part.length ? part[i] : null);
            }
        });
        return map;
    }
}