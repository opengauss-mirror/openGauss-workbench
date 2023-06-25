/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history.point;

import com.nctigba.common.web.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constants.history.ThresholdCommon;
import com.nctigba.observability.sql.mapper.history.HisDiagnosisTaskMapper;
import com.nctigba.observability.sql.model.history.HisDiagnosisResult;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.HisDiagnosisThreshold;
import com.nctigba.observability.sql.model.history.data.PrometheusData;
import com.nctigba.observability.sql.model.history.dto.AnalysisDTO;
import com.nctigba.observability.sql.model.history.dto.MetricToTableDTO;
import com.nctigba.observability.sql.model.history.dto.WaitEventInfo;
import com.nctigba.observability.sql.model.history.point.WaitEventDTO;
import com.nctigba.observability.sql.service.history.DataStoreService;
import com.nctigba.observability.sql.service.history.HisDiagnosisPointService;
import com.nctigba.observability.sql.service.history.collection.CollectionItem;
import com.nctigba.observability.sql.service.history.collection.metric.WaitEventItem;
import com.nctigba.observability.sql.util.ComparatorUtil;
import com.nctigba.observability.sql.util.LocaleString;
import com.nctigba.observability.sql.util.PrometheusUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * HistoryWaitEvent
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Service
public class HistoryWaitEvent implements HisDiagnosisPointService<WaitEventDTO> {
    @Autowired
    private PrometheusUtil util;
    @Autowired
    private HisDiagnosisTaskMapper taskMapper;
    @Autowired
    private WaitEventItem item;

    @Override
    public List<String> getOption() {
        return null;
    }


    @Override
    public List<CollectionItem<?>> getSourceDataKeys() {
        List<CollectionItem<?>> list = new ArrayList<>();
        list.add(item);
        return list;
    }

    @Override
    public AnalysisDTO analysis(HisDiagnosisTask task, DataStoreService dataStoreService) {
        HashMap<String, String> map = new HashMap<>();
        List<HisDiagnosisThreshold> thresholds = task.getThresholds();
        for (HisDiagnosisThreshold threshold : thresholds) {
            if (threshold.getThreshold().equals(ThresholdCommon.WAIT_EVENT_NUM)) {
                map.put(threshold.getThreshold(), threshold.getThresholdValue());
            }
        }
        if (map.isEmpty()) {
            throw new HisDiagnosisException("fetch threshold data failed!");
        }
        List<?> list = (List<?>) dataStoreService.getData(item).getCollectionData();
        List<PrometheusData> prometheusDataList = new ArrayList<>();
        for (Object object : list) {
            if (object instanceof PrometheusData) {
                prometheusDataList.add((PrometheusData) object);
            }
        }
        AnalysisDTO analysisDTO = new AnalysisDTO();
        if (CollectionUtils.isEmpty(prometheusDataList)) {
            analysisDTO.setIsHint(HisDiagnosisResult.ResultState.NO_ADVICE);
        } else {
            List<MetricToTableDTO> dtoList = util.metricToTable(prometheusDataList);
            int count = 0;
            long range = (task.getHisDataEndTime().getTime() - task.getHisDataStartTime().getTime()) / 1000 / 15;
            for (MetricToTableDTO dto : dtoList) {
                count += Integer.parseInt(dto.getValue());
            }
            if (count / range > Integer.parseInt(map.get(ThresholdCommon.WAIT_EVENT_NUM))) {
                analysisDTO.setIsHint(HisDiagnosisResult.ResultState.SUGGESTIONS);
            } else {
                analysisDTO.setIsHint(HisDiagnosisResult.ResultState.NO_ADVICE);
            }
        }
        analysisDTO.setPointType(HisDiagnosisResult.PointType.DIAGNOSIS);
        return analysisDTO;
    }

    @Override
    public WaitEventDTO getShowData(int taskId) {
        HisDiagnosisTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new HisDiagnosisException("taskId is not exists!");
        }
        List<MetricToTableDTO> dtoList = new ArrayList<>();
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
            dtoList = util.metricToTable(prometheusDataList);
        }
        List<WaitEventInfo> dataList = new ArrayList<>();
        for (MetricToTableDTO dto : dtoList) {
            if (CollectionUtils.isEmpty(dataList)) {
                WaitEventInfo waitEventInfo = new WaitEventInfo();
                waitEventInfo.setEventName(dto.getName());
                waitEventInfo.setEventCount(Integer.parseInt(dto.getValue()));
                waitEventInfo.setEventDetail(
                        LocaleString.format("history.WAIT_EVENT." + dto.getName().replace(" ", "") + ".detail"));
                waitEventInfo.setSuggestion(
                        LocaleString.format("history.WAIT_EVENT." + dto.getName().replace(" ", "") + ".suggest"));
                dataList.add(waitEventInfo);
            } else {
                List<WaitEventInfo> isExist = dataList.stream().filter(
                        f -> f.getEventName().equals(dto.getName())).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(isExist)) {
                    WaitEventInfo waitEventInfo = new WaitEventInfo();
                    waitEventInfo.setEventName(dto.getName());
                    waitEventInfo.setEventCount(Integer.parseInt(dto.getValue()));
                    String fieldName;
                    if (dto.getName().contains(":")) {
                        fieldName = dto.getName().substring(0, dto.getName().indexOf(":")).replace(" ", "");
                    } else {
                        fieldName = dto.getName().replace(" ", "");
                    }
                    waitEventInfo.setEventDetail(LocaleString.format("history.WAIT_EVENT." + fieldName + ".detail"));
                    waitEventInfo.setSuggestion(LocaleString.format("history.WAIT_EVENT." + fieldName + ".suggest"));
                    dataList.add(waitEventInfo);
                } else {
                    dataList.forEach(f -> {
                        if (f.getEventName().equals(dto.getName())) {
                            f.setEventCount(f.getEventCount() + Integer.parseInt(dto.getValue()));
                        }
                    });
                }
            }
        }
        dataList = dataList.stream().sorted(new ComparatorUtil()).collect(Collectors.toList());
        WaitEventDTO waitEventDTO = new WaitEventDTO();
        waitEventDTO.setData(dataList);
        waitEventDTO.setChartName(LocaleString.format("history.WAIT_EVENT.table"));
        return waitEventDTO;
    }
}