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
 *  BusinessConnCount.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/point/history/BusinessConnCount.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl.point.history;

import com.nctigba.observability.sql.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constant.MetricConstants;
import com.nctigba.observability.sql.constant.PrometheusConstants;
import com.nctigba.observability.sql.constant.ThresholdConstants;
import com.nctigba.observability.sql.mapper.DiagnosisTaskMapper;
import com.nctigba.observability.sql.model.entity.DiagnosisResultDO;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.vo.collection.PrometheusVO;
import com.nctigba.observability.sql.model.dto.point.AnalysisDTO;
import com.nctigba.observability.sql.model.vo.point.ConnCountVO;
import com.nctigba.observability.sql.model.dto.point.AspAnalysisDTO;
import com.nctigba.observability.sql.model.dto.point.BusinessConnCountDTO;
import com.nctigba.observability.sql.model.dto.point.MetricDataDTO;
import com.nctigba.observability.sql.model.dto.point.PrometheusDataDTO;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.service.DataStoreService;
import com.nctigba.observability.sql.service.DiagnosisPointService;
import com.nctigba.observability.sql.service.impl.collection.metric.BusinessConnCountItem;
import com.nctigba.observability.sql.service.impl.collection.metric.DbAvgCpuItem;
import com.nctigba.observability.sql.util.LocaleStringUtils;
import com.nctigba.observability.sql.util.PointUtils;
import com.nctigba.observability.sql.util.PrometheusUtils;
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
public class BusinessConnCount implements DiagnosisPointService<Object> {
    @Autowired
    private DbAvgCpuItem dbAvgCpuItem;
    @Autowired
    private BusinessConnCountItem countItem;
    @Autowired
    private DiagnosisTaskMapper taskMapper;
    @Autowired
    private PointUtils pointUtils;
    @Autowired
    private PrometheusUtils util;

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
    public AnalysisDTO analysis(DiagnosisTaskDO task, DataStoreService dataStoreService) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        List<?> proList = (List<?>) dataStoreService.getData(dbAvgCpuItem).getCollectionData();
        AnalysisDTO analysisDTO = new AnalysisDTO();
        if (CollectionUtils.isEmpty(proList)) {
            analysisDTO.setIsHint(DiagnosisResultDO.ResultState.NO_ADVICE);
            analysisDTO.setPointType(DiagnosisResultDO.PointType.DIAGNOSIS);
            return analysisDTO;
        }
        List<PrometheusVO> prometheusVOList = new ArrayList<>();
        for (Object object : proList) {
            if (object instanceof PrometheusVO) {
                prometheusVOList.add((PrometheusVO) object);
            }
        }
        List<AspAnalysisDTO> dtoList = new ArrayList<>();
        if (CollectionUtils.isEmpty(prometheusVOList)) {
            analysisDTO.setIsHint(DiagnosisResultDO.ResultState.NO_ADVICE);
        } else {
            dtoList = pointUtils.aspTimeSlot(prometheusVOList);
        }
        List<ConnCountVO> connCount = new ArrayList<>();
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
                DiagnosisTaskDO queryTask = new DiagnosisTaskDO();
                queryTask.setNodeId(task.getNodeId());
                queryTask.setHisDataEndTime(preEndTime);
                queryTask.setHisDataStartTime(preStartTime);
                List<?> preObjectData = (List<?>) countItem.queryData(queryTask);
                int avgPreCount = 0;
                if (!CollectionUtils.isEmpty(preObjectData)) {
                    List<PrometheusVO> preData = new ArrayList<>();
                    for (Object object : preObjectData) {
                        if (object instanceof PrometheusVO) {
                            preData.add((PrometheusVO) object);
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
                    List<PrometheusVO> data = new ArrayList<>();
                    for (Object object : objectData) {
                        if (object instanceof PrometheusVO) {
                            data.add((PrometheusVO) object);
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
                HashMap<String, String> map = pointUtils.thresholdMap(task.getThresholds());
                if (avgCount - avgPreCount > Integer.parseInt(
                        map.get(ThresholdConstants.CONNECTION_NUM))) {
                    analysisDTO.setIsHint(DiagnosisResultDO.ResultState.SUGGESTIONS);
                    ConnCountVO countDTO = new ConnCountVO();
                    countDTO.setNowStartTime(simpleDateFormat.format(sTime));
                    countDTO.setNowEndTime(simpleDateFormat.format(eTime));
                    countDTO.setNowSessionCount(String.valueOf(avgCount));
                    countDTO.setBeforeStartTime(simpleDateFormat.format(preStartTime));
                    countDTO.setBeforeEndTime(simpleDateFormat.format(preEndTime));
                    countDTO.setBeforeSessionCount(String.valueOf(avgPreCount));
                    connCount.add(countDTO);
                } else {
                    analysisDTO.setIsHint(DiagnosisResultDO.ResultState.NO_ADVICE);
                }
            }
        } else {
            analysisDTO.setIsHint(DiagnosisResultDO.ResultState.NO_ADVICE);
        }
        BusinessConnCountDTO businessConnCountDTO = new BusinessConnCountDTO();
        businessConnCountDTO.setConnCount(connCount);
        businessConnCountDTO.setTimeSlot(dtoList);
        analysisDTO.setPointData(businessConnCountDTO);
        analysisDTO.setPointType(DiagnosisResultDO.PointType.DIAGNOSIS);
        return analysisDTO;
    }

    @Override
    public List<?> getShowData(int taskId) {
        DiagnosisTaskDO task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new HisDiagnosisException("taskId is not exists!");
        }
        List<PrometheusDataDTO> dataList = new ArrayList<>();
        for (CollectionItem<?> item : getSourceDataKeys()) {
            List<?> list = (List<?>) item.queryData(task);
            List<PrometheusVO> prometheusVOList = pointUtils.dataToObject(list);
            if (CollectionUtils.isEmpty(prometheusVOList)) {
                continue;
            }
            PrometheusDataDTO metricList = util.metricToLine(prometheusVOList);
            dataList.add(metricList);
        }
        for (PrometheusDataDTO dto : dataList) {
            if (dto.getChartName() != null && dto.getChartName().equals(MetricConstants.DB_AVG_CPU_USAGE_RATE)) {
                dto.setUnit("%");
                dto.setChartName(LocaleStringUtils.format("history.DB_AVG_CPU_USAGE_RATE.metric"));
            } else if (dto.getChartName() != null && dto.getChartName().equals(MetricConstants.BUSINESS_CONN_COUNT)) {
                dto.setUnit("pcs");
                dto.setChartName(LocaleStringUtils.format("history.BUSINESS_CONN_COUNT.metric"));
            } else {
                dto.setUnit(null);
                dto.setChartName(null);
            }
            List<MetricDataDTO> datas = dto.getDatas();
            for (MetricDataDTO dataDTO : datas) {
                if (dataDTO.getName() != null && dataDTO.getName().equals(MetricConstants.DB_AVG_CPU_USAGE_RATE)) {
                    dataDTO.setName(LocaleStringUtils.format("history.DB_AVG_CPU_USAGE_RATE.metric"));
                } else if (dataDTO.getName() != null && dataDTO.getName().equals(MetricConstants.BUSINESS_CONN_COUNT)) {
                    dataDTO.setName(LocaleStringUtils.format("history.BUSINESS_CONN_COUNT.metric"));
                } else {
                    dataDTO.setName(null);
                }
            }
        }
        return dataList;
    }
}
