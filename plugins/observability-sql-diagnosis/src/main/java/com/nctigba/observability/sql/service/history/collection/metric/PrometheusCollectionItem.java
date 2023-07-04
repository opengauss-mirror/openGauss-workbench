/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history.collection.metric;

import com.nctigba.observability.sql.constants.history.PrometheusConstants;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.HisDiagnosisThreshold;
import com.nctigba.observability.sql.model.history.data.PrometheusData;
import com.nctigba.observability.sql.service.history.collection.CollectionItem;
import com.nctigba.observability.sql.util.HostUtil;
import com.nctigba.observability.sql.util.PrometheusUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * PrometheusCollectionItem
 *
 * @author luomeng
 * @since 2023/6/9
 */
public abstract class PrometheusCollectionItem implements CollectionItem<Object> {
    @Autowired
    private PrometheusUtil prometheusUtil;
    @Autowired
    private HostUtil hostUtil;

    public Object collectData(HisDiagnosisTask task) {
        if (task.getHisDataEndTime() == null) {
            Date endDate = new Date();
            task.setHisDataEndTime(endDate);
        }
        List<HisDiagnosisThreshold> thresholds = task.getThresholds();
        String metric = getPrometheusParam(thresholds);
        String queryId = getParamId(metric, task.getNodeId());
        long startTime = task.getHisDataStartTime().getTime() / PrometheusConstants.MS;
        long endTime = task.getHisDataEndTime().getTime() / PrometheusConstants.MS;
        long rangTime = endTime - startTime;
        List<PrometheusData> dataList = new ArrayList<>();
        if (endTime - startTime > PrometheusConstants.MAX_STEP_NUM) {
            for (int i = 1; i <= rangTime / PrometheusConstants.MAX_STEP_NUM + 1; i++) {
                List<PrometheusData> partList = new ArrayList<>();
                String start = String.valueOf(startTime + (long) PrometheusConstants.MAX_STEP_NUM * (i - 1));
                if (rangTime - (long) PrometheusConstants.MAX_STEP_NUM * (i - 1)
                        == rangTime % PrometheusConstants.MAX_STEP_NUM) {
                    List<?> data = (List<?>) prometheusUtil.rangeQuery(queryId, metric,
                        start, String.valueOf(endTime), PrometheusConstants.STEP);
                    for (Object object : data) {
                        if (object instanceof PrometheusData) {
                            partList.add((PrometheusData) object);
                        }
                    }
                } else {
                    partList = (List<PrometheusData>) prometheusUtil.rangeQuery(queryId, metric,
                        start, String.valueOf(startTime + (long) PrometheusConstants.MAX_STEP_NUM  * i), PrometheusConstants.STEP);
                }
                if (CollectionUtils.isEmpty(partList)) {
                    continue;
                }
                dataList.addAll(partList);
            }
        } else {
            dataList = (List<PrometheusData>) prometheusUtil.rangeQuery(queryId, metric,
                String.valueOf(task.getHisDataStartTime().getTime()/ PrometheusConstants.MS),
                String.valueOf(task.getHisDataEndTime().getTime()/ PrometheusConstants.MS),
                PrometheusConstants.STEP);
        }
        return dataList;
    }

    public Object queryData(HisDiagnosisTask task) {
        if (task.getHisDataEndTime() == null) {
            Date endDate = new Date();
            task.setHisDataEndTime(endDate);
        }
        String step;
        long startTime = task.getHisDataStartTime().getTime() / PrometheusConstants.MS;
        long endTime = task.getHisDataEndTime().getTime() / PrometheusConstants.MS;
        int slot = (int) ((endTime - startTime) / Integer.parseInt(PrometheusConstants.STEP));
        if (slot < PrometheusConstants.MIN_STEP_NUM) {
            step = PrometheusConstants.STEP;
        } else {
            step = String.valueOf(slot / PrometheusConstants.MIN_STEP_NUM);
        }
        String metric = getPrometheusParam(null);
        String queryId = getParamId(metric, task.getNodeId());
        return prometheusUtil.rangeQuery(queryId, metric,
            String.valueOf(startTime), String.valueOf(endTime), step);
    }

    abstract String getPrometheusParam(List<HisDiagnosisThreshold> thresholds);

    private String getParamId(String metric, String nodeId) {
        String queryId = null;
        if (metric.contains("history_diagnosis_nodeId")) {
            queryId = nodeId;
        } else if (metric.contains("history_diagnosis_hostId")) {
            queryId = hostUtil.getHostId(nodeId);
        }
        return queryId;
    }
}
