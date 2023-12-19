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
 *  TestCpuTimeTopSql.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/test/java/com/nctigba/observability/sql/history/point/TestCpuTimeTopSql.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.history.point;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nctigba.observability.sql.exception.HisDiagnosisException;
import com.nctigba.observability.sql.enums.OptionEnum;
import com.nctigba.observability.sql.constant.SqlConstants;
import com.nctigba.observability.sql.constant.ThresholdConstants;
import com.nctigba.observability.sql.mapper.DiagnosisTaskMapper;
import com.nctigba.observability.sql.model.entity.DiagnosisResultDO;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.entity.DiagnosisThresholdDO;
import com.nctigba.observability.sql.model.vo.DataStoreVO;
import com.nctigba.observability.sql.model.vo.OptionVO;
import com.nctigba.observability.sql.model.vo.collection.DatabaseVO;
import com.nctigba.observability.sql.model.vo.collection.PrometheusVO;
import com.nctigba.observability.sql.model.dto.point.AnalysisDTO;
import com.nctigba.observability.sql.service.DataStoreService;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.service.impl.collection.table.CpuTimeTopSqlItem;
import com.nctigba.observability.sql.service.impl.point.history.CpuTimeTopSql;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TestCpuTimeTopSql
 *
 * @author luomeng
 * @since 2023/7/7
 */
@RunWith(MockitoJUnitRunner.class)
public class TestCpuTimeTopSql {
    @Mock
    private DiagnosisTaskMapper taskMapper;
    @Mock
    private CpuTimeTopSqlItem item;
    @Mock
    private DataStoreService dataStoreService;
    @InjectMocks
    private CpuTimeTopSql cpuTimeTopSql;
    private DiagnosisTaskDO diagnosisTaskDO;

    @Before
    public void before() {
        OptionVO optionVO = new OptionVO();
        optionVO.setOption(String.valueOf(OptionEnum.IS_MEMORY));
        optionVO.setIsCheck(true);
        DiagnosisThresholdDO diagnosisThreshold = new DiagnosisThresholdDO();
        diagnosisThreshold.setThreshold(ThresholdConstants.SQL_NUM);
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
        List<String> list = cpuTimeTopSql.getOption();
        assertNull(list);
    }

    @Test
    public void testGetSourceDataKeys() {
        List<CollectionItem<?>> result = cpuTimeTopSql.getSourceDataKeys();
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(item, result.get(0));
    }

    @Test
    public void testAnalysis_NoDbData() {
        DataStoreVO config = mock(DataStoreVO.class);
        when(dataStoreService.getData(item)).thenReturn(config);
        when(config.getCollectionData()).thenReturn(new ArrayList<>());
        AnalysisDTO result = cpuTimeTopSql.analysis(mock(DiagnosisTaskDO.class), dataStoreService);
        Assertions.assertEquals(DiagnosisResultDO.ResultState.NO_ADVICE, result.getIsHint());
        Assertions.assertEquals(DiagnosisResultDO.PointType.DISPLAY, result.getPointType());
        assertNull(result.getPointData());
    }

    @Test
    public void testAnalysis_hasDbData() {
        DataStoreVO config = mock(DataStoreVO.class);
        config.setCollectionItem(item);
        config.setCount(1);
        when(dataStoreService.getData(item)).thenReturn(config);
        List<PrometheusVO> list = new ArrayList<>();
        PrometheusVO prometheusVO = new PrometheusVO();
        prometheusVO.setMetric(new JSONObject());
        list.add(prometheusVO);
        when(config.getCollectionData()).thenReturn(list);
        AnalysisDTO result = cpuTimeTopSql.analysis(mock(DiagnosisTaskDO.class), dataStoreService);
        Assertions.assertEquals(DiagnosisResultDO.ResultState.NO_ADVICE, result.getIsHint());
        Assertions.assertEquals(DiagnosisResultDO.PointType.DISPLAY, result.getPointType());
        assertNull(result.getPointData());
    }

    @Test
    public void testGetShowData_Exception() {
        int taskId = 1;
        when(taskMapper.selectById(taskId)).thenReturn(null);
        assertThrows(HisDiagnosisException.class, () -> cpuTimeTopSql.getShowData(taskId));
    }

    @Test
    public void testGetShowData_error() {
        int taskId = 1;
        when(taskMapper.selectById(taskId)).thenReturn(diagnosisTaskDO);
        String error = "error:";
        when(item.queryData(diagnosisTaskDO)).thenReturn(error);
        List<?> dataDTOList = cpuTimeTopSql.getShowData(taskId);
        assertNotNull(dataDTOList);
        assertEquals(error, dataDTOList.get(0));
    }

    @Test
    public void testGetShowData_true() {
        int taskId = 1;
        when(taskMapper.selectById(taskId)).thenReturn(diagnosisTaskDO);
        List<DatabaseVO> list = new ArrayList<>();
        DatabaseVO databaseVO = new DatabaseVO();
        databaseVO.setValue(new JSONArray());
        JSONObject sqlName = new JSONObject();
        sqlName.put("__name__", SqlConstants.CPU_TIME_TOP_SQL);
        databaseVO.setSqlName(sqlName);
        databaseVO.setTableName("");
        list.add(databaseVO);
        when(item.queryData(diagnosisTaskDO)).thenReturn(list);
        List<?> dataDTOList = cpuTimeTopSql.getShowData(taskId);
        assertNotNull(dataDTOList);
    }
}
