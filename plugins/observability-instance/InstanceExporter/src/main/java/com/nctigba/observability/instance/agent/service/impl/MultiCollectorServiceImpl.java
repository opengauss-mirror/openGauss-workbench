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
 *  MultiCollectorServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/InstanceExporter/
 *  src/main/java/com/nctigba/observability/instance/agent/service/impl/MultiCollectorServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.agent.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.StopWatch;
import cn.hutool.core.thread.ThreadFactoryBuilder;
import cn.hutool.core.thread.ThreadUtil;
import com.nctigba.observability.instance.agent.config.model.TargetConfig;
import com.nctigba.observability.instance.agent.constant.CollectConstants;
import com.nctigba.observability.instance.agent.enums.DbTypeEnum;
import com.nctigba.observability.instance.agent.exception.CollectException;
import com.nctigba.observability.instance.agent.metric.Metric;
import com.nctigba.observability.instance.agent.metric.MetricResult;
import com.nctigba.observability.instance.agent.metric.MetricType;
import com.nctigba.observability.instance.agent.metric.db.OpengaussMetrics;
import com.nctigba.observability.instance.agent.model.dto.CollectParamDTO;
import com.nctigba.observability.instance.agent.model.dto.CollectTargetDTO;
import com.nctigba.observability.instance.agent.service.MetricCollectManagerService;
import com.nctigba.observability.instance.agent.service.TargetService;
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.core.metrics.Gauge;
import io.prometheus.metrics.model.registry.MultiCollector;
import io.prometheus.metrics.model.registry.PrometheusScrapeRequest;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author wuyuebin
 * @since 2024/4/12 16:35
 */
@Service
@Slf4j
public class MultiCollectorServiceImpl implements MultiCollector {
    @Autowired
    List<Metric> metricList;
    @Autowired
    OpengaussMetrics opengaussMetrics;
    @Autowired
    TargetService targetService;
    @Autowired
    MetricCollectManagerService metricCollectManager;

    @Override
    public MetricSnapshots collect() {
        return null;
    }

    @Override
    public MetricSnapshots collect(PrometheusScrapeRequest scrapeRequest) {
        long startTime = System.currentTimeMillis();
        String[] nameArr = scrapeRequest.getParameterValues("name[]");
        List<String> groupNames = new ArrayList<>();
        if (nameArr != null && nameArr.length > 0) {
            groupNames.addAll(Arrays.asList(nameArr));
        }
        String[] nodeIds = scrapeRequest.getParameterValues("nodeId");
        if (nodeIds.length == 0) {
            log.error("The parameters nodeId is not provided");
            throw new CollectException("The parameters nodeId is not provided");
        }
        String nodeId = nodeIds[0];
        TargetConfig targetConfig =
            targetService.getTargetConfigs().stream().filter(z -> z.getNodeId().equals(nodeId))
                .findFirst().orElse(null);
        if (targetConfig == null) {
            log.error("targetConfig is null");
            return new MetricSnapshots();
        }

        log.info("multi collector node:{}, IP:{}", targetConfig.getNodeId(), targetConfig.getMachineIP());
        List<MetricSnapshot> list = Collections.synchronizedList(new ArrayList<>());

        CountDownLatch countDownLatch = new CountDownLatch(2);
        ThreadUtil.execAsync(() -> {
            try {
                list.addAll(commonMetric(targetConfig, groupNames));
            } finally {
                countDownLatch.countDown();
            }
        });

        ThreadUtil.execAsync(() -> {
            try {
                if (DbTypeEnum.OPENGAUSS.name().equals(targetConfig.getDbType())) {
                    CollectTargetDTO target = new CollectTargetDTO();
                    target.setTargetConfig(targetConfig);
                    CollectParamDTO param = new CollectParamDTO();
                    param.setGroupNames(groupNames);
                    list.addAll(opengaussMetrics.collectData(target, param));
                }
            } catch (IOException e) {
                log.error("opengauss metrics fail: {}", e.getMessage());
            } finally {
                countDownLatch.countDown();
            }
        });

        try {
            countDownLatch.await(CollectConstants.COLLECT_TIMEOUT + 1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("multi collect countDownLatch await timeout: {}", e.getMessage());
        }
        long endTime = System.currentTimeMillis();
        log.info("Multi scrape metrics for node {}:takes {} ms", nodeId, endTime - startTime);
        return new MetricSnapshots(list);
    }

    public List<MetricSnapshot> commonMetric(TargetConfig targetConfig, List<String> groupNames) {
        long startTime = System.currentTimeMillis();
        List<MetricSnapshot> list = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch countDownLatch = new CountDownLatch(metricList.size());
        ThreadPoolExecutor executors = ThreadUtil.newExecutor(0, metricList.size());
        metricList.forEach(metric -> {
            executors.submit(() -> {
                try {
                    StopWatch stopWatch = new StopWatch();
                    stopWatch.start();
                    if (CollectionUtil.isNotEmpty(groupNames) && !groupNames.contains(metric.getGroupName())) {
                        return;
                    }
                    log.debug("collect:{}", metric.getNames());
                    String collectGroupKey = metric.getCollectGroupKey(targetConfig);
                    metricCollectManager.storeGroupCollectTime(collectGroupKey);
                    long gapTime = metricCollectManager.getGroupCollectGapTime(collectGroupKey);
                    if (gapTime >= (CollectConstants.MIN_COLLECT_INTERVAL - 1000)) {
                        scheduleCommonMetric(metric, gapTime, collectGroupKey, targetConfig, groupNames);
                    }
                    // get cache, if cached time <= CACHE_TIME_OUT, return
                    List<List<MetricResult>> result = metricCollectManager.getCachedGroupCollectData(collectGroupKey);
                    if (CollectionUtil.isEmpty(result)) {
                        CollectTargetDTO target = new CollectTargetDTO();
                        target.setTargetConfig(targetConfig);
                        CollectParamDTO param = new CollectParamDTO();
                        param.setGroupNames(groupNames);
                        result = metric.collectData(target, param);
                    }
                    if (CollectionUtil.isEmpty(result)) {
                        log.warn("Scrape metrics [{}] for node {}: value is empty",
                            metric.getGroupName(), targetConfig.getNodeId());
                        return;
                    }
                    for (int i = 0; i < metric.getNames().length; i++) {
                        List<MetricResult> resultItem = result.get(i);
                        if (MetricType.COUNTER.equals(metric.getType())) {
                            Counter counter = Counter.builder()
                                .name(metric.getNames()[i])
                                .help(metric.getHelps()[i])
                                .labelNames(metric.getLabelNames()).build();
                            resultItem.forEach(item -> {
                                counter.labelValues(item.getLabelValues()).inc(item.getValue());
                            });
                            list.add(counter.collect());
                        }
                        if (MetricType.GAUGE.equals(metric.getType())) {
                            Gauge gauge = Gauge.builder()
                                .name(metric.getNames()[i])
                                .help(metric.getHelps()[i])
                                .labelNames(metric.getLabelNames()).build();
                            resultItem.forEach(item -> {
                                gauge.labelValues(item.getLabelValues()).set(item.getValue());
                            });
                            list.add(gauge.collect());
                        }
                    }
                    stopWatch.stop();
                    if (stopWatch.getTotalTimeSeconds() > CollectConstants.COLLECT_TIMEOUT) {
                        log.warn("The real-time collection task for the metric [{}] of node {}:takes {} seconds, "
                                + "collection  has been timeout",
                            metric.getGroupName(), targetConfig.getNodeId(), stopWatch.getTotalTimeSeconds());
                    } else {
                        log.info("The real-time collection task for the metric [{}] of node {}:takes {} seconds",
                            metric.getGroupName(), targetConfig.getNodeId(), stopWatch.getTotalTimeSeconds());
                    }
                } catch (Exception e) {
                    log.error("The real-time collection task for the metric [{}] of node {} is fail!",
                        metric.getGroupName(), targetConfig.getNodeId());
                } finally {
                    countDownLatch.countDown();
                }
            });
        });

        try {
            countDownLatch.await(CollectConstants.COLLECT_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("commonMetric countDownLatch await timeout: {}", e.getMessage());
        }
        executors.shutdown();
        long endTime = System.currentTimeMillis();
        log.info("Scrape not db metrics for node {}:takes {} ms", targetConfig.getNodeId(),
            endTime - startTime);
        return list;
    }

    private void scheduleCommonMetric(Metric metric, long gapTime, String collectGroupKey, TargetConfig targetConfig,
                                      List<String> groupNames) {
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
            .setNamePrefix("Metric-" + gapTime + "-Collector-" + collectGroupKey).build();
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(
            namedThreadFactory);
        executor.schedule(() -> {
            // real query job
            try {
                StopWatch stopWatch = new StopWatch();
                stopWatch.start();
                CollectTargetDTO target = new CollectTargetDTO();
                target.setTargetConfig(targetConfig);
                CollectParamDTO param = new CollectParamDTO();
                param.setGroupNames(groupNames);
                List<List<MetricResult>> resultNext = metric.collectData(target, param);
                metricCollectManager.cacheGroupCollectData(collectGroupKey, resultNext);
                stopWatch.stop();
                if (stopWatch.getTotalTimeSeconds() > CollectConstants.COLLECT_TIMEOUT + 1) {
                    log.warn("The scheduled collection task for the metric [{}] of node {}:takes {} seconds, "
                            + "collection  has been timeout",
                        metric.getGroupName(), targetConfig.getNodeId(), stopWatch.getTotalTimeSeconds());
                } else {
                    log.info("The scheduled collection task for the metric [{}] of node {}:takes {} seconds",
                        metric.getGroupName(), targetConfig.getNodeId(), stopWatch.getTotalTimeSeconds());
                }
            } catch (Exception e) {
                log.error("The scheduled collection task for the metric [{}] of node {} is fail",
                    metric.getGroupName(), targetConfig.getNodeId());
                log.error(e.getMessage());
            } finally {
                executor.shutdown();
            }
        }, gapTime - CollectConstants.CACHE_TIME_OUT + 1000, TimeUnit.MILLISECONDS);
    }
}
