/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history.point;

import com.nctigba.common.web.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constants.history.MetricCommon;
import com.nctigba.observability.sql.mapper.history.HisDiagnosisTaskMapper;
import com.nctigba.observability.sql.model.history.HisDiagnosisResult;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.data.PrometheusData;
import com.nctigba.observability.sql.model.history.dto.AnalysisDTO;
import com.nctigba.observability.sql.model.history.point.MetricDataDTO;
import com.nctigba.observability.sql.model.history.point.PrometheusDataDTO;
import com.nctigba.observability.sql.service.history.DataStoreService;
import com.nctigba.observability.sql.service.history.HisDiagnosisPointService;
import com.nctigba.observability.sql.service.history.collection.CollectionItem;
import com.nctigba.observability.sql.service.history.collection.metric.ActivityNumItem2;
import com.nctigba.observability.sql.service.history.collection.metric.ThreadPoolUsageItem2;
import com.nctigba.observability.sql.util.LocaleString;
import com.nctigba.observability.sql.util.PrometheusUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * DatabaseHostLowPressure
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Service
public class DatabaseHostLowPressure implements HisDiagnosisPointService<List<PrometheusDataDTO>> {
    @Autowired
    private PrometheusUtil util;
    @Autowired
    private HisDiagnosisTaskMapper taskMapper;
    @Autowired
    private ActivityNumItem2 activityNumItem;
    @Autowired
    private ThreadPoolUsageItem2 threadPoolUsageItem;

    @Override
    public List<String> getOption() {
        return null;
    }


    @Override
    public List<CollectionItem<?>> getSourceDataKeys() {
        List<CollectionItem<?>> list = new ArrayList<>();
        list.add(activityNumItem);
        list.add(threadPoolUsageItem);
        return list;
    }

    @Override
    public AnalysisDTO analysis(HisDiagnosisTask task, DataStoreService dataStoreService) {
        AnalysisDTO analysisDTO = new AnalysisDTO();
        int count = 0;
        for (CollectionItem<?> item : getSourceDataKeys()) {
            List<?> list = (List<?>) dataStoreService.getData(item).getCollectionData();
            if (!CollectionUtils.isEmpty(list)) {
                count++;
            }
        }
        if (count == 2) {
            analysisDTO.setIsHint(HisDiagnosisResult.ResultState.SUGGESTIONS);
        } else {
            analysisDTO.setIsHint(HisDiagnosisResult.ResultState.NO_ADVICE);
        }
        analysisDTO.setPointType(HisDiagnosisResult.PointType.CENTER);
        return analysisDTO;
    }

    @Override
    public List<PrometheusDataDTO> getShowData(int taskId) {
        HisDiagnosisTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new HisDiagnosisException("taskId is not exists!");
        }
        List<PrometheusDataDTO> dataList = new ArrayList<>();
        for (CollectionItem<?> item : getSourceDataKeys()) {
            List<?> list = (List<?>) item.queryData(task);
            List<PrometheusData> prometheusDataList = new ArrayList<>();
            for (Object object : list) {
                if (object instanceof PrometheusData) {
                    prometheusDataList.add((PrometheusData) object);
                }
            }
            if (CollectionUtils.isEmpty(prometheusDataList)) {
                continue;
            }
            PrometheusDataDTO metricList = util.metricToLine(prometheusDataList);
            dataList.add(metricList);
        }
        for (PrometheusDataDTO dto : dataList) {
            if (dto.getChartName() != null && dto.getChartName().equals(MetricCommon.ACTIVITY_NUM)) {
                dto.setUnit("pcs");
                dto.setChartName(LocaleString.format("history.ACTIVITY_NUM.metric"));
            } else if (dto.getChartName() != null && dto.getChartName().equals(MetricCommon.THREAD_POOL_USAGE_RATE)) {
                dto.setUnit("%");
                dto.setChartName(LocaleString.format("history.THREAD_POOL_USAGE_RATE.metric"));
            } else {
                dto.setUnit(null);
                dto.setChartName(null);
            }
            List<MetricDataDTO> datas = dto.getDatas();
            for (MetricDataDTO dataDTO : datas) {
                if (dataDTO.getName() != null && dataDTO.getName().equals(MetricCommon.ACTIVITY_NUM)) {
                    dataDTO.setName(LocaleString.format("history.ACTIVITY_NUM.metric"));
                } else if (dataDTO.getName() != null && dataDTO.getName().equals(MetricCommon.THREAD_POOL_USAGE_RATE)) {
                    dataDTO.setName(LocaleString.format("history.THREAD_POOL_USAGE_RATE.metric"));
                } else {
                    dataDTO.setName(null);
                }
            }
        }
        return dataList;
    }
}
