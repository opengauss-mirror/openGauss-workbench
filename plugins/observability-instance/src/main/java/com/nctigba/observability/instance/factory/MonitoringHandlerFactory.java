/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package com.nctigba.observability.instance.factory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.nctigba.observability.instance.constants.MonitoringType;
import com.nctigba.observability.instance.handler.monitoring.MonitoringHandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.stereotype.Component;

/**
 * The monitoring implementation class of MonitoringHandler uses the self
 * implemented bean name of monitoringType, such as prometheus<br>
 *
 * @author yangjie
 */

@Slf4j
@Component
public class MonitoringHandlerFactory {

    private Map<String, MonitoringHandler> handlerMap;

    @Resource
    private List<MonitoringHandler> handlerList;

    @PostConstruct
    public void init() {
        handlerMap = handlerList.stream()
                .collect(Collectors.toMap(MonitoringHandler::getMonitorType, Function.identity()));
        log.info("load MonitoringHandler complete. entity:{}", JSON.toJSONString(handlerMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entity -> AopUtils.getTargetClass(entity.getValue())))));

    }

    /**
     * Return the processing class of the monitoring according to the monitoring
     * type. If it is not found, return the default processing class
     *
     * @param monitoringType Monitoring type
     * @return MonitoringHandler Monitoring and processing class
     */
    public MonitoringHandler getInstance(String monitoringType) {
        return handlerMap.containsKey(monitoringType) ? handlerMap.get(monitoringType)
                : handlerMap.get(MonitoringType.DEFAULT.getMonitoringType());
    }
}
