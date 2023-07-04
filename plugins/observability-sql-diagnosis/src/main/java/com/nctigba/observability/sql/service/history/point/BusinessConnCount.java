/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history.point;

import com.nctigba.common.web.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constants.history.MetricCommon;
import com.nctigba.observability.sql.constants.history.PrometheusConstants;
import com.nctigba.observability.sql.constants.history.ThresholdCommon;
import com.nctigba.observability.sql.mapper.history.HisDiagnosisTaskMapper;
import com.nctigba.observability.sql.model.history.HisDiagnosisResult;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.data.PrometheusData;
import com.nctigba.observability.sql.model.history.dto.AnalysisDTO;
import com.nctigba.observability.sql.model.history.dto.ConnCountDTO;
import com.nctigba.observability.sql.model.history.point.AspAnalysisDTO;
import com.nctigba.observability.sql.model.history.point.BusinessConnCountDTO;
import com.nctigba.observability.sql.model.history.point.MetricDataDTO;
import com.nctigba.observability.sql.model.history.point.PrometheusDataDTO;
import com.nctigba.observability.sql.service.history.DataStoreService;
import com.nctigba.observability.sql.service.history.HisDiagnosisPointService;
import com.nctigba.observability.sql.service.history.collection.CollectionItem;
import com.nctigba.observability.sql.service.history.collection.metric.BusinessConnCountItem;
import com.nctigba.observability.sql.service.history.collection.metric.DbAvgCpuItem;
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
    private BusinessConnCountItem countItem;
    @Autowired
    private HisDiagnosisTaskMapper taskMapper;
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
        list.add(countItem);
        return list;
    }

    @Override
    public AnalysisDTO analysis(HisDiagnosisTask task, DataStoreService dataStoreService) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        HashMap<String, String> map = pointUtil.thresholdMap(task.getThresholds());
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
            dtoList = pointUtil.aspTimeSlot(prometheusDataList);
        }
        List<ConnCountDTO> connCount = new ArrayList<>();
        if (!CollectionUtils.isEmpty(dtoList)) {
            for (AspAnalysisDTO dto : dtoList) {
                Date sTime;
                Date eTime;
                Date preStartTime;
                Date preEndTime;
                try {
                    sTime = new Date(simpleDateFormat.parse(dto.getStartTime()).getTime());
                    eTime = new Date(simpleDateFormat.parse(dto.getEndTime()).getTime());
                    preEndTime = new Date(
                            (sTime.getTime() - Integer.parseInt(PrometheusConstants.STEP) * PrometheusConstants.MS));
                    long slot = (eTime.getTime() - sTime.getTime()) / PrometheusConstants.MS;
                    if (slot == 0) {
                        preStartTime = preEndTime;
                    } else {
                        preStartTime = new Date((sTime.getTime() - slot * PrometheusConstants.MS));
                    }
                } catch (ParseException e) {
                    throw new HisDiagnosisException("error:", e);
                }
                HisDiagnosisTask queryTask = new HisDiagnosisTask();
                queryTask.setNodeId(task.getNodeId());
                queryTask.setHisDataEndTime(preEndTime);
                queryTask.setHisDataStartTime(preStartTime);
                List<?> preObjectData = (List<?>) countItem.queryData(queryTask);
                int avgPreCount = 0;
                if (!CollectionUtils.isEmpty(preObjectData)) {
                    List<PrometheusData> preData = new ArrayList<>();
                    for (Object object : preObjectData) {
                        if (object instanceof PrometheusData) {
                            preData.add((PrometheusData) object);
                        }
                    }
                    List<?> preDataList = preData.get(0).getValues();
                    int preCount = 0;
                    for (Object o1 : preDataList) {
                        if (o1 instanceof List) {
                            preCount += Integer.parseInt(String.valueOf(((List<?>) o1).get(1)));
                        }
                    }
                    avgPreCount = preCount / preDataList.size();
                }
                List<?> objectData = (List<?>) countItem.queryData(queryTask);
                int avgCount = 0;
                if (!CollectionUtils.isEmpty(objectData)) {
                    List<PrometheusData> data = new ArrayList<>();
                    for (Object object : objectData) {
                        if (object instanceof PrometheusData) {
                            data.add((PrometheusData) object);
                        }
                    }
                    List<?> datasList = data.get(0).getValues();
                    int count = 0;
                    for (Object o1 : datasList) {
                        if (o1 instanceof List) {
                            count += Integer.parseInt(String.valueOf(((List<?>) o1).get(1)));
                        }
                    }
                    avgCount = count / datasList.size();
                }
                if (avgCount - avgPreCount > Integer.parseInt(
                        map.get(ThresholdCommon.CONNECTION_NUM))) {
                    analysisDTO.setIsHint(HisDiagnosisResult.ResultState.SUGGESTIONS);
                    ConnCountDTO countDTO = new ConnCountDTO();
                    countDTO.setNowStartTime(simpleDateFormat.format(sTime));
                    countDTO.setNowEndTime(simpleDateFormat.format(eTime));
                    countDTO.setNowSessionCount(String.valueOf(avgCount));
                    countDTO.setBeforeStartTime(simpleDateFormat.format(preStartTime));
                    countDTO.setBeforeEndTime(simpleDateFormat.format(preEndTime));
                    countDTO.setBeforeSessionCount(String.valueOf(avgPreCount));
                    connCount.add(countDTO);
                } else {
                    analysisDTO.setIsHint(HisDiagnosisResult.ResultState.NO_ADVICE);
                }
            }
        } else {
            analysisDTO.setIsHint(HisDiagnosisResult.ResultState.NO_ADVICE);
        }
        BusinessConnCountDTO businessConnCountDTO = new BusinessConnCountDTO();
        businessConnCountDTO.setConnCount(connCount);
        businessConnCountDTO.setTimeSlot(dtoList);
        analysisDTO.setPointData(businessConnCountDTO);
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
            List<PrometheusData> prometheusDataList = pointUtil.dataToObject(list);
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
            } else if (dto.getChartName() != null && dto.getChartName().equals(MetricCommon.BUSINESS_CONN_COUNT)) {
                dto.setUnit("pcs");
                dto.setChartName(LocaleString.format("history.BUSINESS_CONN_COUNT.metric"));
            } else {
                dto.setUnit(null);
                dto.setChartName(null);
            }
            List<MetricDataDTO> datas = dto.getDatas();
            for (MetricDataDTO dataDTO : datas) {
                if (dataDTO.getName() != null && dataDTO.getName().equals(MetricCommon.DB_AVG_CPU_USAGE_RATE)) {
                    dataDTO.setName(LocaleString.format("history.DB_AVG_CPU_USAGE_RATE.metric"));
                } else if (dataDTO.getName() != null && dataDTO.getName().equals(MetricCommon.BUSINESS_CONN_COUNT)) {
                    dataDTO.setName(LocaleString.format("history.BUSINESS_CONN_COUNT.metric"));
                } else {
                    dataDTO.setName(null);
                }
            }
        }
        return dataList;
    }
}
