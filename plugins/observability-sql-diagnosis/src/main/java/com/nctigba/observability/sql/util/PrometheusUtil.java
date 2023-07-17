/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.util;

import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.nctigba.common.web.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constants.history.PrometheusConstants;
import com.nctigba.observability.sql.mapper.NctigbaEnvMapper;
import com.nctigba.observability.sql.model.NctigbaEnv;
import com.nctigba.observability.sql.model.history.data.PrometheusData;
import com.nctigba.observability.sql.model.history.dto.MetricToTableDTO;
import com.nctigba.observability.sql.model.history.point.MetricDataDTO;
import com.nctigba.observability.sql.model.history.point.PrometheusDataDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.opengauss.admin.common.exception.CustomException;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * PrometheusUtil
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Component
@Slf4j
public class PrometheusUtil {
    @Autowired
    private NctigbaEnvMapper envMapper;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostFacade hostFacade;

    private String getPrometheusUrl() {
        var env = envMapper.selectOne(
                Wrappers.<NctigbaEnv>lambdaQuery().eq(NctigbaEnv::getType, NctigbaEnv.envType.PROMETHEUS));
        if (env == null) {
            throw new HisDiagnosisException("Prometheus not found");
        }
        var host = hostFacade.getById(env.getHostid());
        return "http://" + host.getPublicIp() + ":" + env.getPort();
    }

    public Object rangeQuery(String paramId, String item, String start, String end, String step) {
        String query;
        if (item.contains("history_diagnosis_nodeId")) {
            query = item.replace("history_diagnosis_nodeId", paramId);
        } else if (item.contains("history_diagnosis_hostId")) {
            query = item.replace("history_diagnosis_hostId", paramId);
        } else {
            query = item;
        }
        List<PrometheusData> prometheusDataList;
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(getPrometheusUrl() + PrometheusConstants.PROMETHEUS_QUERY_RANGE);
        builder.queryParam("query", query);
        builder.queryParam("start", start);
        builder.queryParam("end", end);
        builder.queryParam("step", step);
        String url = builder.build().encode().toUriString();
        log.info("request url:[{}]", url);
        try {
            String response = HttpUtils.sendGet(url.replace("+", "%2B"), "");
            JSONObject responseJson = JSONObject.parseObject(response);
            if (responseJson != null && "success".equals(responseJson.get("status"))) {
                JSONObject dataJson = JSONObject.parseObject(responseJson.getString("data"));
                prometheusDataList = JSON.parseArray(dataJson.getString("result"), PrometheusData.class);
            } else {
                log.info("query prometheus range data failed ! "
                        + "please check the log, the error message is:{}", response);
                return "error:query prometheus range data failed ! "
                        + "please check the log, the error message is:" + response;
            }
        } catch (CustomException e) {
            log.error(e.getMessage());
            return "error:" + e.getMessage();
        }
        if (prometheusDataList.size() > 0 && prometheusDataList.get(0).getMetric().size() < 1) {
            JSONObject metric = new JSONObject();
            metric.put("__name__", item);
            prometheusDataList.get(0).setMetric(metric);
        } else if (prometheusDataList.size() > 0) {
            prometheusDataList.get(0).getMetric().put("__name__", item);
        } else {
            return prometheusDataList;
        }
        return prometheusDataList;
    }

    public Map<String, List<Object>> transToTimeList(List<PrometheusData> metricList) {
        if (CollectionUtils.isEmpty(metricList)) {
            log.error("transToTimeList: prometheus data is empty!");
            throw new CustomException("");
        }
        // 1. Processing of original data and extracting duplicates__ name__ Value Data
        Map<String, List<PrometheusData>> metricMap = new HashMap<>();
        for (PrometheusData metric : metricList) {
            String metricName = metric.getMetric().getString("__name__");
            List<PrometheusData> list;
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
            throw new CustomException("");
        }
        Map<String, List<Object>> timeMetricMap = new HashMap<>();
        for (List<PrometheusData> metrics : metricMap.values()) {
            for (PrometheusData metric : metrics) {
                // Get the corresponding field
                String metricName = metric.getMetric().getString("wait_status");
                String warningMsg = metric.getMetric().getString("warning_msg");
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
                    map.put("metricData", valueArray.get(1).toString());
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

    public List<MetricToTableDTO> metricToTable(List<PrometheusData> metricList) {
        log.info("Monitoring data starts to be converted into tabular data");
        // Get timestamp data in descending order
        Map<String, List<Object>> timeMetricMap = transToTimeList(metricList);
        if (MapUtil.isEmpty(timeMetricMap)) {
            log.error("prometheus to table: data is empty!");
        }
        List<MetricToTableDTO> data = new ArrayList<>();
        for (List<Object> timeMetricList : timeMetricMap.values()) {
            List<String> columnNames = new ArrayList<>();
            List<Map<String, Object>> timeMertic = JSON.parseObject(JSON.toJSONString(timeMetricList),
                    new TypeReference<>() {
                    });
            ArrayList<ArrayList<String>> result = new ArrayList<>();
            // Record the metricData length
            int max = 0;
            for (Map<String, Object> timeMetricObj : timeMertic) {
                ArrayList<String> metricData = new ArrayList<>();
                metricData.add(timeMetricObj.get("metricData").toString());
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
                for (int j = 0; j < columnNames.size(); j++) {
                    MetricToTableDTO metricToTableDTO = new MetricToTableDTO();
                    String title = columnNames.get(j);
                    metricToTableDTO.setName(title);
                    metricToTableDTO.setValue(result.get(j).get(i));
                    data.add(metricToTableDTO);
                }
            }
        }
        log.info("Monitoring data completion converted to tabular data");
        return data;
    }

    public PrometheusDataDTO metricToLine(List<PrometheusData> metricList) {
        log.info("Monitoring data starts to be converted into line data");
        if (CollectionUtils.isEmpty(metricList)) {
            throw new CustomException("");
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        PrometheusDataDTO item = new PrometheusDataDTO();
        List<MetricDataDTO> dataDTOList = new ArrayList<>();
        for (PrometheusData metric : metricList) {
            // Index name
            String metricName = metric.getMetric().getString("__name__");
            item.setChartName(metricName);
            JSONArray lineValues = metric.getValues();
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
            MetricDataDTO metricDataDTO = new MetricDataDTO();
            metricDataDTO.setData(dataList);
            metricDataDTO.setName(metricName);
            dataDTOList.add(metricDataDTO);
            item.setTime(timeList);
        }
        item.setDatas(dataDTOList);
        log.info("Monitoring data completed convert to line data");
        return item;
    }
}
