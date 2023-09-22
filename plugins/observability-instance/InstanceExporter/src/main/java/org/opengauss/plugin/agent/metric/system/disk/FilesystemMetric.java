/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.agent.metric.system.disk;

import io.prometheus.client.Collector;
import org.opengauss.plugin.agent.metric.Metric;
import org.opengauss.plugin.agent.metric.OSmetric;
import org.opengauss.plugin.agent.util.CmdUtil;
import org.opengauss.plugin.agent.util.StringUtil;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * collect the filesystem info
 *
 * @since 2023/9/15 17:17
 */
@Service("filesystem_")
public class FilesystemMetric implements OSmetric {
    private static final String[] KEYS = {"size_kbytes", "used_size_kbytes", "free_size_kbytes"};
    private static final String FILESYSTEM_COMMAND = "df | tail -n +2";

    /**
     * collect the filesystem data
     *
     * @param dbPort port
     * @return map: the filesystem data
     * @throws FileNotFoundException FileNotFoundException
     * @throws IOException IOException
     */
    @Override
    public Map<String, Metric> getMetric(Integer dbPort) throws FileNotFoundException, IOException {
        Map<String, Metric> map = new HashMap<>();
        var labelNames = Arrays.asList("device", "mount");
        for (String key : KEYS) {
            map.put(key, new Metric(Collector.Type.GAUGE, labelNames));
        }
        CmdUtil.readFromCmd(FILESYSTEM_COMMAND, line -> {
            var part = StringUtil.splitByBlank(line);
            map.get(KEYS[0]).addValue(Arrays.asList(part[0], part[5]),
                Double.valueOf(part[1]));
            map.get(KEYS[1]).addValue(Arrays.asList(part[0], part[5]),
                Double.valueOf(part[2]));
            map.get(KEYS[2]).addValue(Arrays.asList(part[0], part[5]),
                Double.valueOf(part[3]));
        });
        return map;
    }
}
