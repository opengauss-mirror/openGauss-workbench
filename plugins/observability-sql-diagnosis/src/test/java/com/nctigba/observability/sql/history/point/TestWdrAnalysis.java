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
 *  TestWdrAnalysis.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/test/java/com/nctigba/observability/sql/history/point/TestWdrAnalysis.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.history.point;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nctigba.observability.sql.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constant.MetricConstants;
import com.nctigba.observability.sql.mapper.DiagnosisTaskMapper;
import com.nctigba.observability.sql.model.entity.DiagnosisResultDO;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.entity.DiagnosisThresholdDO;
import com.nctigba.observability.sql.model.vo.DataStoreVO;
import com.nctigba.observability.sql.model.vo.OptionVO;
import com.nctigba.observability.sql.model.vo.collection.PrometheusVO;
import com.nctigba.observability.sql.model.dto.point.AnalysisDTO;
import com.nctigba.observability.sql.model.dto.point.MetricDataDTO;
import com.nctigba.observability.sql.model.dto.point.PrometheusDataDTO;
import com.nctigba.observability.sql.service.DataStoreService;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.service.impl.collection.metric.DbAvgCpuItem;
import com.nctigba.observability.sql.service.impl.point.history.WdrAnalysis;
import com.nctigba.observability.sql.util.PointUtils;
import com.nctigba.observability.sql.util.PrometheusUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TestWdrAnalysis
 *
 * @author luomeng
 * @since 2023/7/7
 */
@RunWith(MockitoJUnitRunner.class)
public class TestWdrAnalysis {
    @Mock
    private PrometheusUtils util;
    @Mock
    private PointUtils pointUtils;
    @Mock
    private DiagnosisTaskMapper taskMapper;
    @Mock
    private DbAvgCpuItem dbAvgCpuItem;
    @Mock
    private DataStoreService dataStoreService;
    @InjectMocks
    private WdrAnalysis wdrAnalysis;
    private DiagnosisTaskDO diagnosisTaskDO;

    @Before
    public void before() {
        OptionVO optionVO = new OptionVO();
        optionVO.setOption("IS_LOCK");
        optionVO.setIsCheck(true);
        DiagnosisThresholdDO diagnosisThreshold = new DiagnosisThresholdDO();
        diagnosisThreshold.setThreshold("cpuUsageRate");
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
    }

    @Test
    public void testGetOption() {
        List<String> list = wdrAnalysis.getOption();
        assertNull(list);
    }

    @Test
    public void testGetSourceDataKeys() {
        List<CollectionItem<?>> result = wdrAnalysis.getSourceDataKeys();
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(dbAvgCpuItem, result.get(0));
    }

    @Test
    public void testAnalysis_NoPrometheusData() {
        DataStoreVO config = mock(DataStoreVO.class);
        when(dataStoreService.getData(dbAvgCpuItem)).thenReturn(config);
        when(config.getCollectionData()).thenReturn(new ArrayList<>());
        AnalysisDTO result = wdrAnalysis.analysis(mock(DiagnosisTaskDO.class), dataStoreService);
        Assertions.assertEquals(DiagnosisResultDO.ResultState.NO_ADVICE, result.getIsHint());
        Assertions.assertEquals(DiagnosisResultDO.PointType.CENTER, result.getPointType());
        assertNull(result.getPointData());
    }

    @Test
    public void testAnalysis_hasPrometheusData() {
        DataStoreVO config = mock(DataStoreVO.class);
        config.setCollectionItem(dbAvgCpuItem);
        config.setCount(1);
        when(dataStoreService.getData(dbAvgCpuItem)).thenReturn(config);
        List<PrometheusVO> list = new ArrayList<>();
        PrometheusVO prometheusVO = new PrometheusVO();
        prometheusVO.setMetric(new JSONObject());
        JSONArray objects = new JSONArray();
        for (int i = 0; i <= 240; i++) {
            JSONArray array = new JSONArray();
            array.add("100000");
            objects.add(array);
        }
        prometheusVO.setValues(objects);
        list.add(prometheusVO);
        when(config.getCollectionData()).thenReturn(list);
        AnalysisDTO result = wdrAnalysis.analysis(mock(DiagnosisTaskDO.class), dataStoreService);
        Assertions.assertEquals(DiagnosisResultDO.ResultState.NO_ADVICE, result.getIsHint());
        Assertions.assertEquals(DiagnosisResultDO.PointType.CENTER, result.getPointType());
        assertNull(result.getPointData());
    }

    @Test
    public void testGetShowData_Exception() {
        int taskId = 1;
        when(taskMapper.selectById(taskId)).thenReturn(null);
        assertThrows(HisDiagnosisException.class, () -> wdrAnalysis.getShowData(taskId));
    }

    @Test
    public void testGetShowData() {
        int taskId = 1;
        when(taskMapper.selectById(taskId)).thenReturn(diagnosisTaskDO);
        List<PrometheusVO> list = new ArrayList<>();
        PrometheusVO prometheusVO = new PrometheusVO();
        prometheusVO.setMetric(new JSONObject());
        prometheusVO.setValues(new JSONArray());
        list.add(prometheusVO);
        when(dbAvgCpuItem.queryData(diagnosisTaskDO)).thenReturn(list);
        List<PrometheusVO> prometheusVOList = new ArrayList<>();
        prometheusVOList.add(prometheusVO);
        when(pointUtils.dataToObject(list)).thenReturn(prometheusVOList);
        PrometheusDataDTO prometheusDataDTO = new PrometheusDataDTO();
        prometheusDataDTO.setData(new ArrayList<>());
        prometheusDataDTO.setChartName(MetricConstants.DB_AVG_CPU_USAGE_RATE);
        List<MetricDataDTO> dataDTOS = new ArrayList<>();
        MetricDataDTO metricDataDTO = new MetricDataDTO();
        metricDataDTO.setData(new ArrayList<>());
        metricDataDTO.setName(MetricConstants.DB_AVG_CPU_USAGE_RATE);
        dataDTOS.add(metricDataDTO);
        prometheusDataDTO.setDatas(dataDTOS);
        when(util.metricToLine(prometheusVOList)).thenReturn(prometheusDataDTO);
        List<PrometheusDataDTO> dataDTOList = wdrAnalysis.getShowData(taskId);
        assertNotNull(dataDTOList);
    }
}
