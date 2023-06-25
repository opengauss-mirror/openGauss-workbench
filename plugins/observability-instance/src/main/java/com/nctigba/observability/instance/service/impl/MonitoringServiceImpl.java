/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package com.nctigba.observability.instance.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nctigba.common.web.exception.CustomException;
import com.nctigba.common.web.exception.CustomExceptionEnum;
import com.nctigba.observability.instance.constants.MonitoringResultType;
import com.nctigba.observability.instance.factory.MonitoringHandlerFactory;
import com.nctigba.observability.instance.handler.monitoring.MonitoringHandler;
import com.nctigba.observability.instance.model.monitoring.MonitoringMetric;
import com.nctigba.observability.instance.model.monitoring.MonitoringParam;
import com.nctigba.observability.instance.service.MonitoringService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MonitoringServiceImpl implements MonitoringService {
    @Autowired
    private MonitoringHandlerFactory monitoringHandlerFactory;

    @Override
    public Map<String, Object> getPointMonitoringData(MonitoringParam param) {
        MonitoringHandler monitoringHandler = monitoringHandlerFactory.getInstance(param.getMonitoringType());
        List<MonitoringMetric> monitoringMetricList = monitoringHandler.pointQuery(param.getQuery(), param.getTime());
        Map<String, Object> map = new HashMap<>();
        for (MonitoringMetric monitoringMetric : monitoringMetricList) {
            JSONObject metric = monitoringMetric.getMetric();
            JSONArray value = monitoringMetric.getValue();
            if (metric.containsKey("__name__")) {
                metric.put("value", value.size() == 2 ? value.get(1) : null);
                map.put(metric.getString("__name__"), metric);
            } else {
                map.put("value", value.size() == 2 ? value.get(1) : null);
            }
        }
        return map;
    }

    @Override
    public List<MonitoringMetric> getCurrentMonitoringData(MonitoringParam param) {
        MonitoringHandler monitoringHandler = monitoringHandlerFactory.getInstance(param.getMonitoringType());
        return monitoringHandler.pointQuery(param.getQuery(), param.getTime());
    }

    @Override
    public List<Object> getRangeMonitoringData(MonitoringParam param) {
        MonitoringHandler monitoringHandler = monitoringHandlerFactory.getInstance(param.getMonitoringType());
        List<MonitoringMetric> monitoringMetricList = monitoringHandler.rangeQuery(param.getQuery(), param.getStart(),
                param.getEnd(), param.getStep());
        if (MonitoringResultType.TABLE.name().equalsIgnoreCase(param.getType())) {
            return monitoringHandler.metricToTable(monitoringMetricList, param);
        } else if (MonitoringResultType.LINE.name().equalsIgnoreCase(param.getType())) {
            return monitoringHandler.metricToLine(monitoringMetricList, param);
        } else {
            throw new CustomException(CustomExceptionEnum.PARAM_INVALID_ERROR,
                    "Unsupported data format:" + param.getType());
        }
    }
}
