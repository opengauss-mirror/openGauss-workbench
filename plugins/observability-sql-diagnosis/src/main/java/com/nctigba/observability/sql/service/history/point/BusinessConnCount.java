/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history.point;

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
import com.nctigba.observability.sql.util.PointUtil;
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
    private PointUtil pointUtil;
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
        AnalysisDTO analysisDTO = new AnalysisDTO();
        if (CollectionUtils.isEmpty(proList)) {
            analysisDTO.setIsHint(HisDiagnosisResult.ResultState.NO_ADVICE);
            analysisDTO.setPointType(HisDiagnosisResult.PointType.DIAGNOSIS);
            return analysisDTO;
        }
        List<PrometheusData> prometheusDataList = new ArrayList<>();
        for (Object object : proList) {
            if (object instanceof PrometheusData) {
                prometheusDataList.add((PrometheusData) object);
            }
        }
        List<AspAnalysisDTO> dtoList = new ArrayList<>();
        if (CollectionUtils.isEmpty(prometheusDataList)) {
            analysisDTO.setIsHint(HisDiagnosisResult.ResultState.NO_ADVICE);
        } else {
            dtoList = pointUtil.getAspTimeSlot(prometheusDataList);
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
                Object preObjectData = dbUtil.rangQuery(SqlCommon.BUSINESS_CONN_COUNT,
                                                        preStartTime, preEndTime,
                                                        task.getNodeId());
                List<DatabaseData> preData = new ArrayList<>();
                if (preObjectData != null) {
                    List<?> preDataList = (List<?>) preObjectData;
                    for (Object object : preDataList) {
                        if (object instanceof DatabaseData) {
                            preData.add((DatabaseData) object);
                        }
                    }
                }
                List<?> preDataList = (List<?>) preData.get(0).getValue().get(0);
                HashMap<String, String> hashMap = new HashMap<>();
                for (Object o1 : preDataList) {
                    if (o1 instanceof HashMap) {
                        hashMap.put("count", String.valueOf(((HashMap<?, ?>) o1).get("count")));
                        break;
                    }
                }
                Object objectData = dbUtil.rangQuery(SqlCommon.BUSINESS_CONN_COUNT, sTime,
                                                     eTime,
                                                     task.getNodeId());
                List<DatabaseData> data = new ArrayList<>();
                if (objectData != null) {
                    List<?> datas = (List<?>) objectData;
                    for (Object object : datas) {
                        if (object instanceof DatabaseData) {
                            data.add((DatabaseData) object);
                        }
                    }
                }
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
