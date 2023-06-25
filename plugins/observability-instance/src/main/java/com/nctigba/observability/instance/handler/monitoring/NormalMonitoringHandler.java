/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package com.nctigba.observability.instance.handler.monitoring;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.nctigba.observability.instance.constants.CommonConstants;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UriComponentsBuilder;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.nctigba.common.web.exception.CustomException;
import com.nctigba.common.web.exception.CustomExceptionEnum;
import com.nctigba.observability.instance.constants.MonitoringConstants;
import com.nctigba.observability.instance.constants.MonitoringType;
import com.nctigba.observability.instance.entity.NctigbaEnv;
import com.nctigba.observability.instance.entity.NctigbaEnv.envType;
import com.nctigba.observability.instance.mapper.NctigbaEnvMapper;
import com.nctigba.observability.instance.model.monitoring.MonitoringMetric;
import com.nctigba.observability.instance.model.monitoring.MonitoringParam;
import com.nctigba.observability.instance.util.HttpUtils;

import cn.hutool.core.map.MapUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class NormalMonitoringHandler implements MonitoringHandler {
    @Autowired
    protected NctigbaEnvMapper envMapper;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    protected HostFacade hostFacade;

    private String getPrometheusUrl() {
        var env = envMapper.selectOne(Wrappers.<NctigbaEnv>lambdaQuery().eq(NctigbaEnv::getType, envType.PROMETHEUS));
        if (env == null)
            throw new RuntimeException("Prometheus not found");
        var host = hostFacade.getById(env.getHostid());
        return "http://" + host.getPublicIp() + ":" + env.getPort();
    }

    @Override
    public String getMonitorType() {
        return MonitoringType.DEFAULT.getMonitoringType();
    }

    /**
     * Querying prometheus data for a period of time
     *
     * @param query Indicator parameters
     * @param start start time
     * @param end   End time
     * @param step  step
     * @return List<MonitoringMetric>
     */
    @Override
    public List<MonitoringMetric> rangeQuery(String query, String start, String end, String step) {
        List<MonitoringMetric> monitoringMetricList = null;
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(getPrometheusUrl() + MonitoringConstants.PROMETHEUS_QUERY_RANGE);
        builder.queryParam("query", query);
        builder.queryParam("start", start);
        builder.queryParam("end", end);
        builder.queryParam("step", step);
        String url = builder.build().encode().toUriString();
        log.info("request url:[{}]", url);
        try {
            String response = HttpUtils.sendGet(url.replace("+", "%2B"), "");
            JSONObject responseJson = JSONObject.parseObject(response);
            if ("success".equals(responseJson.get("status"))) {
                JSONObject dataJson = JSONObject.parseObject(responseJson.getString("data"));
                monitoringMetricList = JSON.parseArray(dataJson.getString("result"), MonitoringMetric.class);
            } else {
                log.info("query prometheus range data failed ! please check the log, the error message is:{}",
                        response);
                throw new CustomException(CustomExceptionEnum.INTERNAL_SERVER_ERROR);
            }
        } catch (CustomException e) {
            log.error(e.getMessage());
            throw new CustomException("create URI failed");
        }
        return monitoringMetricList;
    }

    /**
     * Query prometheus data at a specified time
     *
     * @param query Indicator parameters
     * @param time  Specify the timestamp. The default is the current system time of
     *              prometheus
     * @return List<MonitoringMetric>
     */
    @Override
    public List<MonitoringMetric> pointQuery(String query, String time) {
        List<MonitoringMetric> monitoringMetricList;
        String baseUrl = getPrometheusUrl() + MonitoringConstants.PROMETHEUS_QUERY_POINT;
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl);
        builder.queryParam("query", query);
        if (StringUtils.isNotEmpty(time)) {
            builder.queryParam("time", time);
        }
        String url = builder.build().encode().toUriString();
        log.info("request url:[{}]", url);
        try {
            String response = HttpUtils.sendGet(url.replace("+", "%2B"), "");
            JSONObject responseJson = JSONObject.parseObject(response);
            if ("success".equals(responseJson.get("status"))) {
                JSONObject dataJson = JSONObject.parseObject(responseJson.getString("data"));
                monitoringMetricList = JSON.parseArray(dataJson.getString("result"), MonitoringMetric.class);
            } else {
                log.info("query prometheus range data failed ! please check the log, the error message is:{}",
                        response);
                throw new CustomException(CustomExceptionEnum.INTERNAL_SERVER_ERROR);
            }
        } catch (CustomException e) {
            log.error(e.getMessage());
            throw new CustomException("create URI failed");
        }
        return monitoringMetricList;
    }

    @Override
    public List<Object> metricToTable(List<MonitoringMetric> metricList, MonitoringParam param) {
        log.info("Monitoring data starts to be converted into tabular data");
        List<Object> tableList = null;
        // Get timestamp data in descending order
        Map<String, List<Object>> timeMetricMap = transToTimeList(metricList);
        if (MapUtil.isEmpty(timeMetricMap)) {
            log.error("prometheus to table: data is empty!");
            throw new CustomException(CustomExceptionEnum.MONITORING_ACCESS_DATA_ERROR);
        }
        List<Object> data = new ArrayList<>();
        List<String> columnNames = new ArrayList<>();
        for (List<Object> timeMetricList : timeMetricMap.values()) {
            List<Map<String, Object>> timeMertic = JSON.parseObject(JSON.toJSONString(timeMetricList),
                    new TypeReference<List<Map<String, Object>>>() {
                    });
            ArrayList<ArrayList<String>> result = new ArrayList<>();
            // Record the metricData length
            int max = 0;
            for (Map<String, Object> timeMetricObj : timeMertic) {
                ArrayList<String> metricData = JSON.parseObject(timeMetricObj.get("metricData").toString(),
                        new TypeReference<ArrayList<String>>() {
                        });
                if (ObjectUtils.isNotEmpty(metricData) && metricData.size() > 0) {
                    max = Math.max(max, metricData.size());
                    String title = timeMetricObj.get("metricName").toString();
                    if (!columnNames.contains(title)) {
                        columnNames.add(title);
                    }
                    result.add(metricData);
                }
            }
            // Store the converted structure into data in turn
            for (int i = 0; i < max; i++) {
                Map<String, Object> map = new HashMap<>();
                for (int j = 0; j < columnNames.size(); j++) {
                    String title = columnNames.get(j);
                    map.put(title, result.get(j).get(i));
                }
                data.add(map);
            }
        }
        tableList = data;
        // Determine whether to sort
        if (StringUtils.isNotEmpty(param.getField())) {
            log.info("Start of monitoring data sorting");
            List<Map<String, String>> tableSortList = this.sortList(tableList, param);
            JSONArray jsonArray = new JSONArray();
            jsonArray.addAll(tableSortList);
            log.info("Monitoring data sorting completed");
            return jsonArray.toJavaList(Object.class);
        }
        log.info("Monitoring data completion converted to tabular data");
        return tableList;
    }

    private Map<String, List<Object>> transToTimeList(List<MonitoringMetric> metricList) {
        if (CollectionUtils.isEmpty(metricList)) {
            log.error("transToTimeList: prometheus data is empty!");
            throw new CustomException(CustomExceptionEnum.MONITORING_ACCESS_DATA_ERROR);
        }
        // 1. Processing of original data and extracting duplicates__ name__ Value Data
        Map<String, List<MonitoringMetric>> metricMap = new HashMap<>();
        for (MonitoringMetric metric : metricList) {
            String metricName = metric.getMetric().getString(CommonConstants.NAME);
            List<MonitoringMetric> list;
            if (metricMap.containsKey(metricName)) {
                list = metricMap.get(metricName);
            } else {
                list = new ArrayList<>();
            }
            list.add(metric);
            metricMap.put(metricName, list);
        }
        // 2. Process the data and return it in the form of a map. The key is a
        // timestamp, the value is a metric list, and the timeMetricMap is used to store
        // the processed data
        if (metricMap.isEmpty()) {
            log.error("The first processing of the raw data results in a null result!");
            throw new CustomException(CustomExceptionEnum.MONITORING_ACCESS_DATA_ERROR);
        }
        Map<String, List<Object>> timeMetricMap = new HashMap<>();
        for (List<MonitoringMetric> metrics : metricMap.values()) {
            for (MonitoringMetric metric : metrics) {
                // Get the corresponding field
                String metricName = metric.getMetric().getString(CommonConstants.NAME);
                String warningMsg = metric.getMetric().getString("warning_msg");
                Object metricData = JSON.parse(metric.getMetric().getString("table"));
                JSONArray values = metric.getValues();
                for (Object value : values) {
                    JSONArray valueArray = JSONArray.parseArray(JSONObject.toJSON(value).toString());
                    List<Object> list;
                    String curTimeStamp = valueArray.get(0).toString();
                    if (timeMetricMap.containsKey(curTimeStamp)) {
                        list = timeMetricMap.get(curTimeStamp);
                    } else {
                        list = new ArrayList<>();
                    }
                    Map<String, Object> map = new HashMap<>();
                    map.put("metricData", metricData);
                    map.put("metricName", metricName);
                    map.put("warning_msg", warningMsg);
                    list.add(map);
                    timeMetricMap.put(curTimeStamp, list);
                }
            }
        }
        // Sort in descending order
        LinkedHashMap<String, List<Object>> result = new LinkedHashMap<>();
        timeMetricMap.entrySet().stream().sorted((c1, c2) -> c2.getKey().compareTo(c1.getKey()))
                .forEachOrdered(x -> result.put(x.getKey(), x.getValue()));
        return result;
    }

    @Override
    public List<Object> metricToLine(List<MonitoringMetric> metricList, MonitoringParam param) {
        log.info("Monitoring data starts to be converted into line data");
        if (CollectionUtils.isEmpty(metricList)) {
            return Collections.singletonList(metricList);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (MonitoringMetric metric : metricList) {
            // Index name
            String metricName = metric.getMetric().getString(CommonConstants.NAME);
            if (StringUtils.isNotBlank(param.getLegendName())) {
                metricName = metric.getMetric().getString(param.getLegendName());
            }
            JSONArray lineValues = metric.getValues();
            Map<String, Object> item = new HashMap<>();
            // Get data of time and value
            List<String> timeList = new ArrayList<>();
            List<String> dataList = new ArrayList<>();
            for (Object value : lineValues) {
                JSONArray valueArray = JSONArray.parseArray(JSONObject.toJSON(value).toString());
                if (valueArray.size() > 1) {
                    // Convert timestamp to date format
                    String time = simpleDateFormat
                            .format(new Date(Long.parseLong(valueArray.get(0).toString()) * 1000));
                    timeList.add(time);
                    dataList.add(valueArray.get(1).toString());
                }
            }

            // Encapsulated into the echarts data
            item.put("name", metricName);
            item.put("data", dataList);
            item.put("time", timeList);
            item.put("type", "line");

            result.add(item);
        }
        log.info("Monitoring data completed convert to line data");
        return Collections.singletonList(result);
    }

    @Override
    public List<Map<String, String>> sortList(List<Object> tableList, MonitoringParam param) {
        String field = param.getField();
        if (StringUtils.isEmpty(field)) {
            log.error("field cannot be null!");
            throw new CustomException(CustomExceptionEnum.PARAM_INVALID_ERROR, "field cannot be null!");
        }
        if (StringUtils.isEmpty(param.getOrder())) {
            log.error("order cannot be null!");
            throw new CustomException(CustomExceptionEnum.PARAM_INVALID_ERROR, "order cannot be null!");
        }
        if (ObjectUtils.isEmpty(tableList)) {
            log.error("result is null!");
            throw new CustomException(CustomExceptionEnum.PARAM_INVALID_ERROR, "result cannot be null!");
        }
        // Sort by field
        List<Map<String, String>> listMapSort = JSON.parseObject(JSON.toJSONString(tableList),
                new TypeReference<List<Map<String, String>>>() {
                });
        // Sorting is divided into numerical type and time interval type
        String firstValue = listMapSort.get(0).get(field);
        if (firstValue.contains(":")) {
            // Interval type
            listMapSort = listMapSort.stream()
                    .sorted((x, y) -> (int) (resolutionInterval(y.get(field)) - resolutionInterval(x.get(field))))
                    .collect(Collectors.toList());

        } else {
            // Digital
            listMapSort = listMapSort.stream()
                    .sorted((x, y) -> BigDecimal.valueOf(Double.parseDouble(y.get(field)))
                            .compareTo(BigDecimal.valueOf(Double.parseDouble(x.get(field)))))
                    .collect(Collectors.toList());
        }
        // De duplication according to filter
        if (StringUtils.isNotEmpty(param.getFilter())) {
            listMapSort = distinctByKey(listMapSort, param.getFilter());
        }
        // Intercept the first 10 lines of listMapSort
        int listMapSortLength = listMapSort.size();
        if (listMapSortLength <= 10) {
            return listMapSort;
        }
        // Desc in reverse order, the default is positive order
        if (!"desc".equalsIgnoreCase(param.getOrder())) {
            Collections.reverse(listMapSort);
        }
        log.info("monitoring data sort finish! sortType:{}", param.getOrder());
        return listMapSort.subList(0, 10);
    }

    private List<Map<String, String>> distinctByKey(List<Map<String, String>> listMapSort, String filter) {
        Set<String> set = new HashSet<>();
        List<Map<String, String>> newListMapSort = new ArrayList<>();
        for (Map<String, String> stringStringMap : listMapSort) {
            String text = stringStringMap.get(filter);
            if (!set.contains(text)) {
                set.add(text);
                newListMapSort.add(stringStringMap);
            }
        }
        log.info("monitoring data distinct finish! distinct filed:{}", filter);
        return newListMapSort;
    }

    private long resolutionInterval(String interval) {
        if (StringUtils.isEmpty(interval)) {
            return 0;
        }
        long stamp = 0;
        String time = interval;
        if (interval.contains(",")) {
            String[] intervalArr = interval.split(",");
            String timeDay = intervalArr[0].trim();
            time = intervalArr[1].trim();
            long timeDayNumber = Long.parseLong(timeDay.substring(0, timeDay.indexOf("day")).trim());
            stamp = stamp + timeDayNumber * 86400;
        }
        if (interval.contains(".")) {
            String[] timeArr = time.split(".")[0].split(":");
            int timeCount = Integer.parseInt(timeArr[0]) * 3600 + Integer.parseInt(timeArr[1]) * 60
                    + Integer.parseInt(timeArr[2]);
            stamp = stamp + timeCount;
        }
        return stamp;
    }
}
