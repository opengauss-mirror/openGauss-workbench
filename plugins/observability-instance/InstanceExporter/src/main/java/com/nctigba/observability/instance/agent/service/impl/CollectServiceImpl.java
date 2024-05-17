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

import com.nctigba.observability.instance.agent.metric.Metric;
import com.nctigba.observability.instance.agent.metric.db.OpengaussMetrics;
import com.nctigba.observability.instance.agent.service.CollectService;
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

    /**
     * @inheritDoc
     */
    @Override
    public String getMetricsData(String url, Optional<String[]> name, String nodeId) throws IOException {
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
        builder = builder.queryParam("nodeId", nodeId);
        ResponseEntity<String> response = restTemplate.getForEntity(builder.toUriString(), String.class);
        return response.getBody();
    }
}