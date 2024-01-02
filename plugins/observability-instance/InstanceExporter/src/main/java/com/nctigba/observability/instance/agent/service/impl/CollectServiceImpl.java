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
 *  CollectServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/InstanceExporter/
 *  src/main/java/com/nctigba/observability/instance/agent/service/impl/CollectServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.agent.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.StopWatch;
import com.nctigba.observability.instance.agent.collector.AgentCollector;
import com.nctigba.observability.instance.agent.collector.AgentCounter;
import com.nctigba.observability.instance.agent.collector.AgentGauge;
import com.nctigba.observability.instance.agent.constant.CollectConstants;
import com.nctigba.observability.instance.agent.exception.CollectException;
import com.nctigba.observability.instance.agent.metric.Metric;
import com.nctigba.observability.instance.agent.metric.MetricResult;
import com.nctigba.observability.instance.agent.metric.MetricType;
import com.nctigba.observability.instance.agent.metric.db.OpengaussMetrics;
import com.nctigba.observability.instance.agent.model.dto.CollectParamDTO;
import com.nctigba.observability.instance.agent.model.dto.CollectTargetDTO;
import com.nctigba.observability.instance.agent.service.CollectService;
import com.nctigba.observability.instance.agent.service.MetricCollectManagerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * CollectService Implement
 *
 * @since 2023/12/1
 */
@Service
@Slf4j
public class CollectServiceImpl implements CollectService {
    @Autowired
    OpengaussMetrics opengaussMetrics;
    @Autowired
    List<Metric> metricList;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    MetricCollectManagerService metricCollectManager;

    /**
     * @inheritDoc
     */
    @Override
    public void collectMetricsData(CollectTargetDTO target, CollectParamDTO param) throws IOException {
        opengaussMetrics.collectData(target, param);

        long startTime = System.currentTimeMillis();

        metricList.forEach(z -> {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            String collectGroupKey = z.getCollectGroupKey(target.getTargetConfig());

            // filter collect data
            if (param.getGroupNames() != null
                    && !param.getGroupNames().isEmpty()
                    && param.getGroupNames().indexOf(z.getGroupName()) < 0) {
                return;
            }

            // TOD: to remove same timed task with same key
            // get cached data, if not > CACHE_TIME_OUT，return directly
            metricCollectManager.storeGroupCollectTime(collectGroupKey);

            // get cache, if cached time <= CACHE_TIME_OUT, return
            List<List<MetricResult>> result = metricCollectManager.getCachedGroupCollectData(collectGroupKey);

            // no cache, collect data
            if (CollectionUtil.isEmpty(result)) {
                try {
                    result = z.collectData(target, param);
                } catch (CollectException e) {
                    log.error("Collect data error for group: {}", z.getGroupName());
                }
            }

            // if has time range after last scrape, build once timed task after (time range - 2s)
            // just support smallest range = MIN_COLLECT_INTERVAL
            // so, just create job when time range > (MIN_COLLECT_INTERVAL-1s)
            // 15s gap time，and cache valid time is CACHE_TIME_OUT
            // my once timed task should start after gap time - CACHE_TIME_OUT +1
            // TOD: set a max value, to void task which is after a very long time
            long gapTime = metricCollectManager.getGroupCollectGapTime(collectGroupKey);
            if (gapTime >= (CollectConstants.MIN_COLLECT_INTERVAL - 1000)) {
                log.debug("Cache scrape metrics for node {}: match gapTime {} seconds",
                        target.getTargetConfig().getNodeId(), gapTime);
                ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
                executor.schedule(() -> {
                    // real query job
                    List<List<MetricResult>> resultNext = z.collectData(target, param);
                    metricCollectManager.cacheGroupCollectData(collectGroupKey, resultNext);
                    executor.shutdown();
                    log.debug("Cached metric data scrape metrics for node {}:{}",
                            target.getTargetConfig().getNodeId(), z.getGroupName());
                }, gapTime - CollectConstants.CACHE_TIME_OUT + 1000, TimeUnit.MILLISECONDS);
            }

            if (CollectionUtil.isEmpty(result)) {
                return;
            }
            // set collector value
            for (int i = 0; i < z.getNames().length; i++) {
                String collectKey = z.getCollectorKey(target.getTargetConfig(), i);

                // clear group data,
                // void one data has data at A time and has no data at B time, then B time still collect
                // value of Atime
                AgentCollector agentCollector = metricCollectManager.getCollector(collectKey);
                agentCollector.cleanAllLabelValuesData();

                List<MetricResult> resultItem = result.get(i);
                if (z.getType().equals(MetricType.COUNTER) && agentCollector instanceof AgentCounter) {
                    resultItem.forEach(metricResult -> {
                        ((AgentCounter) agentCollector)
                                .labelValues(metricResult.getLabelValues())
                                .inc(metricResult.getValue());
                    });
                } else if (z.getType().equals(MetricType.GAUGE) && agentCollector instanceof AgentGauge) {
                    resultItem.forEach(metricResult -> {
                        ((AgentGauge) agentCollector)
                                .labelValues(metricResult.getLabelValues())
                                .set(metricResult.getValue());
                    });
                } else {
                    log.error("Metric type not support:{}", z.getType());
                }
            }

            // print run time
            stopWatch.stop();
            log.debug("Scrape metrics [{}] for node {}:takes {} seconds",
                    z.getGroupName(), target.getTargetConfig().getNodeId(), stopWatch.getTotalTimeSeconds());
        });

        // print run time
        long endTime = System.currentTimeMillis();
        log.debug("Scrape not db metrics for node {}:takes {} ms", target.getTargetConfig().getNodeId(),
                endTime - startTime);
    }

    /**
     * @inheritDoc
     */
    @Override
    public String getMetricsData(String url, Optional<String[]> name) throws IOException {
        List<String> nameParams = new ArrayList<>();
        if (name.isPresent()) {
            nameParams = new ArrayList<>(Arrays.asList(name.get()));
            // openGauss send groupName in name[],need to change to real metric name
            List<String> matchMetricNames = opengaussMetrics.getRealMetricNameByGroupName(name.get());
            nameParams.addAll(matchMetricNames);

            // Metric change group name to real metric name
            final List<String> nameParamsTemp = nameParams;
            for (int i = 0; i < metricList.size(); i++) {
                if (nameParamsTemp.contains(metricList.get(i).getGroupName())) {
                    nameParams.addAll(Arrays.asList(metricList.get(i).getNames()));
                }
            }
            log.debug("nameParams:{}", nameParams);
        }
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        for (int i = 0; i < nameParams.size(); i++) {
            builder = builder.queryParam("name[]", nameParams.get(i));
        }
        ResponseEntity<String> response = restTemplate.getForEntity(builder.toUriString(), String.class);
        return response.getBody();
    }
}