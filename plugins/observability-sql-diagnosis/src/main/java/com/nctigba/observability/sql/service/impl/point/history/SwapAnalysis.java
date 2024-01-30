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
 *  SwapAnalysis.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/point/history/SwapAnalysis.java
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
import com.nctigba.observability.sql.model.vo.AutoShowDataVO;
import com.nctigba.observability.sql.model.vo.collection.PrometheusVO;
import com.nctigba.observability.sql.model.vo.point.ChartShowDataVO;
import com.nctigba.observability.sql.model.vo.point.ChartShowDataYDataVO;
import com.nctigba.observability.sql.model.vo.point.ShowData;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.service.DataStoreService;
import com.nctigba.observability.sql.service.DiagnosisPointService;
import com.nctigba.observability.sql.service.impl.collection.metric.SwapInItem;
import com.nctigba.observability.sql.service.impl.collection.metric.SwapOutItem;
import com.nctigba.observability.sql.util.LocaleStringUtils;
import com.nctigba.observability.sql.util.PrometheusUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * SwapAnalysis
 *
 * @author luomeng
 * @since 2023/11/12
 */
@Service
public class SwapAnalysis implements DiagnosisPointService<AutoShowDataVO> {
    @Autowired
    private SwapInItem swapInItem;
    @Autowired
    private SwapOutItem swapOutItem;
    @Autowired
    private PrometheusUtils util;
    @Autowired
    private DiagnosisTaskMapper taskMapper;

    @Override
    public List<String> getOption() {
        return null;
    }

    @Override
    public List<CollectionItem<?>> getSourceDataKeys() {
        List<CollectionItem<?>> list = new ArrayList<>();
        list.add(swapInItem);
        list.add(swapOutItem);
        return list;
    }

    @Override
    public AnalysisDTO analysis(DiagnosisTaskDO task, DataStoreService dataStoreService) {
        AnalysisDTO analysisDTO = new AnalysisDTO();
        analysisDTO.setIsHint(DiagnosisResultDO.ResultState.NO_ADVICE);
        analysisDTO.setPointType(DiagnosisResultDO.PointType.DIAGNOSIS);
        for (CollectionItem<?> item : getSourceDataKeys()) {
            List<?> list = (List<?>) dataStoreService.getData(item).getCollectionData();
            if (!CollectionUtils.isEmpty(list)) {
                analysisDTO.setIsHint(DiagnosisResultDO.ResultState.SUGGESTIONS);
            }
        }
        return analysisDTO;
    }

    @Override
    public AutoShowDataVO getShowData(int taskId) {
        DiagnosisTaskDO task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new HisDiagnosisException("taskId is not exists!");
        }
        List<String> inList = new ArrayList<>();
        List<String> outList = new ArrayList<>();
        List<String> timeList = new ArrayList<>();
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
                if (dataDTO.getName() != null && dataDTO.getName().equals(MetricConstants.SWAP_IN)) {
                    inList.addAll(dataDTO.getData());
                } else if (dataDTO.getName() != null && dataDTO.getName().equals(MetricConstants.SWAP_OUT)) {
                    outList.addAll(dataDTO.getData());
                } else {
                    dataDTO.setData(null);
                }
            }
            timeList = dtoList.getTime();
        }
        return buildShowData(inList, outList, timeList);
    }

    private AutoShowDataVO buildShowData(List<String> inList, List<String> outList, List<String> timeList) {
        ChartShowDataYDataVO inChartData = new ChartShowDataYDataVO();
        inChartData.setName(LocaleStringUtils.format("sql.swap.in"));
        inChartData.setData(inList);
        ChartShowDataYDataVO outChartData = new ChartShowDataYDataVO();
        outChartData.setName(LocaleStringUtils.format("sql.swap.out"));
        outChartData.setData(outList);
        List<ChartShowDataYDataVO> series = new ArrayList<>();
        series.add(inChartData);
        series.add(outChartData);
        ChartShowDataVO chartShowDataVO = new ChartShowDataVO();
        chartShowDataVO.setUnit("pages");
        chartShowDataVO.setXData(timeList);
        chartShowDataVO.setSeries(series);
        chartShowDataVO.setDataName("Swap");
        AutoShowDataVO dataVO = new AutoShowDataVO();
        List<ShowData> list = new ArrayList<>();
        list.add(chartShowDataVO);
        dataVO.setData(list);
        return dataVO;
    }
}
