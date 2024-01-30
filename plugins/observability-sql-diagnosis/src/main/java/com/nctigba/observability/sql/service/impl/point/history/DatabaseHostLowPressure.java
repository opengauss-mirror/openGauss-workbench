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
 *  DatabaseHostLowPressure.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/point/history/DatabaseHostLowPressure.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl.point.history;

import com.nctigba.observability.sql.constant.MetricConstants;
import com.nctigba.observability.sql.exception.HisDiagnosisException;
import com.nctigba.observability.sql.mapper.DiagnosisTaskMapper;
import com.nctigba.observability.sql.model.dto.point.AnalysisDTO;
import com.nctigba.observability.sql.model.dto.point.MetricDataDTO;
import com.nctigba.observability.sql.model.dto.point.PrometheusDataDTO;
import com.nctigba.observability.sql.model.entity.DiagnosisResultDO;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.vo.collection.PrometheusVO;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.service.DataStoreService;
import com.nctigba.observability.sql.service.DiagnosisPointService;
import com.nctigba.observability.sql.service.impl.collection.metric.ActivityNumLowItem;
import com.nctigba.observability.sql.service.impl.collection.metric.ThreadPoolUsageLowItem;
import com.nctigba.observability.sql.util.LocaleStringUtils;
import com.nctigba.observability.sql.util.PrometheusUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * DatabaseHostLowPressure
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Service
public class DatabaseHostLowPressure implements DiagnosisPointService<List<PrometheusDataDTO>> {
    @Autowired
    private PrometheusUtils util;
    @Autowired
    private DiagnosisTaskMapper taskMapper;
    @Autowired
    private ActivityNumLowItem activityNumItem;
    @Autowired
    private ThreadPoolUsageLowItem threadPoolUsageItem;

    @Override
    public List<String> getOption() {
        return null;
    }


    @Override
    public List<CollectionItem<?>> getSourceDataKeys() {
        List<CollectionItem<?>> list = new ArrayList<>();
        list.add(activityNumItem);
        list.add(threadPoolUsageItem);
        return list;
    }

    @Override
    public AnalysisDTO analysis(DiagnosisTaskDO task, DataStoreService dataStoreService) {
        AnalysisDTO analysisDTO = new AnalysisDTO();
        int count = 0;
        for (CollectionItem<?> item : getSourceDataKeys()) {
            List<?> list = (List<?>) dataStoreService.getData(item).getCollectionData();
            if (!CollectionUtils.isEmpty(list)) {
                count++;
            }
        }
        if (count == 2) {
            analysisDTO.setIsHint(DiagnosisResultDO.ResultState.SUGGESTIONS);
        } else {
            analysisDTO.setIsHint(DiagnosisResultDO.ResultState.NO_ADVICE);
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
            List<PrometheusVO> prometheusVOList = new ArrayList<>();
            for (Object object : list) {
                if (object instanceof PrometheusVO) {
                    prometheusVOList.add((PrometheusVO) object);
                }
            }
            if (CollectionUtils.isEmpty(prometheusVOList)) {
                continue;
            }
            PrometheusDataDTO metricList = util.metricToLine(prometheusVOList);
            dataList.add(metricList);
        }
        for (PrometheusDataDTO dto : dataList) {
            if (dto.getChartName() != null && dto.getChartName().equals(MetricConstants.ACTIVITY_NUM)) {
                dto.setUnit("pcs");
                dto.setChartName(LocaleStringUtils.format("history.ACTIVITY_NUM.metric"));
            } else if (dto.getChartName() != null && dto.getChartName().equals(
                    MetricConstants.THREAD_POOL_USAGE_RATE)) {
                dto.setUnit("%");
                dto.setChartName(LocaleStringUtils.format("history.THREAD_POOL_USAGE_RATE.metric"));
            } else {
                dto.setUnit(null);
                dto.setChartName(null);
            }
            List<MetricDataDTO> datas = dto.getDatas();
            for (MetricDataDTO dataDTO : datas) {
                if (dataDTO.getName() != null && dataDTO.getName().equals(MetricConstants.ACTIVITY_NUM)) {
                    dataDTO.setName(LocaleStringUtils.format("history.ACTIVITY_NUM.metric"));
                } else if (dataDTO.getName() != null && dataDTO.getName().equals(
                        MetricConstants.THREAD_POOL_USAGE_RATE)) {
                    dataDTO.setName(LocaleStringUtils.format("history.THREAD_POOL_USAGE_RATE.metric"));
                } else {
                    dataDTO.setName(null);
                }
            }
        }
        return dataList;
    }
}
