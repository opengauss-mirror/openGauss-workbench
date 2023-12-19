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
 *  MetricCollectManagerServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/InstanceExporter/
 *  src/main/java/com/nctigba/observability/instance/agent/service/impl/MetricCollectManagerServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.agent.service.impl;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import com.nctigba.observability.instance.agent.collector.AgentCollector;
import com.nctigba.observability.instance.agent.constant.CollectConstants;
import com.nctigba.observability.instance.agent.metric.MetricResult;
import com.nctigba.observability.instance.agent.service.MetricCollectManagerService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * MetricCollectManagerService Implement
 *
 * @since 2023/12/1
 */
@Service
public class MetricCollectManagerServiceImpl implements MetricCollectManagerService {
    private static final long CACHE_TIME_OUT = CollectConstants.CACHE_TIME_OUT;
    private static Map<String, AgentCollector> metricsCache = new HashMap<>();
    private static Map<String, Long> groupLastCollectTimeCache = new HashMap<>();
    private static Map<String, Long> groupThisCollectTimeCache = new HashMap<>();

    private TimedCache<String, List<List<MetricResult>>> timedCache = CacheUtil.newTimedCache(CACHE_TIME_OUT);

    @PostConstruct
    private void init() throws IOException {
        timedCache.schedulePrune(CACHE_TIME_OUT);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void storeCollector(String metricKey, AgentCollector collector) {
        metricsCache.put(metricKey, collector);
    }

    /**
     * @inheritDoc
     */
    @Override
    public AgentCollector getCollector(String metricKey) {
        return metricsCache.get(metricKey);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void storeGroupCollectTime(String groupCollectKey) {
        Long lastTime = groupThisCollectTimeCache.get(groupCollectKey);
        groupLastCollectTimeCache.put(groupCollectKey, lastTime);
        groupThisCollectTimeCache.put(groupCollectKey, System.currentTimeMillis());
    }

    /**
     * @inheritDoc
     */
    @Override
    public long getGroupCollectGapTime(String groupCollectKey) {
        Long lastTime = Optional.ofNullable(groupLastCollectTimeCache.get(groupCollectKey)).orElse(0L);
        Long thisTime = Optional.ofNullable(groupThisCollectTimeCache.get(groupCollectKey)).orElse(0L);
        if (lastTime != 0 && thisTime != 0 && thisTime > lastTime) {
            return thisTime - lastTime;
        }
        return 0;
    }

    /**
     * @inheritDoc
     */
    @Override
    public List<List<MetricResult>> getCachedGroupCollectData(String groupCollectKey) {
        return timedCache.get(groupCollectKey, false);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void cacheGroupCollectData(String groupCollectKey, List<List<MetricResult>> result) {
        timedCache.put(groupCollectKey, result);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void clear() {
        metricsCache = new HashMap<>();
        groupLastCollectTimeCache = new HashMap<>();
        groupThisCollectTimeCache = new HashMap<>();
        timedCache = CacheUtil.newTimedCache(CACHE_TIME_OUT);
    }
}
