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
 *  PrometheusCollectionItem.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/collection/metric/PrometheusCollectionItem.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl.collection.metric;

import com.nctigba.observability.sql.constant.PrometheusConstants;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.entity.DiagnosisThresholdDO;
import com.nctigba.observability.sql.model.vo.collection.PrometheusVO;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.util.HostUtils;
import com.nctigba.observability.sql.util.PrometheusUtils;
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
    private PrometheusUtils prometheusUtils;
    @Autowired
    private HostUtils hostUtils;

    public Object collectData(DiagnosisTaskDO task) {
        if (task.getHisDataEndTime() == null) {
            Date endDate = new Date();
            task.setHisDataEndTime(endDate);
        }
        List<DiagnosisThresholdDO> thresholds = task.getThresholds();
        String metric = getPrometheusParam(thresholds);
        String queryId = getParamId(metric, task.getNodeId());
        long startTime = task.getHisDataStartTime().getTime() / PrometheusConstants.MS;
        long endTime = task.getHisDataEndTime().getTime() / PrometheusConstants.MS;
        long rangTime = endTime - startTime;
        List<PrometheusVO> dataList = new ArrayList<>();
        if (endTime - startTime > PrometheusConstants.MAX_STEP_NUM) {
            for (int i = 1; i <= rangTime / PrometheusConstants.MAX_STEP_NUM + 1; i++) {
                List<PrometheusVO> partList = new ArrayList<>();
                String start = String.valueOf(startTime + (long) PrometheusConstants.MAX_STEP_NUM * (i - 1));
                if (rangTime - (long) PrometheusConstants.MAX_STEP_NUM * (i - 1)
                        == rangTime % PrometheusConstants.MAX_STEP_NUM) {
                    List<?> data = (List<?>) prometheusUtils.rangeQuery(
                            queryId, metric, start, String.valueOf(endTime), PrometheusConstants.STEP);
                    for (Object object : data) {
                        if (object instanceof PrometheusVO) {
                            partList.add((PrometheusVO) object);
                        }
                    }
                } else {
                    partList = (List<PrometheusVO>) prometheusUtils.rangeQuery(queryId, metric, start, String.valueOf(
                            startTime + (long) PrometheusConstants.MAX_STEP_NUM * i), PrometheusConstants.STEP);
                }
                if (CollectionUtils.isEmpty(partList)) {
                    continue;
                }
                dataList.addAll(partList);
            }
        } else {
            dataList = (List<PrometheusVO>) prometheusUtils.rangeQuery(
                    queryId, metric, String.valueOf(task.getHisDataStartTime().getTime() / PrometheusConstants.MS),
                    String.valueOf(task.getHisDataEndTime().getTime() / PrometheusConstants.MS),
                    PrometheusConstants.STEP);
        }
        return dataList;
    }

    public Object queryData(DiagnosisTaskDO task) {
        if (task.getHisDataEndTime() == null) {
            Date endDate = new Date();
            task.setHisDataEndTime(endDate);
        }
        String step;
        long startTime = task.getHisDataStartTime().getTime() / PrometheusConstants.MS;
        long endTime = task.getHisDataEndTime().getTime() / PrometheusConstants.MS;
        int slot = (int) (endTime - startTime);
        if (slot < PrometheusConstants.MAX_STEP_NUM) {
            step = PrometheusConstants.STEP;
        } else {
            step = String.valueOf(slot / PrometheusConstants.MIN_STEP_NUM);
        }
        String metric = getPrometheusParam(null);
        String queryId = getParamId(metric, task.getNodeId());
        return prometheusUtils.rangeQuery(queryId, metric, String.valueOf(startTime), String.valueOf(endTime), step);
    }

    /**
     * Get prometheus param
     *
     * @param thresholds Request parameters
     * @return String
     */
    public abstract String getPrometheusParam(List<DiagnosisThresholdDO> thresholds);

    private String getParamId(String metric, String nodeId) {
        String queryId = null;
        String hostId = hostUtils.getHostId(nodeId);
        if (metric.contains("history_diagnosis_nodeId") && metric.contains("history_diagnosis_hostId")) {
            queryId = nodeId + "," + hostId;
        } else if (metric.contains("history_diagnosis_nodeId")) {
            queryId = nodeId;
        } else if (metric.contains("history_diagnosis_hostId")) {
            queryId = hostId;
        }
        return queryId;
    }
}
