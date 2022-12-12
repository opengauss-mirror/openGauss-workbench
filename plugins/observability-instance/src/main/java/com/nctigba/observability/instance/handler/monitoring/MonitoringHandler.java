package com.nctigba.observability.instance.handler.monitoring;

import java.util.List;
import java.util.Map;

import com.nctigba.observability.instance.model.monitoring.MonitoringMetric;
import com.nctigba.observability.instance.model.monitoring.MonitoringParam;

public interface MonitoringHandler {
    /**
     * Monitoring system type
     *
     * @return
     */
    String getMonitorType();

    /**
     * Get time period monitoring data
     *
     * @param query
     * @param start prot
     * @param end   Connect Users
     * @param step  Connection password
     * @return Indicator set
     */
    List<MonitoringMetric> rangeQuery(String query, String start, String end, String step);

    /**
     * Get the monitoring data of the specified time node
     *
     * @param query ip
     * @param time  Specify time
     * @return Indicator set
     */
    List<MonitoringMetric> pointQuery(String query, String time);

    /**
     * Convert indicator data format to table
     *
     * @param rangeMetricList Range Indicator List
     * @param param           Indicator Query Parameters
     * @return Indicator set in tabular format
     */
    List<Object> metricToTable(List<MonitoringMetric> rangeMetricList, MonitoringParam param);

    /**
     * Convert indicator data format to line chart
     *
     * @param rangeMetricList Range Indicator List
     * @param param query parameters
     * @return Indicator set in polyline format
     */
    List<Object> metricToLine(List<MonitoringMetric> rangeMetricList, MonitoringParam param);

    /**
     * Sort the monitoring data
     *
     * @param tableList Table Data
     * @param param     Monitoring query parameters
     * @return Sorted indicator set
     */
    List<Map<String, String>> sortList(List<Object> tableList, MonitoringParam param);
}
