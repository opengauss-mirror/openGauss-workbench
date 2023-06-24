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
 * AspAnalysis
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Service
public class AspAnalysis implements HisDiagnosisPointService<List<PrometheusDataDTO>> {
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
        List<?> list = (List<?>) dataStoreService.getData(dbAvgCpuItem).getCollectionData();
        List<PrometheusData> prometheusDataList = new ArrayList<>();
        for (Object object : list) {
            if (object instanceof PrometheusData) {
                prometheusDataList.add((PrometheusData) object);
            }
        }
        List<AspAnalysisDTO> dtoList = new ArrayList<>();
        AnalysisDTO analysisDTO = new AnalysisDTO();
        if (CollectionUtils.isEmpty(prometheusDataList)) {
            analysisDTO.setIsHint(HisDiagnosisResult.ResultState.NO_ADVICE);
        } else {
            List<Integer> timeList = new ArrayList<>();
            for (PrometheusData data : prometheusDataList) {
                JSONArray values = data.getValues();
                for (Object value : values) {
                    JSONArray timeJson = new JSONArray();
                    if (value instanceof JSONArray) {
                        timeJson = (JSONArray) value;
                    }
                    Object time = timeJson.get(0);
                    timeList.add(Integer.parseInt(time.toString()));
                }
            }
            int minute = PrometheusConstants.MINUTE;
            long ms = PrometheusConstants.MS;
            int mCount = minute / Integer.parseInt(PrometheusConstants.STEP) - 1;
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
            if (timeList.size() == 1) {
                AspAnalysisDTO dto = new AspAnalysisDTO();
                dto.setStartTime(simpleDateFormat.format(new Date(timeList.get(0) * ms)));
                dto.setEndTime(simpleDateFormat.format(new Date(timeList.get(0) * ms)));
                dtoList.add(dto);
            } else if (timeList.size() == 2) {
                if (timeList.get(0) + Integer.parseInt(PrometheusConstants.STEP) == timeList.get(1)) {
                    AspAnalysisDTO dto = new AspAnalysisDTO();
                    dto.setStartTime(simpleDateFormat.format(new Date(timeList.get(0) * ms)));
                    dto.setEndTime(simpleDateFormat.format(new Date(timeList.get(1) * ms)));
                    dtoList.add(dto);
                } else {
                    AspAnalysisDTO dto1 = new AspAnalysisDTO();
                    dto1.setStartTime(simpleDateFormat.format(new Date(timeList.get(0) * ms)));
                    dto1.setEndTime(simpleDateFormat.format(new Date(timeList.get(0) * ms)));
                    dtoList.add(dto1);
                    AspAnalysisDTO dto2 = new AspAnalysisDTO();
                    dto2.setStartTime(simpleDateFormat.format(new Date(timeList.get(1) * ms)));
                    dto2.setEndTime(simpleDateFormat.format(new Date(timeList.get(1) * ms)));
                    dtoList.add(dto2);
                }
            } else if (timeList.size() == 3) {
                if (timeList.get(0) + Integer.parseInt(PrometheusConstants.STEP) * 2 == timeList.get(2)) {
                    AspAnalysisDTO dto = new AspAnalysisDTO();
                    dto.setStartTime(simpleDateFormat.format(new Date(timeList.get(0) * ms)));
                    dto.setEndTime(simpleDateFormat.format(new Date(timeList.get(2) * ms)));
                    dtoList.add(dto);
                } else {
                    if (timeList.get(0) + Integer.parseInt(PrometheusConstants.STEP) == timeList.get(1)) {
                        AspAnalysisDTO dto1 = new AspAnalysisDTO();
                        dto1.setStartTime(simpleDateFormat.format(new Date(timeList.get(0) * ms)));
                        dto1.setEndTime(simpleDateFormat.format(new Date(timeList.get(1) * ms)));
                        dtoList.add(dto1);
                        AspAnalysisDTO dto2 = new AspAnalysisDTO();
                        dto2.setStartTime(simpleDateFormat.format(new Date(timeList.get(2) * ms)));
                        dto2.setEndTime(simpleDateFormat.format(new Date(timeList.get(2) * ms)));
                        dtoList.add(dto2);
                    } else if (timeList.get(1) + Integer.parseInt(PrometheusConstants.STEP) == timeList.get(2)) {
                        AspAnalysisDTO dto1 = new AspAnalysisDTO();
                        dto1.setStartTime(simpleDateFormat.format(new Date(timeList.get(0) * ms)));
                        dto1.setEndTime(simpleDateFormat.format(new Date(timeList.get(0) * ms)));
                        dtoList.add(dto1);
                        AspAnalysisDTO dto2 = new AspAnalysisDTO();
                        dto2.setStartTime(simpleDateFormat.format(new Date(timeList.get(1) * ms)));
                        dto2.setEndTime(simpleDateFormat.format(new Date(timeList.get(2) * ms)));
                        dtoList.add(dto2);
                    } else {
                        AspAnalysisDTO dto1 = new AspAnalysisDTO();
                        dto1.setStartTime(simpleDateFormat.format(new Date(timeList.get(0) * ms)));
                        dto1.setEndTime(simpleDateFormat.format(new Date(timeList.get(0) * ms)));
                        dtoList.add(dto1);
                        AspAnalysisDTO dto2 = new AspAnalysisDTO();
                        dto2.setStartTime(simpleDateFormat.format(new Date(timeList.get(1) * ms)));
                        dto2.setEndTime(simpleDateFormat.format(new Date(timeList.get(1) * ms)));
                        dtoList.add(dto2);
                        AspAnalysisDTO dto3 = new AspAnalysisDTO();
                        dto3.setStartTime(simpleDateFormat.format(new Date(timeList.get(2) * ms)));
                        dto3.setEndTime(simpleDateFormat.format(new Date(timeList.get(2) * ms)));
                        dtoList.add(dto3);
                    }
                }
            } else {
                int cursor = 0;
                for (int i = 0; i < timeList.size() - 3; i = cursor) {
                    int step = Integer.parseInt(PrometheusConstants.STEP);
                    int count = minute / step - 1;
                    int startTime = timeList.get(i);
                    int realityTime = timeList.get(i + count);
                    int expectTime = startTime + step * (count);
                    int temp = 0;
                    if (realityTime == expectTime) {
                        for (int n = i + count; n < timeList.size() - i - count; n++) {
                            int realityValue = timeList.get(n);
                            int expectValue = startTime + step * (n - i);
                            if (realityValue > expectValue) {
                                cursor = n;
                                temp++;
                                break;
                            }
                        }
                    }
                    if (temp > 0) {
                        continue;
                    } else {
                        cursor = i + mCount;
                    }
                    AspAnalysisDTO dto = new AspAnalysisDTO();
                    if (realityTime > expectTime) {
                        while (count > 0) {
                            if (timeList.get(i + count) == startTime + step * (count)) {
                                int entTime = timeList.get(i + count);
                                dto.setStartTime(simpleDateFormat.format(new Date(startTime * ms)));
                                dto.setEndTime(simpleDateFormat.format(new Date(entTime * ms)));
                                dtoList.add(dto);
                                break;
                            }
                            count--;
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
        analysisDTO.setPointData(dtoList);
        analysisDTO.setPointType(HisDiagnosisResult.PointType.DIAGNOSIS);
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
