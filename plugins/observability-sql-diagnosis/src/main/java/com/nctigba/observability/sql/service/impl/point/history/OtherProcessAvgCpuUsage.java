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
 *  OtherProcessAvgCpuUsage.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/point/history/OtherProcessAvgCpuUsage.java
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
import com.nctigba.observability.sql.service.impl.collection.metric.AvgCpuItem;
import com.nctigba.observability.sql.service.impl.collection.metric.DbAvgCpuItem;
import com.nctigba.observability.sql.util.LocaleStringUtils;
import com.nctigba.observability.sql.util.PrometheusUtils;
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
public class OtherProcessAvgCpuUsage implements DiagnosisPointService<PrometheusDataDTO> {
    @Autowired
    private PrometheusUtils util;
    @Autowired
    private DiagnosisTaskMapper taskMapper;
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
    public AnalysisDTO analysis(DiagnosisTaskDO task, DataStoreService dataStoreService) {
        List<PrometheusVO> sysList = new ArrayList<>();
        List<PrometheusVO> proList = new ArrayList<>();
        for (CollectionItem<?> item : getSourceDataKeys()) {
            List<?> list = (List<?>) dataStoreService.getData(item).getCollectionData();
            for (Object object : list) {
                if (object instanceof PrometheusVO) {
                    if (item == avgCpuItem) {
                        sysList.add((PrometheusVO) object);
                    } else {
                        proList.add((PrometheusVO) object);
                    }
                }
            }
        }
        AnalysisDTO analysisDTO = new AnalysisDTO();
        if (CollectionUtils.isEmpty(sysList)) {
            analysisDTO.setIsHint(DiagnosisResultDO.ResultState.NO_ADVICE);
        } else {
            StringBuilder sb = new StringBuilder();
            List<Object> sysData = sysList.get(0).getValues();
            List<Object> proData;
            if (CollectionUtils.isEmpty(proList)) {
                analysisDTO.setIsHint(DiagnosisResultDO.ResultState.SUGGESTIONS);
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
                    analysisDTO.setIsHint(DiagnosisResultDO.ResultState.SUGGESTIONS);
                } else {
                    analysisDTO.setIsHint(DiagnosisResultDO.ResultState.NO_ADVICE);
                }
            }
        }
        analysisDTO.setPointType(DiagnosisResultDO.PointType.CENTER);
        return analysisDTO;
    }

    @Override
    public PrometheusDataDTO getShowData(int taskId) {
        DiagnosisTaskDO task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new HisDiagnosisException("taskId is not exists!");
        }
        PrometheusDataDTO dataList = new PrometheusDataDTO();
        List<MetricDataDTO> metricList = new ArrayList<>();
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
            PrometheusDataDTO dtoList = util.metricToLine(prometheusVOList);
            List<MetricDataDTO> datas = dtoList.getDatas();
            for (MetricDataDTO dataDTO : datas) {
                if (dataDTO.getName() != null && dataDTO.getName().equals(MetricConstants.AVG_CPU_USAGE_RATE)) {
                    dataDTO.setName(LocaleStringUtils.format("history.AVG_CPU_USAGE_RATE.metric"));
                } else if (dataDTO.getName() != null && dataDTO.getName().equals(
                        MetricConstants.DB_AVG_CPU_USAGE_RATE)) {
                    dataDTO.setName(LocaleStringUtils.format("history.DB_AVG_CPU_USAGE_RATE.metric"));
                } else {
                    dataDTO.setData(null);
                }
            }
            dataList.setTime(dtoList.getTime());
            metricList.addAll(dtoList.getDatas());
        }
        dataList.setChartName(LocaleStringUtils.format("history.OTHER_PROCESS_CPU.metric"));
        dataList.setUnit("%");
        dataList.setDatas(metricList);
        return dataList;
    }
}
