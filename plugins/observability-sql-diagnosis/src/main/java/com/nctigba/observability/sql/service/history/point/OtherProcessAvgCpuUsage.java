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
import com.nctigba.observability.sql.service.history.collection.metric.AvgCpuItem;
import com.nctigba.observability.sql.service.history.collection.metric.DbAvgCpuItem;
import com.nctigba.observability.sql.util.LocaleString;
import com.nctigba.observability.sql.util.PrometheusUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * OtherProcessAvgCpuUsage
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Service
public class OtherProcessAvgCpuUsage implements HisDiagnosisPointService<PrometheusDataDTO> {
    @Autowired
    private PrometheusUtil util;
    @Autowired
    private HisDiagnosisTaskMapper taskMapper;
    @Autowired
    private AvgCpuItem avgCpuItem;
    @Autowired
    private DbAvgCpuItem dbAvgCpuItem;


    @Override
    public List<String> getOption() {
        return null;
    }

    @Override
    public List<CollectionItem<?>> getSourceDataKeys() {
        List<CollectionItem<?>> list = new ArrayList<>();
        list.add(avgCpuItem);
        list.add(dbAvgCpuItem);
        return list;
    }

    @Override
    public AnalysisDTO analysis(HisDiagnosisTask task, DataStoreService dataStoreService) {
        List<PrometheusData> sysList = new ArrayList<>();
        List<PrometheusData> proList = new ArrayList<>();
        for (CollectionItem<?> item : getSourceDataKeys()) {
            List<?> list = (List<?>) dataStoreService.getData(item).getCollectionData();
            for (Object object : list) {
                if (object instanceof PrometheusData) {
                    if (item == avgCpuItem) {
                        sysList.add((PrometheusData) object);
                    } else {
                        proList.add((PrometheusData) object);
                    }
                }
            }
        }
        AnalysisDTO analysisDTO = new AnalysisDTO();
        if (CollectionUtils.isEmpty(sysList)) {
            analysisDTO.setIsHint(HisDiagnosisResult.ResultState.NO_ADVICE);
        } else {
            StringBuilder sb = new StringBuilder();
            List<Object> sysData = sysList.get(0).getValues();
            List<Object> proData;
            if (CollectionUtils.isEmpty(proList)) {
                analysisDTO.setIsHint(HisDiagnosisResult.ResultState.SUGGESTIONS);
            } else {
                proData = proList.get(0).getValues();
                for (Object sysObject : sysData) {
                    List<Object> sys = (List<Object>) sysObject;
                    for (Object proObject : proData) {
                        List<Object> pro = (List<Object>) proObject;
                        if (sys.get(0).toString().equals(pro.get(0).toString())) {
                            float sub =
                                    Float.parseFloat(sys.get(1).toString()) - Float.parseFloat(pro.get(1).toString());
                            if (sub > 0.0f) {
                                sb.append(sub);
                            }
                            break;
                        }
                    }
                }
                if (sb.length() > 0) {
                    analysisDTO.setIsHint(HisDiagnosisResult.ResultState.SUGGESTIONS);
                } else {
                    analysisDTO.setIsHint(HisDiagnosisResult.ResultState.NO_ADVICE);
                }
            }
        }
        analysisDTO.setPointType(HisDiagnosisResult.PointType.CENTER);
        return analysisDTO;
    }

    @Override
    public PrometheusDataDTO getShowData(int taskId) {
        HisDiagnosisTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new HisDiagnosisException("taskId is not exists!");
        }
        PrometheusDataDTO dataList = new PrometheusDataDTO();
        List<MetricDataDTO> metricList = new ArrayList<>();
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
            PrometheusDataDTO dtoList = util.metricToLine(prometheusDataList);
            List<MetricDataDTO> datas = dtoList.getDatas();
            for (MetricDataDTO dataDTO : datas) {
                if (dataDTO.getName() != null && dataDTO.getName().equals(MetricCommon.AVG_CPU_USAGE_RATE)) {
                    dataDTO.setName(LocaleString.format("history.AVG_CPU_USAGE_RATE.metric"));
                } else if (dataDTO.getName() != null && dataDTO.getName().equals(MetricCommon.DB_AVG_CPU_USAGE_RATE)) {
                    dataDTO.setName(LocaleString.format("history.DB_AVG_CPU_USAGE_RATE.metric"));
                } else {
                    dataDTO.setData(null);
                }
            }
            dataList.setTime(dtoList.getTime());
            metricList.addAll(dtoList.getDatas());
        }
        dataList.setChartName(LocaleString.format("history.OTHER_PROCESS_CPU.metric"));
        dataList.setUnit("%");
        dataList.setDatas(metricList);
        return dataList;
    }
}
