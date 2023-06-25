/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history.point;

import com.alibaba.fastjson.JSONArray;
import com.nctigba.common.web.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constants.history.MetricCommon;
import com.nctigba.observability.sql.constants.history.PrometheusConstants;
import com.nctigba.observability.sql.constants.history.SqlCommon;
import com.nctigba.observability.sql.constants.history.ThresholdCommon;
import com.nctigba.observability.sql.mapper.history.HisDiagnosisTaskMapper;
import com.nctigba.observability.sql.model.history.HisDiagnosisResult;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.HisDiagnosisThreshold;
import com.nctigba.observability.sql.model.history.data.DatabaseData;
import com.nctigba.observability.sql.model.history.data.PrometheusData;
import com.nctigba.observability.sql.model.history.dto.AnalysisDTO;
import com.nctigba.observability.sql.model.history.point.AspAnalysisDTO;
import com.nctigba.observability.sql.model.history.point.BusinessConnCountDTO;
import com.nctigba.observability.sql.model.history.point.MetricDataDTO;
import com.nctigba.observability.sql.model.history.point.PrometheusDataDTO;
import com.nctigba.observability.sql.service.history.DataStoreService;
import com.nctigba.observability.sql.service.history.HisDiagnosisPointService;
import com.nctigba.observability.sql.service.history.collection.CollectionItem;
import com.nctigba.observability.sql.service.history.collection.metric.DbAvgCpuItem;
import com.nctigba.observability.sql.util.DbUtil;
import com.nctigba.observability.sql.util.LocaleString;
import com.nctigba.observability.sql.util.PrometheusUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

/**
 * BusinessConnCount
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Service
public class BusinessConnCount implements HisDiagnosisPointService<Object> {
    @Autowired
    private DbAvgCpuItem dbAvgCpuItem;
    @Autowired
    private HisDiagnosisTaskMapper taskMapper;
    @Autowired
    private DbUtil dbUtil;
    @Autowired
    private PrometheusUtil util;

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
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        HashMap<String, String> map = new HashMap<>();
        List<HisDiagnosisThreshold> thresholds = task.getThresholds();
        for (HisDiagnosisThreshold threshold : thresholds) {
            if (threshold.getThreshold().equals(ThresholdCommon.ACTIVITY_NUM)) {
                map.put(threshold.getThreshold(), threshold.getThresholdValue());
            }
        }
        if (map.isEmpty()) {
            throw new HisDiagnosisException("fetch threshold data failed!");
        }
        List<?> proList = (List<?>) dataStoreService.getData(dbAvgCpuItem).getCollectionData();
        List<PrometheusData> prometheusDataList = new ArrayList<>();
        for (Object object : proList) {
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
            if (timeList.size() == 1) {
                AspAnalysisDTO dto = new AspAnalysisDTO();
                dto.setStartTime(simpleDateFormat.format(new Date(timeList.get(0) * PrometheusConstants.MS)));
                dto.setEndTime(simpleDateFormat.format(new Date(timeList.get(0) * PrometheusConstants.MS)));
                dtoList.add(dto);
            } else if (timeList.size() == 2) {
                if (timeList.get(0) + Integer.parseInt(PrometheusConstants.STEP) == timeList.get(1)) {
                    AspAnalysisDTO dto = new AspAnalysisDTO();
                    dto.setStartTime(simpleDateFormat.format(new Date(timeList.get(0) * PrometheusConstants.MS)));
                    dto.setEndTime(simpleDateFormat.format(new Date(timeList.get(1) * PrometheusConstants.MS)));
                    dtoList.add(dto);
                } else {
                    AspAnalysisDTO dto1 = new AspAnalysisDTO();
                    dto1.setStartTime(simpleDateFormat.format(new Date(timeList.get(0) * PrometheusConstants.MS)));
                    dto1.setEndTime(simpleDateFormat.format(new Date(timeList.get(0) * PrometheusConstants.MS)));
                    dtoList.add(dto1);
                    AspAnalysisDTO dto2 = new AspAnalysisDTO();
                    dto2.setStartTime(simpleDateFormat.format(new Date(timeList.get(1) * PrometheusConstants.MS)));
                    dto2.setEndTime(simpleDateFormat.format(new Date(timeList.get(1) * PrometheusConstants.MS)));
                    dtoList.add(dto2);
                }
            } else if (timeList.size() == 3) {
                if (timeList.get(0) + Integer.parseInt(PrometheusConstants.STEP) * 2 == timeList.get(2)) {
                    AspAnalysisDTO dto = new AspAnalysisDTO();
                    dto.setStartTime(simpleDateFormat.format(new Date(timeList.get(0) * PrometheusConstants.MS)));
                    dto.setEndTime(simpleDateFormat.format(new Date(timeList.get(2) * PrometheusConstants.MS)));
                    dtoList.add(dto);
                } else {
                    if (timeList.get(0) + Integer.parseInt(PrometheusConstants.STEP) == timeList.get(1)) {
                        AspAnalysisDTO dto1 = new AspAnalysisDTO();
                        dto1.setStartTime(simpleDateFormat.format(new Date(timeList.get(0) * PrometheusConstants.MS)));
                        dto1.setEndTime(simpleDateFormat.format(new Date(timeList.get(1) * PrometheusConstants.MS)));
                        dtoList.add(dto1);
                        AspAnalysisDTO dto2 = new AspAnalysisDTO();
                        dto2.setStartTime(simpleDateFormat.format(new Date(timeList.get(2) * PrometheusConstants.MS)));
                        dto2.setEndTime(simpleDateFormat.format(new Date(timeList.get(2) * PrometheusConstants.MS)));
                        dtoList.add(dto2);
                    } else if (timeList.get(1) + Integer.parseInt(PrometheusConstants.STEP) == timeList.get(2)) {
                        AspAnalysisDTO dto1 = new AspAnalysisDTO();
                        dto1.setStartTime(simpleDateFormat.format(new Date(timeList.get(0) * PrometheusConstants.MS)));
                        dto1.setEndTime(simpleDateFormat.format(new Date(timeList.get(0) * PrometheusConstants.MS)));
                        dtoList.add(dto1);
                        AspAnalysisDTO dto2 = new AspAnalysisDTO();
                        dto2.setStartTime(simpleDateFormat.format(new Date(timeList.get(1) * PrometheusConstants.MS)));
                        dto2.setEndTime(simpleDateFormat.format(new Date(timeList.get(2) * PrometheusConstants.MS)));
                        dtoList.add(dto2);
                    } else {
                        AspAnalysisDTO dto1 = new AspAnalysisDTO();
                        dto1.setStartTime(simpleDateFormat.format(new Date(timeList.get(0) * PrometheusConstants.MS)));
                        dto1.setEndTime(simpleDateFormat.format(new Date(timeList.get(0) * PrometheusConstants.MS)));
                        dtoList.add(dto1);
                        AspAnalysisDTO dto2 = new AspAnalysisDTO();
                        dto2.setStartTime(simpleDateFormat.format(new Date(timeList.get(1) * PrometheusConstants.MS)));
                        dto2.setEndTime(simpleDateFormat.format(new Date(timeList.get(1) * PrometheusConstants.MS)));
                        dtoList.add(dto2);
                        AspAnalysisDTO dto3 = new AspAnalysisDTO();
                        dto3.setStartTime(simpleDateFormat.format(new Date(timeList.get(2) * PrometheusConstants.MS)));
                        dto3.setEndTime(simpleDateFormat.format(new Date(timeList.get(2) * PrometheusConstants.MS)));
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
        }
        List<BusinessConnCountDTO> dataList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(dtoList)) {
            for (AspAnalysisDTO dto : dtoList) {
                Date sTime;
                Date eTime;
                Date preStartTime;
                Date preEndTime;
                try {
                    sTime = new Date(simpleDateFormat.parse(dto.getStartTime()).getTime());
                    eTime = new Date(simpleDateFormat.parse(dto.getEndTime()).getTime());
                    preStartTime = new Date((sTime.getTime() * PrometheusConstants.MS - 60));
                    preEndTime = sTime;
                } catch (ParseException e) {
                    throw new HisDiagnosisException("error:", e);
                }
                List<DatabaseData> preData = (List<DatabaseData>) dbUtil.rangQuery(SqlCommon.BUSINESS_CONN_COUNT,
                        preStartTime, preEndTime,
                        task.getNodeId());
                List<?> preDataList = (List<?>) preData.get(0).getValue().get(0);
                HashMap<String, String> hashMap = new HashMap<>();
                for (Object o1 : preDataList) {
                    if (o1 instanceof HashMap) {
                        hashMap.put("count", String.valueOf(((HashMap<?, ?>) o1).get("count")));
                        break;
                    }
                }
                List<DatabaseData> data = (List<DatabaseData>) dbUtil.rangQuery(SqlCommon.BUSINESS_CONN_COUNT, sTime,
                        eTime,
                        task.getNodeId());
                List<?> datasList = (List<?>) data.get(0).getValue().get(0);
                HashMap<String, String> hashMap1 = new HashMap<>();
                for (Object o2 : datasList) {
                    if (o2 instanceof HashMap) {
                        hashMap1.put("count", String.valueOf(((HashMap<?, ?>) o2).get("count")));
                        break;
                    }
                }
                int preCount = Integer.parseInt(hashMap.get("count"));
                int count = Integer.parseInt(hashMap1.get("count"));
                if (count - preCount > Integer.parseInt(map.get(ThresholdCommon.ACTIVITY_NUM))) {
                    analysisDTO.setIsHint(HisDiagnosisResult.ResultState.SUGGESTIONS);
                    BusinessConnCountDTO countDTO = new BusinessConnCountDTO();
                    countDTO.setNowStartTime(simpleDateFormat.format(sTime));
                    countDTO.setNowEndTime(simpleDateFormat.format(eTime));
                    countDTO.setNowSessionCount(String.valueOf(count));
                    countDTO.setBeforeStartTime(simpleDateFormat.format(preStartTime));
                    countDTO.setBeforeEndTime(simpleDateFormat.format(preEndTime));
                    countDTO.setBeforeSessionCount(String.valueOf(preCount));
                    dataList.add(countDTO);
                } else {
                    analysisDTO.setIsHint(HisDiagnosisResult.ResultState.NO_ADVICE);
                }
            }
        } else {
            analysisDTO.setIsHint(HisDiagnosisResult.ResultState.NO_ADVICE);
        }
        analysisDTO.setPointData(dataList);
        analysisDTO.setPointType(HisDiagnosisResult.PointType.DIAGNOSIS);
        return analysisDTO;
    }

    @Override
    public List<?> getShowData(int taskId) {
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
