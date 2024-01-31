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
 *  WdrAnalysis.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/point/history/WdrAnalysis.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl.point.history;

import com.alibaba.fastjson.JSONArray;
import com.nctigba.observability.sql.constant.MetricConstants;
import com.nctigba.observability.sql.constant.PrometheusConstants;
import com.nctigba.observability.sql.exception.HisDiagnosisException;
import com.nctigba.observability.sql.mapper.DiagnosisTaskMapper;
import com.nctigba.observability.sql.model.dto.point.AnalysisDTO;
import com.nctigba.observability.sql.model.dto.point.AspAnalysisDTO;
import com.nctigba.observability.sql.model.dto.point.MetricDataDTO;
import com.nctigba.observability.sql.model.dto.point.PrometheusDataDTO;
import com.nctigba.observability.sql.model.entity.DiagnosisResultDO;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.vo.collection.PrometheusVO;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.service.DataStoreService;
import com.nctigba.observability.sql.service.DiagnosisPointService;
import com.nctigba.observability.sql.service.impl.collection.metric.DbAvgCpuItem;
import com.nctigba.observability.sql.util.LocaleStringUtils;
import com.nctigba.observability.sql.util.PointUtils;
import com.nctigba.observability.sql.util.PrometheusUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * WdrAnalysis
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Service
public class WdrAnalysis implements DiagnosisPointService<List<PrometheusDataDTO>> {
    @Autowired
    private PrometheusUtils util;
    @Autowired
    private DiagnosisTaskMapper taskMapper;
    @Autowired
    private DbAvgCpuItem dbAvgCpuItem;
    @Autowired
    private PointUtils pointUtils;

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
    public AnalysisDTO analysis(DiagnosisTaskDO task, DataStoreService dataStoreService) {
        AnalysisDTO analysisDTO = new AnalysisDTO();
        List<?> list = (List<?>) dataStoreService.getData(dbAvgCpuItem).getCollectionData();
        if (CollectionUtils.isEmpty(list)) {
            analysisDTO.setIsHint(DiagnosisResultDO.ResultState.NO_ADVICE);
            analysisDTO.setPointType(DiagnosisResultDO.PointType.CENTER);
            return analysisDTO;
        }
        List<PrometheusVO> prometheusVOList = new ArrayList<>();
        for (Object object : list) {
            if (object instanceof PrometheusVO) {
                prometheusVOList.add((PrometheusVO) object);
            }
        }
        List<AspAnalysisDTO> dtoList = new ArrayList<>();
        if (CollectionUtils.isEmpty(prometheusVOList)) {
            analysisDTO.setIsHint(DiagnosisResultDO.ResultState.NO_ADVICE);
        } else {
            PrometheusVO prometheusVO = prometheusVOList.get(0);
            JSONArray values = prometheusVO.getValues();
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
                    int startTime = timeList.get(i);
                    int realityTime = timeList.get(i + count);
                    int expectTime = startTime + step * (count);
                    if (realityTime == expectTime) {
                        while (timeList.size() - count > 0) {
                            if (timeList.get(i + count) > startTime + step * (count)) {
                                int endTime = timeList.get(i + count);
                                AspAnalysisDTO dto = new AspAnalysisDTO(startTime, endTime);
                                dtoList.add(dto);
                                break;
                            }
                            count++;
                        }
                    }
                }
            }
            if (!CollectionUtils.isEmpty(dtoList)) {
                analysisDTO.setIsHint(DiagnosisResultDO.ResultState.SUGGESTIONS);
            } else {
                analysisDTO.setIsHint(DiagnosisResultDO.ResultState.NO_ADVICE);
            }
        }
        analysisDTO.setPointType(DiagnosisResultDO.PointType.CENTER);
        return analysisDTO;
    }

    @Override
    public List<PrometheusDataDTO> getShowData(int taskId) {
        DiagnosisTaskDO task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new HisDiagnosisException("taskId is not exists!");
        }
        List<PrometheusDataDTO> dataList = new ArrayList<>();
        for (CollectionItem<?> item : getSourceDataKeys()) {
            List<?> list;
            Object obj = item.queryData(task);
            if (obj instanceof ArrayList) {
                list = (List<?>) obj;
            } else {
                continue;
            }
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
                List<MetricDataDTO> datas = dto.getDatas();
                for (MetricDataDTO dataDTO : datas) {
                    if (dataDTO.getName() != null && dataDTO.getName().equals(MetricConstants.DB_AVG_CPU_USAGE_RATE)) {
                        dataDTO.setName(LocaleStringUtils.format("history.DB_AVG_CPU_USAGE_RATE.metric"));
                    }
                }
            }
        }
        return dataList;
    }
}