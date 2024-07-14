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
 *  MetricsService.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/main/java/com/nctigba/observability/instance/service/MetricsService.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.service;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.nctigba.observability.instance.enums.MetricsLine;
import com.nctigba.observability.instance.enums.MetricsValue;
import com.nctigba.observability.instance.mapper.NctigbaEnvMapper;
import com.nctigba.observability.instance.model.dto.MetricQueryDTO;
import com.nctigba.observability.instance.model.dto.PrometheusQueryDTO;
import com.nctigba.observability.instance.model.entity.NctigbaEnvDO;
import com.nctigba.observability.instance.service.MetricsService.PrometheusResult.PromData.MonitoringMetric;
import com.nctigba.observability.instance.util.ListUtils;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.opengauss.admin.common.exception.CustomException;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.nctigba.observability.instance.enums.MetricsLine.INSTANCE_DB_SLOWSQL;
import static com.nctigba.observability.instance.enums.MetricsLine.INSTANCE_DB_SLOWSQL_DBNAME;

@Service
@Log4j2
public class MetricsService {
    // prometheus query ?query={query}
    private static final String PROMETHEUS_QUERY_POINT = "/api/v1/query";

    // prometheus range query ?query={query}&start={start}&end={end}&step={step}
    private static final String PROMETHEUS_QUERY_RANGE = "/api/v1/query_range";
    private static final String TIME = "time";
    private static final Map<String, String> PROM = new HashMap<>();
    private static final String DEFAULT = "DEFAULT";
    private static final Pattern PATTERN = Pattern.compile("metrics\\.([^\\.]+)\\.name");
    @Autowired
    private NctigbaEnvMapper envMapper;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostFacade hostFacade;
    @Autowired
    private ClusterManager clusterManager;
    @Autowired
    private TopSQLService topSQLService;

    private String getPrometheusUrl() {
        if (PROM.containsKey(DEFAULT)) {
            return PROM.get(DEFAULT);
        }
        var env = envMapper
                .selectOne(Wrappers.<NctigbaEnvDO>lambdaQuery().eq(
                        NctigbaEnvDO::getType,
                        NctigbaEnvDO.envType.PROMETHEUS_MAIN));
        if (env == null) {
            throw new CustomException("Prometheus not found");
        }
        String url = "http://127.0.0.1:" + env.getPort();
        PROM.put(DEFAULT, url);
        return url;
    }

    private PrometheusResult query(String type, String query, Map<String, Object> param) {
        String baseUrl = getPrometheusUrl() + type;
        var result = HttpUtil.get(baseUrl, param);
        var prometheusResult = JSONUtil.toBean(result, PrometheusResult.class);
        prometheusResult.isSuccess();
        return prometheusResult;
    }

    /**
     * prometheus one value
     *
     * @param query promql
     * @param time  second
     * @return prometheus result
     */
    public List<MonitoringMetric> value(String query, Long time) {
        if (StrUtil.isBlank(query)) {
            throw new NullPointerException("query null");
        }
        log.info("promQL:{}, time:{}", query, time);
        var prometheusResult = query(PROMETHEUS_QUERY_POINT, query, Map.of("query", query, "time", time));
        return prometheusResult.getData().getResult();
    }

    /**
     * prometheus line value
     *
     * @param query promql
     * @param start second
     * @param end   second
     * @param step  second
     * @return prometheus result
     */
    public List<MonitoringMetric> list(String query, Number start, Number end, Integer step) {
        if (StrUtil.isBlank(query)) {
            return Collections.emptyList();
        }
        log.info("promQL:{}, start:{}, end:{}, step:{}", query, start, end, step);
        var prometheusResult = query(PROMETHEUS_QUERY_RANGE, query,
                Map.of("query", query, "start", start, "end", end, "step", step));
        return prometheusResult.getData().getResult();
    }

    /**
     * prometheus batch value
     *
     * @param metricsArr metrics
     * @param nodeId     node id
     * @param start      second
     * @param end        second
     * @param step       second
     * @return prometheus result
     */
    public Map<String, Object> listBatch(Enum<?>[] metricsArr, String nodeId, Long start, Long end, Integer step) {
        MetricQueryDTO dto = new MetricQueryDTO(metricsArr, nodeId, start, end, step, null);
        return executePrometheus(dto);
    }

    /**
     * prometheus slow sql
     *
     * @param dto metric query dto
     * @return prometheus result
     */
    public Map<String, Object> slowSqlList(MetricQueryDTO dto) {
        Map<String, Object> result = executePrometheus(dto);
        result.put("slowSqlThreshold", topSQLService.getSlowSqlThreshold(dto.getNodeId()));
        return result;
    }

    private Map<String, Object> executePrometheus(MetricQueryDTO dto) {
        var result = new HashMap<String, Object>();
        var node = clusterManager.getOpsNodeById(dto.getNodeId());
        List<Long> timeline = new ArrayList<>();
        for (long i = dto.getStart(); i < dto.getEnd(); i += dto.getStep()) {
            timeline.add(i);
        }
        result.put(TIME, timeline.stream().map(t -> new Date(t.longValue() * 1000)).collect(Collectors.toList()));
        var countDown = ThreadUtil.newCountDownLatch(dto.getMetricsArr().length);
        for (Object metric : dto.getMetricsArr()) {
            ThreadUtil.execute(() -> {
                try {
                    PrometheusQueryDTO queryDTO = new PrometheusQueryDTO();
                    queryDTO.setDbName(dto.getDbName());
                    queryDTO.setMetric(metric);
                    queryDTO.setStart(dto.getStart());
                    queryDTO.setEnd(dto.getEnd());
                    queryDTO.setNode(node);
                    queryDTO.setStep(dto.getStep());
                    result.putAll(extracted(queryDTO));
                } finally {
                    countDown.countDown();
                }
            });
        }
        try {
            countDown.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new CustomException(e.getMessage());
        }
        return result;
    }

    private Map<String, Object> extracted(PrometheusQueryDTO dto) {
        if (dto.getMetric() instanceof MetricsLine) {
            MetricsLine metricLine = (MetricsLine) dto.getMetric();
            String promQl = metricLine.promQl(dto.getNode());
            if (metricLine.equals(INSTANCE_DB_SLOWSQL) && StrUtil.isNotEmpty(dto.getDbName())) {
                String tmpQl = INSTANCE_DB_SLOWSQL_DBNAME.promQl(dto.getNode());
                promQl = tmpQl.replace("postgres", dto.getDbName());
            }
            var metrics = list(promQl, dto.getStart(), dto.getEnd(), dto.getStep());
            List<Long> timeline = new ArrayList<>();
            for (long i = dto.getStart(); i < dto.getEnd(); i += dto.getStep()) {
                timeline.add(i);
            }
            return Map.of(metricLine.name(), parseLine(promQl, metrics, timeline, metricLine.getTemplate()));
        }
        if (dto.getMetric() instanceof MetricsValue) {
            MetricsValue metricValue = (MetricsValue) dto.getMetric();
            String promQl = metricValue.promQl(dto.getNode());
            var metrics = value(promQl, System.currentTimeMillis() / 1000);
            return Map.of(metricValue.name(), parseValue(promQl, metrics, metricValue.getTemplate()));
        }
        return Collections.emptyMap();
    }

    private Object parseValue(String promQl, List<MonitoringMetric> metric, String template) {
        if (metric.size() == 0) {
            return null;
        } else if (metric.size() == 1) {
            return metric.get(0).getValue().get(1);
        } else {
            var map = new HashMap<String, Object>();
            for (var monitoringMetric : metric) {
                if (template == null) {
                    return metric.get(0).getValue().get(1);
                }
                String key = StrUtil.format(template, monitoringMetric.getMetric());
                map.put(key, monitoringMetric.getValue().get(1));
            }
            return map;
        }
    }

    private Object parseLine(String promQl, List<MonitoringMetric> metric, List<Long> timeline, String template) {
        if (metric.size() == 0) {
            return null;
        } else if (StrUtil.isBlank(template)) {
            return ListUtils.collect(metric.get(0).getValues(), timeline);
        } else {
            var map = new HashMap<String, Object>();
            for (var monitoringMetric : metric) {
                String key = StrUtil.format(template, monitoringMetric.getMetric());
                var lineNumber = ListUtils.collect(monitoringMetric.getValues(), timeline);
                map.put(key, lineNumber);
            }
            return map;
        }
    }

    /**
     * Get all metrics keys
     *
     * @return java.util.List<java.lang.String>
     * @throws IOException Read messages.properties error
     * @since 2023/12/1
     */
    public List<String> getAllMetricKeys() throws IOException {
        Properties properties = new Properties();
        InputStream fis = this.getClass().getClassLoader().getResourceAsStream("messages.properties");
        properties.load(fis);
        fis.close();

        Set<Object> keySet = properties.keySet();
        Iterator<Object> iterator = keySet.iterator();
        List<String> metricNames = new ArrayList<>();
        while (iterator.hasNext()) {
            Object key = iterator.next();
            Matcher matcher = PATTERN.matcher(key.toString());
            if (matcher.matches()) {
                String name = matcher.group(1);
                metricNames.add(name);
            }
        }

        return metricNames;
    }

    @Data
    public static class PrometheusResult {
        Stat status;
        PromData data;

        public void isSuccess() {
            if (status == Stat.error) {
                throw new CustomException(error);
            }
        }

        @Data
        public static class PromData {
            ResultType resultType;
            List<MonitoringMetric> result;

            @Data
            public static class MonitoringMetric {
                Map<String, String> metric;
                List<Number> value;
                List<List<Number>> values;
            }

            public enum ResultType {
                matrix,
                vector,
                scalar,
                string;
            }
        }

        String errorType;
        String error;

        public enum Stat {
            success,
            error
        }
    }
}