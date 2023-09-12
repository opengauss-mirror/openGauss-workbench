/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.agent.metric.opengauss;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opengauss.plugin.agent.metric.DBmetric;
import org.opengauss.plugin.agent.metric.Metric;
import org.opengauss.plugin.agent.util.DbUtil;
import org.springframework.stereotype.Service;

import io.prometheus.client.Collector;
import lombok.RequiredArgsConstructor;

/**
 * DBStatus
 *
 * @since 2023/8/7 11:46
 */
@Service
@RequiredArgsConstructor
public class DBStatus implements DBmetric {
    private static final String SQL = "select 1 as count";

    private final DbUtil dbUtil;

    @Override
    public Map<String, Metric> getMetric(Integer dbPort) throws FileNotFoundException, IOException {
        if (dbPort == null) {
            return Collections.emptyMap();
        }
        Map<String, Metric> map = new HashMap<>();
        List<Map<String, Object>> query = dbUtil.query(SQL);
        Integer count = 0;
        if (query.get(0).get("count") instanceof Integer) {
            count = (Integer) query.get(0).get("count");
        }
        map.put("pg_db_status", new Metric(Collector.Type.GAUGE, null).addValue(null, count));
        return map;
    }
}
