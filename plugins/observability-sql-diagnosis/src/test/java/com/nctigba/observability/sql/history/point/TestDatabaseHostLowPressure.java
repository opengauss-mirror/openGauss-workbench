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
 *  TestDatabaseHostLowPressure.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/test/java/com/nctigba/observability/sql/history/point/TestDatabaseHostLowPressure.java
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
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.entity.DiagnosisThresholdDO;
import com.nctigba.observability.sql.model.vo.DataStoreVO;
import com.nctigba.observability.sql.model.entity.DiagnosisResultDO;
import com.nctigba.observability.sql.model.vo.OptionVO;
import com.nctigba.observability.sql.model.vo.collection.PrometheusVO;
import com.nctigba.observability.sql.model.dto.point.AnalysisDTO;
import com.nctigba.observability.sql.model.dto.point.MetricDataDTO;
import com.nctigba.observability.sql.model.dto.point.PrometheusDataDTO;
import com.nctigba.observability.sql.service.DataStoreService;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.service.impl.collection.metric.ActivityNumLowItem;
import com.nctigba.observability.sql.service.impl.collection.metric.ThreadPoolUsageLowItem;
import com.nctigba.observability.sql.service.impl.point.history.DatabaseHostLowPressure;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TestDatabaseHostLowPressure
 *
 * @author luomeng
 * @since 2023/7/7
 */
@RunWith(MockitoJUnitRunner.class)
public class TestDatabaseHostLowPressure {
    @Mock
    private PrometheusUtils util;
    @Mock
    private DiagnosisTaskMapper taskMapper;
    @Mock
    private ActivityNumLowItem activityNumLowItem;
    @Mock
    private ThreadPoolUsageLowItem threadPoolUsageLowItem;
    @Mock
    private DataStoreService dataStoreService;
    @InjectMocks
    private DatabaseHostLowPressure databaseHostLowPressure;
    private DiagnosisTaskDO diagnosisTaskDO;

    @Before
    public void before() {
        OptionVO optionVO = new OptionVO();
        optionVO.setOption(String.valueOf(OptionEnum.IS_IO));
        optionVO.setIsCheck(true);
        DiagnosisThresholdDO diagnosisThreshold = new DiagnosisThresholdDO();
        diagnosisThreshold.setThreshold(ThresholdConstants.THREAD_POOL_USAGE_RATE);
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
        List<String> list = databaseHostLowPressure.getOption();
        assertNull(list);
    }

    @Test
    public void testGetSourceDataKeys() {
        List<CollectionItem<?>> result = databaseHostLowPressure.getSourceDataKeys();
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(activityNumLowItem, result.get(0));
        Assertions.assertEquals(threadPoolUsageLowItem, result.get(1));
    }

    @Test
    public void testAnalysis_NoPrometheusData() {
        DataStoreVO config = mock(DataStoreVO.class);
        when(dataStoreService.getData(activityNumLowItem)).thenReturn(config);
        when(dataStoreService.getData(threadPoolUsageLowItem)).thenReturn(config);
        when(config.getCollectionData()).thenReturn(new ArrayList<>());
        AnalysisDTO result = databaseHostLowPressure.analysis(mock(DiagnosisTaskDO.class), dataStoreService);
        Assertions.assertEquals(DiagnosisResultDO.ResultState.NO_ADVICE, result.getIsHint());
        Assertions.assertEquals(DiagnosisResultDO.PointType.CENTER, result.getPointType());
        assertNull(result.getPointData());
    }

    @Test
    public void testAnalysis_hasPrometheusData() {
        DataStoreVO config = mock(DataStoreVO.class);
        config.setCollectionItem(activityNumLowItem);
        config.setCount(1);
        when(dataStoreService.getData(activityNumLowItem)).thenReturn(config);
        DataStoreVO dbConfig = mock(DataStoreVO.class);
        dbConfig.setCollectionItem(threadPoolUsageLowItem);
        dbConfig.setCount(1);
        when(dataStoreService.getData(threadPoolUsageLowItem)).thenReturn(config);
        List<PrometheusVO> list = new ArrayList<>();
        PrometheusVO prometheusVO = new PrometheusVO();
        prometheusVO.setMetric(new JSONObject());
        prometheusVO.setValues(new JSONArray());
        list.add(prometheusVO);
        when(config.getCollectionData()).thenReturn(list);
        AnalysisDTO result = databaseHostLowPressure.analysis(mock(DiagnosisTaskDO.class), dataStoreService);
        Assertions.assertEquals(DiagnosisResultDO.ResultState.SUGGESTIONS, result.getIsHint());
        Assertions.assertEquals(DiagnosisResultDO.PointType.CENTER, result.getPointType());
        assertNull(result.getPointData());
    }

    @Test
    public void testGetShowData_Exception() {
        int taskId = 1;
        when(taskMapper.selectById(taskId)).thenReturn(null);
        assertThrows(HisDiagnosisException.class, () -> databaseHostLowPressure.getShowData(taskId));
    }

    @Test
    public void testGetShowData() {
        int taskId = 1;
        when(taskMapper.selectById(taskId)).thenReturn(diagnosisTaskDO);
        List<PrometheusVO> numList = new ArrayList<>();
        PrometheusVO numData = new PrometheusVO();
        numData.setMetric(new JSONObject());
        numData.setValues(new JSONArray());
        numList.add(numData);
        when(activityNumLowItem.queryData(diagnosisTaskDO)).thenReturn(numList);
        List<PrometheusVO> poolList = new ArrayList<>();
        PrometheusVO poolData = new PrometheusVO();
        poolData.setMetric(new JSONObject());
        poolData.setValues(new JSONArray());
        poolList.add(poolData);
        when(threadPoolUsageLowItem.queryData(diagnosisTaskDO)).thenReturn(poolList);
        PrometheusDataDTO numDataDTO = new PrometheusDataDTO();
        numDataDTO.setChartName(MetricConstants.ACTIVITY_NUM);
        List<MetricDataDTO> numDataDTOS = new ArrayList<>();
        MetricDataDTO numMetricData = new MetricDataDTO();
        numMetricData.setData(new ArrayList<>());
        numMetricData.setName(MetricConstants.ACTIVITY_NUM);
        numDataDTOS.add(numMetricData);
        numDataDTO.setDatas(numDataDTOS);
        when(util.metricToLine(numList)).thenReturn(numDataDTO);
        PrometheusDataDTO poolDataDTO = new PrometheusDataDTO();
        poolDataDTO.setChartName(MetricConstants.THREAD_POOL_USAGE_RATE);
        List<MetricDataDTO> poolDataDTOS = new ArrayList<>();
        MetricDataDTO poolMetricData = new MetricDataDTO();
        poolMetricData.setData(new ArrayList<>());
        poolMetricData.setName(MetricConstants.THREAD_POOL_USAGE_RATE);
        poolDataDTOS.add(poolMetricData);
        poolDataDTO.setDatas(poolDataDTOS);
        when(util.metricToLine(poolList)).thenReturn(poolDataDTO);
        List<PrometheusDataDTO> dataDTOList = databaseHostLowPressure.getShowData(taskId);
        assertNotNull(dataDTOList);
    }
}
