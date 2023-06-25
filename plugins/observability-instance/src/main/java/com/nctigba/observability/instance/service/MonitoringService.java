/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package com.nctigba.observability.instance.service;

import java.util.List;
import java.util.Map;

import com.nctigba.observability.instance.model.monitoring.MonitoringMetric;
import com.nctigba.observability.instance.model.monitoring.MonitoringParam;

public interface MonitoringService {

    Map<String, Object> getPointMonitoringData(MonitoringParam monitoringParam);

    List<MonitoringMetric> getCurrentMonitoringData(MonitoringParam monitoringParam);

    List<Object> getRangeMonitoringData(MonitoringParam monitoringParam);
}
