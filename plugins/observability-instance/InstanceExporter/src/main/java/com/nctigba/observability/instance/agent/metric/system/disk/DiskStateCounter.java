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
 *  DiskStateCounter.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/InstanceExporter/
 *  src/main/java/com/nctigba/observability/instance/agent/metric/system/disk/DiskStateCounter.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.agent.metric.system.disk;

import cn.hutool.core.util.ReUtil;
import com.nctigba.observability.instance.agent.exception.CollectException;
import com.nctigba.observability.instance.agent.metric.MetricResult;
import com.nctigba.observability.instance.agent.metric.MetricType;
import com.nctigba.observability.instance.agent.metric.OSMetric;
import com.nctigba.observability.instance.agent.model.dto.CollectParamDTO;
import com.nctigba.observability.instance.agent.model.dto.CollectTargetDTO;
import com.nctigba.observability.instance.agent.service.MetricCollectManagerService;
import com.nctigba.observability.instance.agent.util.FileUtils;
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
 * Linux 2.6 file {@code /proc/diskstats}<br/>
 * Linux 2.4 file {@code /proc/partitions} TODO
 *
 * @see <a href=
 * "https://www.kernel.org/doc/Documentation/ABI/testing/procfs-diskstats">
 * kernel.org/doc/procfs-diskstats</a>
 * @see <a href=
 * "https://blog.csdn.net/qu1993/article/details/105702991">blog.csdn.net/qu1993/article/details/105702991</a>
 * @since 2023/12/1
 */
@Service
@Slf4j
@Data
public class DiskStateCounter implements OSMetric {
    private static final String IGNORE = "^(ram|loop|fd|(h|s|v|xv)d[a-z]|nvme\\d+n\\d+p)\\d+$";
    private static final String[] KEYS = new String[]{
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
            "time spent flushing"
    };

    @Autowired
    MetricCollectManagerService metricCollectManager;

    private MetricType type = MetricType.COUNTER;
    private String groupName = "agent_disk";
    private String[] labelNames = {"host", "major", "minor", "device"};

    /**
     * @inheritDoc
     */
    @Override
    public String[] getNames() {
        List<String> names = new ArrayList<>();
        for (int i = 3; i < KEYS.length; i++) {
            names.add((groupName + "_" + KEYS[i]).replaceAll(" ", "_"));
        }
        return names.toArray(new String[0]);
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getHelps() {
        List<String> helps = new ArrayList<>();
        for (int i = 3; i < KEYS.length; i++) {
            helps.add("Disk_ information field " + KEYS[i]);
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
            FileUtils.readFileLine(target.getTargetConfig().getNodeId(), "/proc/diskstats", line -> {
                var part = StringUtils.splitByBlank(line);
                if (ReUtil.contains(IGNORE, part[2])) {
                    return;
                }
                if (result.isEmpty()) {
                    for (int i = 3; i < KEYS.length; i++) {
                        result.add(new ArrayList<>());
                    }
                }
                String[] labels = {target.getTargetConfig().getHostId(), part[0], part[1], part[2]};
                for (int i = 3; i < KEYS.length; i++) {
                    Double value = i < part.length ? Double.valueOf(part[i]) : 0;
                    result.get(i - 3).add(new MetricResult(labels, value));
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            throw new CollectException(this, e);
        }

        return result;
    }
}