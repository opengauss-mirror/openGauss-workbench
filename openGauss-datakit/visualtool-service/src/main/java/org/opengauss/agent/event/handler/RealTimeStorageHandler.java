/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.agent.event.handler;

import static org.opengauss.admin.common.constant.AgentConstants.Default.NONE;

import com.lmax.disruptor.EventHandler;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.codec.digest.MurmurHash3;
import org.opengauss.admin.common.core.domain.entity.agent.MetricRealTime;
import org.opengauss.admin.common.enums.agent.StoragePolicy;
import org.opengauss.admin.common.utils.spring.SpringUtils;
import org.opengauss.agent.data.CustomMetricData;
import org.opengauss.agent.data.CustomMetricDataPoint;
import org.opengauss.agent.event.MetricEvent;
import org.opengauss.agent.repository.MetricRealTimeStorageService;
import org.opengauss.agent.utils.AgentTaskUtils;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * RealTimeStorageHandler
 *
 * @author: wangchao
 * @Date: 2025/3/18 10:21
 * @Description: RealTimeStorageHandler
 * @since 7.0.0-RC2
 **/
@Slf4j
public class RealTimeStorageHandler implements EventHandler<MetricEvent> {
    private MetricRealTimeStorageService realTimeStorageService;

    public RealTimeStorageHandler() {
    }

    @Override
    public void onEvent(MetricEvent event, long sequence, boolean isEndOfBatch) {
        if (Objects.equals(StoragePolicy.REAL_TIME, event.getStoragePolicy()) || Objects.equals(StoragePolicy.HISTORY,
            event.getStoragePolicy())) {
            log.info("realTime metric event with {} event {} info  {}", sequence, isEndOfBatch, event.info());
            List<CustomMetricData> data = event.getMetricData();
            List<String> collectorMetrics = event.getCollectorMetrics();
            List<MetricRealTime> metricRealTimeList = new LinkedList<>();
            Map<String, String> propertyMap = event.getProperty();
            data.stream().filter(metric -> collectorMetrics.contains(metric.getName())).forEach(metric -> {
                List<CustomMetricDataPoint> points = metric.getPoints();
                points.forEach(point -> {
                    MetricRealTime metricData = MetricRealTime.builder()
                        .metric(metric.getName())
                        .agentId(event.getAgentId())
                        .clusterNodeId(StrUtil.isEmpty(event.getClusterNodeId()) ? NONE : event.getClusterNodeId())
                        .property(AgentTaskUtils.getProperty(metric.getName(), point.getAttributes(), propertyMap))
                        .value(point.getValue())
                        .startTime(point.getStartEpochNanos())
                        .endTime(point.getEpochNanos())
                        .createTime(Instant.now())
                        .build();
                    metricData.setMultiId(
                        MurmurHash3.hash32x86(metricData.pkVal().toString().getBytes(StandardCharsets.UTF_8)));
                    metricRealTimeList.add(metricData);
                });
            });
            realTimeStorageService.saveOrUpdateBatch(metricRealTimeList);
        }
    }

    @Override
    public void onStart() {
        log.info("Real time metric handler onStart");
        realTimeStorageService = SpringUtils.getBean(MetricRealTimeStorageService.class);
        log.info("MetricRealTimeStorageService is get successfully");
    }

    @Override
    public void onShutdown() {
        log.info("Real time metric handler onShutdown");
    }
}
