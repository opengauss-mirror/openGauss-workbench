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
 *  HistoryWaitEvent.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/point/history/HistoryWaitEvent.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl.point.history;

import com.nctigba.observability.sql.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constant.ThresholdConstants;
import com.nctigba.observability.sql.mapper.DiagnosisTaskMapper;
import com.nctigba.observability.sql.model.dto.point.AnalysisDTO;
import com.nctigba.observability.sql.model.dto.point.WaitEventDTO;
import com.nctigba.observability.sql.model.entity.DiagnosisResultDO;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.entity.DiagnosisThresholdDO;
import com.nctigba.observability.sql.model.vo.collection.PrometheusVO;
import com.nctigba.observability.sql.model.vo.point.MetricToTableVO;
import com.nctigba.observability.sql.model.vo.point.WaitEventInfoVO;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.service.DataStoreService;
import com.nctigba.observability.sql.service.DiagnosisPointService;
import com.nctigba.observability.sql.service.impl.collection.metric.WaitEventItem;
import com.nctigba.observability.sql.util.ComparatorUtils;
import com.nctigba.observability.sql.util.LocaleStringUtils;
import com.nctigba.observability.sql.util.PrometheusUtils;
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
public class HistoryWaitEvent implements DiagnosisPointService<WaitEventDTO> {
    @Autowired
    private PrometheusUtils util;
    @Autowired
    private DiagnosisTaskMapper taskMapper;
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
    public AnalysisDTO analysis(DiagnosisTaskDO task, DataStoreService dataStoreService) {
        HashMap<String, String> map = new HashMap<>();
        List<DiagnosisThresholdDO> thresholds = task.getThresholds();
        for (DiagnosisThresholdDO threshold : thresholds) {
            if (threshold.getThreshold().equals(ThresholdConstants.WAIT_EVENT_NUM)) {
                map.put(threshold.getThreshold(), threshold.getThresholdValue());
            }
        }
        if (map.isEmpty()) {
            throw new HisDiagnosisException("fetch threshold data failed!");
        }
        List<?> list = (List<?>) dataStoreService.getData(item).getCollectionData();
        List<PrometheusVO> prometheusVOList = new ArrayList<>();
        for (Object object : list) {
            if (object instanceof PrometheusVO) {
                prometheusVOList.add((PrometheusVO) object);
            }
        }
        AnalysisDTO analysisDTO = new AnalysisDTO();
        if (CollectionUtils.isEmpty(prometheusVOList)) {
            analysisDTO.setIsHint(DiagnosisResultDO.ResultState.NO_ADVICE);
        } else {
            List<MetricToTableVO> dtoList = util.metricToTable(prometheusVOList);
            int count = 0;
            long range = (task.getHisDataEndTime().getTime() - task.getHisDataStartTime().getTime()) / 1000 / 15;
            for (MetricToTableVO dto : dtoList) {
                count += Integer.parseInt(dto.getValue());
            }
            if (count / range > Integer.parseInt(map.get(ThresholdConstants.WAIT_EVENT_NUM))) {
                analysisDTO.setIsHint(DiagnosisResultDO.ResultState.SUGGESTIONS);
            } else {
                analysisDTO.setIsHint(DiagnosisResultDO.ResultState.NO_ADVICE);
            }
        }
        analysisDTO.setPointType(DiagnosisResultDO.PointType.DIAGNOSIS);
        return analysisDTO;
    }

    @Override
    public WaitEventDTO getShowData(int taskId) {
        DiagnosisTaskDO task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new HisDiagnosisException("taskId is not exists!");
        }
        List<MetricToTableVO> dtoList = getMetricData(task);
        List<WaitEventInfoVO> dataList = new ArrayList<>();
        for (MetricToTableVO dto : dtoList) {
            if (CollectionUtils.isEmpty(dataList)) {
                WaitEventInfoVO waitEventInfoVO = new WaitEventInfoVO();
                waitEventInfoVO.setEventName(dto.getName());
                waitEventInfoVO.setEventCount(Integer.parseInt(dto.getValue()));
                waitEventInfoVO.setEventDetail(
                        LocaleStringUtils.format("history.WAIT_EVENT." + dto.getName().replace(" ", "") + ".detail"));
                waitEventInfoVO.setSuggestion(
                        LocaleStringUtils.format("history.WAIT_EVENT." + dto.getName().replace(" ", "") + ".suggest"));
                dataList.add(waitEventInfoVO);
            } else {
                List<WaitEventInfoVO> isExist = dataList.stream().filter(
                        f -> f.getEventName().equals(dto.getName())).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(isExist)) {
                    WaitEventInfoVO waitEventInfoVO = new WaitEventInfoVO();
                    waitEventInfoVO.setEventName(dto.getName());
                    waitEventInfoVO.setEventCount(Integer.parseInt(dto.getValue()));
                    String fieldName = dto.getName().contains(":") ? dto.getName().substring(
                            0, dto.getName().indexOf(":")).replace(" ", "") : dto.getName().replace(" ", "");
                    waitEventInfoVO.setEventDetail(
                            LocaleStringUtils.format("history.WAIT_EVENT." + fieldName + ".detail"));
                    waitEventInfoVO.setSuggestion(
                            LocaleStringUtils.format("history.WAIT_EVENT." + fieldName + ".suggest"));
                    dataList.add(waitEventInfoVO);
                } else {
                    dataList.forEach(f -> {
                        if (f.getEventName().equals(dto.getName())) {
                            f.setEventCount(f.getEventCount() + Integer.parseInt(dto.getValue()));
                        }
                    });
                }
            }
        }
        dataList = dataList.stream().sorted(new ComparatorUtils()).collect(Collectors.toList());
        WaitEventDTO waitEventDTO = new WaitEventDTO();
        waitEventDTO.setData(dataList);
        waitEventDTO.setChartName(LocaleStringUtils.format("history.WAIT_EVENT.table"));
        return waitEventDTO;
    }

    private List<MetricToTableVO> getMetricData(DiagnosisTaskDO task) {
        List<MetricToTableVO> dtoList = new ArrayList<>();
        for (CollectionItem<?> item : getSourceDataKeys()) {
            List<?> list = (List<?>) item.queryData(task);
            List<PrometheusVO> prometheusVOList = new ArrayList<>();
            for (Object object : list) {
                if (object instanceof PrometheusVO) {
                    prometheusVOList.add((PrometheusVO) object);
                }
            }
            if (CollectionUtils.isEmpty(prometheusVOList)) {
                continue;
            }
            dtoList = util.metricToTable(prometheusVOList);
        }
        return dtoList;
    }
}