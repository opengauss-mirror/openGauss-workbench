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
 *  TestBusinessConnCount.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/test/java/com/nctigba/observability/sql/history/point/TestBusinessConnCount.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.history.point;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nctigba.observability.sql.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constant.MetricConstants;
import com.nctigba.observability.sql.enums.OptionEnum;
import com.nctigba.observability.sql.constant.ThresholdConstants;
import com.nctigba.observability.sql.mapper.DiagnosisTaskMapper;
import com.nctigba.observability.sql.model.entity.DiagnosisResultDO;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.vo.DataStoreVO;
import com.nctigba.observability.sql.model.entity.DiagnosisThresholdDO;
import com.nctigba.observability.sql.model.vo.OptionVO;
import com.nctigba.observability.sql.model.vo.collection.PrometheusVO;
import com.nctigba.observability.sql.model.dto.point.AnalysisDTO;
import com.nctigba.observability.sql.model.dto.point.AspAnalysisDTO;
import com.nctigba.observability.sql.model.dto.point.MetricDataDTO;
import com.nctigba.observability.sql.model.dto.point.PrometheusDataDTO;
import com.nctigba.observability.sql.service.DataStoreService;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.service.impl.collection.metric.BusinessConnCountItem;
import com.nctigba.observability.sql.service.impl.collection.metric.DbAvgCpuItem;
import com.nctigba.observability.sql.service.impl.point.history.BusinessConnCount;
import com.nctigba.observability.sql.util.PointUtils;
import com.nctigba.observability.sql.util.PrometheusUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TestBusinessConnCount
 *
 * @author luomeng
 * @since 2023/7/7
 */
@RunWith(MockitoJUnitRunner.class)
public class TestBusinessConnCount {
    @Mock
    private PrometheusUtils util;
    @Mock
    private PointUtils pointUtils;
    @Mock
    private BusinessConnCountItem countItem;
    @Mock
    private DiagnosisTaskMapper taskMapper;
    @Mock
    private DbAvgCpuItem dbAvgCpuItem;
    @Mock
    private DataStoreService dataStoreService;
    @InjectMocks
    private BusinessConnCount businessConnCount;
    private DiagnosisTaskDO diagnosisTaskDO;

    @Before
    public void before() {
        OptionVO optionVO = new OptionVO();
        optionVO.setOption(String.valueOf(OptionEnum.IS_MEMORY));
        optionVO.setIsCheck(true);
        DiagnosisThresholdDO diagnosisThreshold = new DiagnosisThresholdDO();
        diagnosisThreshold.setThreshold(ThresholdConstants.CONNECTION_NUM);
        diagnosisThreshold.setThresholdValue("20");
        diagnosisTaskDO = new DiagnosisTaskDO();
        String nodeId = "37e8a893-0b7e-49b2-a0b4-e6fdf7dc4345";
        Date sTime = new Date();
        Date eTime = new Date();
        diagnosisTaskDO.setNodeId(nodeId);
        diagnosisTaskDO.setHisDataStartTime(sTime);
        diagnosisTaskDO.setHisDataEndTime(eTime);
        List<OptionVO> config = new ArrayList<>() {{
            add(optionVO);
        }};
        diagnosisTaskDO.setConfigs(config);
        List<DiagnosisThresholdDO> threshold = new ArrayList<>() {{
            add(diagnosisThreshold);
        }};
        diagnosisTaskDO.setThresholds(threshold);
        diagnosisTaskDO.setSpan("50s");
        diagnosisTaskDO.setClusterId(nodeId);
        diagnosisTaskDO.setRemarks("test");
        diagnosisTaskDO.setCreateTime(new Date());
        diagnosisTaskDO.setDbName("test");
        diagnosisTaskDO.setNodeVOSub(new OpsClusterVO());
        diagnosisTaskDO.setIsDeleted(1);
        diagnosisTaskDO.setId(1);
        diagnosisTaskDO.setUpdateTime(new Date());
    }

    @Test
    public void testGetOption() {
        List<String> list = businessConnCount.getOption();
        assertNull(list);
    }

    @Test
    public void testGetSourceDataKeys() {
        List<CollectionItem<?>> result = businessConnCount.getSourceDataKeys();
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(dbAvgCpuItem, result.get(0));
        Assertions.assertEquals(countItem, result.get(1));
    }

    @Test
    public void testAnalysis_NoPrometheusData() {
        DataStoreVO config = mock(DataStoreVO.class);
        when(dataStoreService.getData(dbAvgCpuItem)).thenReturn(config);
        when(config.getCollectionData()).thenReturn(new ArrayList<>());
        AnalysisDTO result = businessConnCount.analysis(mock(DiagnosisTaskDO.class), dataStoreService);
        Assertions.assertEquals(DiagnosisResultDO.ResultState.NO_ADVICE, result.getIsHint());
        Assertions.assertEquals(DiagnosisResultDO.PointType.DIAGNOSIS, result.getPointType());
        assertNull(result.getPointData());
    }

    @Test
    public void testAnalysis_hasProData_NoAsp() {
        DataStoreVO config = mock(DataStoreVO.class);
        config.setCollectionItem(dbAvgCpuItem);
        config.setCount(1);
        when(dataStoreService.getData(dbAvgCpuItem)).thenReturn(config);
        List<PrometheusVO> list = new ArrayList<>();
        PrometheusVO prometheusVO = new PrometheusVO();
        prometheusVO.setMetric(new JSONObject());
        list.add(prometheusVO);
        when(config.getCollectionData()).thenReturn(list);
        AnalysisDTO result = businessConnCount.analysis(mock(DiagnosisTaskDO.class), dataStoreService);
        Assertions.assertEquals(DiagnosisResultDO.ResultState.NO_ADVICE, result.getIsHint());
        Assertions.assertEquals(DiagnosisResultDO.PointType.DIAGNOSIS, result.getPointType());
        assertNotNull(result.getPointData());
    }

    @Test
    public void testAnalysis_hasProData_hasAsp() {
        DataStoreVO config = mock(DataStoreVO.class);
        config.setCollectionItem(dbAvgCpuItem);
        config.setCount(1);
        when(dataStoreService.getData(dbAvgCpuItem)).thenReturn(config);
        List<PrometheusVO> list = new ArrayList<>();
        PrometheusVO prometheusVO = new PrometheusVO();
        prometheusVO.setMetric(new JSONObject());
        JSONArray jsonArray = new JSONArray();
        List<String> stringList = new ArrayList<>();
        stringList.add("100");
        stringList.add("200");
        jsonArray.add(stringList);
        prometheusVO.setValues(jsonArray);
        list.add(prometheusVO);
        when(config.getCollectionData()).thenReturn(list);
        List<AspAnalysisDTO> analysisDTOList = new ArrayList<>();
        AspAnalysisDTO analysisDTO = new AspAnalysisDTO(1, 2);
        analysisDTOList.add(analysisDTO);
        when(pointUtils.aspTimeSlot(list)).thenReturn(analysisDTOList);
        HashMap<String, String> map = new HashMap<>();
        map.put(ThresholdConstants.CONNECTION_NUM, "10");
        when(pointUtils.thresholdMap(diagnosisTaskDO.getThresholds())).thenReturn(map);
        when(countItem.queryData(any())).thenReturn(list);
        AnalysisDTO result = businessConnCount.analysis(diagnosisTaskDO, dataStoreService);
        Assertions.assertEquals(DiagnosisResultDO.ResultState.NO_ADVICE, result.getIsHint());
        Assertions.assertEquals(DiagnosisResultDO.PointType.DIAGNOSIS, result.getPointType());
        assertNotNull(result.getPointData());
    }

    @Test
    public void testAnalysis_hasData_NoPro() {
        DataStoreVO config = mock(DataStoreVO.class);
        config.setCollectionItem(dbAvgCpuItem);
        config.setCount(1);
        when(dataStoreService.getData(dbAvgCpuItem)).thenReturn(config);
        List<PrometheusVO> list = new ArrayList<>();
        when(config.getCollectionData()).thenReturn(list);
        AnalysisDTO result = businessConnCount.analysis(mock(DiagnosisTaskDO.class), dataStoreService);
        Assertions.assertEquals(DiagnosisResultDO.ResultState.NO_ADVICE, result.getIsHint());
        Assertions.assertEquals(DiagnosisResultDO.PointType.DIAGNOSIS, result.getPointType());
        assertNull(result.getPointData());
    }

    @Test
    public void testGetShowData_Exception() {
        int taskId = 1;
        when(taskMapper.selectById(taskId)).thenReturn(null);
        assertThrows(HisDiagnosisException.class, () -> businessConnCount.getShowData(taskId));
    }

    @Test
    public void testGetShowData() {
        int taskId = 1;
        when(taskMapper.selectById(taskId)).thenReturn(diagnosisTaskDO);
        List<PrometheusVO> cpuList = new ArrayList<>();
        PrometheusVO cpuData = new PrometheusVO();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("cpu", "50");
        cpuData.setMetric(jsonObject);
        JSONArray jsonArray = new JSONArray();
        jsonArray.add("cpu");
        cpuData.setValues(jsonArray);
        cpuList.add(cpuData);
        when(dbAvgCpuItem.queryData(diagnosisTaskDO)).thenReturn(cpuList);
        List<PrometheusVO> countList = new ArrayList<>();
        PrometheusVO countData = new PrometheusVO();
        countData.setMetric(new JSONObject());
        countData.setValues(new JSONArray());
        countList.add(countData);
        when(countItem.queryData(diagnosisTaskDO)).thenReturn(countList);
        List<PrometheusVO> cpuDataList = new ArrayList<>();
        cpuDataList.add(cpuData);
        when(pointUtils.dataToObject(cpuList)).thenReturn(cpuDataList);
        List<PrometheusVO> countDataList = new ArrayList<>();
        countDataList.add(countData);
        when(pointUtils.dataToObject(countList)).thenReturn(countDataList);
        PrometheusDataDTO cpuDataDTO = new PrometheusDataDTO();
        cpuDataDTO.setData(new ArrayList<>());
        cpuDataDTO.setChartName(MetricConstants.DB_AVG_CPU_USAGE_RATE);
        List<MetricDataDTO> cpuDataDTOS = new ArrayList<>();
        MetricDataDTO cpuMetricDataDTO = new MetricDataDTO();
        cpuMetricDataDTO.setData(new ArrayList<>());
        cpuMetricDataDTO.setName(MetricConstants.DB_AVG_CPU_USAGE_RATE);
        cpuDataDTOS.add(cpuMetricDataDTO);
        cpuDataDTO.setDatas(cpuDataDTOS);
        when(util.metricToLine(cpuDataList)).thenReturn(cpuDataDTO);
        PrometheusDataDTO countDataDTO = new PrometheusDataDTO();
        countDataDTO.setData(new ArrayList<>());
        countDataDTO.setChartName(MetricConstants.BUSINESS_CONN_COUNT);
        List<MetricDataDTO> countDataDTOS = new ArrayList<>();
        MetricDataDTO countMetricDataDTO = new MetricDataDTO();
        countMetricDataDTO.setData(new ArrayList<>());
        countMetricDataDTO.setName(MetricConstants.BUSINESS_CONN_COUNT);
        countDataDTOS.add(countMetricDataDTO);
        countDataDTO.setDatas(countDataDTOS);
        when(util.metricToLine(countDataList)).thenReturn(countDataDTO);
        Object dataDTOList = businessConnCount.getShowData(taskId);
        assertNotNull(dataDTOList);
    }
}
