/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history.point;

import com.alibaba.fastjson.JSONArray;
import com.nctigba.common.web.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constants.history.MetricCommon;
import com.nctigba.observability.sql.constants.history.PrometheusConstants;
import com.nctigba.observability.sql.mapper.history.HisDiagnosisTaskMapper;
import com.nctigba.observability.sql.model.history.HisDiagnosisResult;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.data.PrometheusData;
import com.nctigba.observability.sql.model.history.dto.AnalysisDTO;
import com.nctigba.observability.sql.model.history.point.AspAnalysisDTO;
import com.nctigba.observability.sql.model.history.point.MetricDataDTO;
import com.nctigba.observability.sql.model.history.point.PrometheusDataDTO;
import com.nctigba.observability.sql.service.history.DataStoreService;
import com.nctigba.observability.sql.service.history.HisDiagnosisPointService;
import com.nctigba.observability.sql.service.history.collection.CollectionItem;
import com.nctigba.observability.sql.service.history.collection.metric.DbAvgCpuItem;
import com.nctigba.observability.sql.util.LocaleString;
import com.nctigba.observability.sql.util.PrometheusUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * WdrAnalysis
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Service
public class WdrAnalysis implements HisDiagnosisPointService<List<PrometheusDataDTO>> {
    @Autowired
    private PrometheusUtil util;
    @Autowired
    private HisDiagnosisTaskMapper taskMapper;
    @Autowired
    private DbAvgCpuItem dbAvgCpuItem;

    @Override
    public List<String> getOption() {
        return null;
    }


    @Override
    public List<CollectionItem<?>> getSourceDataKeys() {
        List<CollectionItem<?>> list = new ArrayList<>();
        list.add(dbAvgCpuItem);
        return list;
    }

    @Override
    public AnalysisDTO analysis(HisDiagnosisTask task, DataStoreService dataStoreService) {
        AnalysisDTO analysisDTO = new AnalysisDTO();
        List<?> list = (List<?>) dataStoreService.getData(dbAvgCpuItem).getCollectionData();
        if (CollectionUtils.isEmpty(list)) {
            analysisDTO.setIsHint(HisDiagnosisResult.ResultState.NO_ADVICE);
            analysisDTO.setPointType(HisDiagnosisResult.PointType.CENTER);
            return analysisDTO;
        }
        List<PrometheusData> prometheusDataList = new ArrayList<>();
        for (Object object : list) {
            if (object instanceof PrometheusData) {
                prometheusDataList.add((PrometheusData) object);
            }
        }
        List<AspAnalysisDTO> dtoList = new ArrayList<>();
        if (CollectionUtils.isEmpty(prometheusDataList)) {
            analysisDTO.setIsHint(HisDiagnosisResult.ResultState.NO_ADVICE);
        } else {
            PrometheusData prometheusData = prometheusDataList.get(0);
            JSONArray values = prometheusData.getValues();
            List<Integer> timeList = new ArrayList<>();
            for (Object value : values) {
                JSONArray timeJson = new JSONArray();
                if (value instanceof JSONArray) {
                    timeJson = (JSONArray) value;
                }
                Object time = timeJson.get(0);
                timeList.add(Integer.parseInt(time.toString()));
            }
            int minute = PrometheusConstants.MINUTE;
            int mCount = minute / Integer.parseInt(PrometheusConstants.STEP);
            int hCount = minute * mCount;
            if (timeList.size() > hCount) {
                for (int i = 0; i <= timeList.size() - mCount * minute; i = i + hCount) {
                    int step = Integer.parseInt(PrometheusConstants.STEP);
                    int count = minute / step * minute;
                    AspAnalysisDTO dto = new AspAnalysisDTO();
                    int startTime = timeList.get(i);
                    int realityTime = timeList.get(i + count);
                    int expectTime = startTime + step * (count);
                    if (realityTime == expectTime) {
                        while (timeList.size() - count > 0) {
                            if (timeList.get(i + count) > startTime + step * (count)) {
                                int endTime = timeList.get(i + count);
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
                                dto.setStartTime(simpleDateFormat.format(new Date(startTime * PrometheusConstants.MS)));
                                dto.setEndTime(simpleDateFormat.format(new Date(endTime * PrometheusConstants.MS)));
                                dtoList.add(dto);
                                break;
                            }
                            count++;
                        }
                    }
                }
            }
            if (!CollectionUtils.isEmpty(dtoList)) {
                analysisDTO.setIsHint(HisDiagnosisResult.ResultState.SUGGESTIONS);
            } else {
                analysisDTO.setIsHint(HisDiagnosisResult.ResultState.NO_ADVICE);
            }
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
            list.forEach(data -> {
                if (data instanceof PrometheusData) {
                    prometheusDataList.add((PrometheusData) data);
                }
            });
            if (CollectionUtils.isEmpty(prometheusDataList)) {
                continue;
            }
            PrometheusDataDTO metricList = util.metricToLine(prometheusDataList);
            dataList.add(metricList);
        }
        for (PrometheusDataDTO dto : dataList) {
            if (dto.getChartName() != null && dto.getChartName().equals(MetricCommon.DB_AVG_CPU_USAGE_RATE)) {
                dto.setUnit("%");
                dto.setChartName(LocaleString.format("history.DB_AVG_CPU_USAGE_RATE.metric"));
                List<MetricDataDTO> datas = dto.getDatas();
                for (MetricDataDTO dataDTO : datas) {
                    if (dataDTO.getName() != null && dataDTO.getName().equals(MetricCommon.DB_AVG_CPU_USAGE_RATE)) {
                        dataDTO.setName(LocaleString.format("history.DB_AVG_CPU_USAGE_RATE.metric"));
                    }
                }
            }
        }
        return dataList;
    }
}