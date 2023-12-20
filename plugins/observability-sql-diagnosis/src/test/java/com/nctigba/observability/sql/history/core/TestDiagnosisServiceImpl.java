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
 *  TestDiagnosisServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/test/java/com/nctigba/observability/sql/history/core/TestDiagnosisServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.history.core;

import com.alibaba.fastjson.JSONObject;
import com.nctigba.observability.sql.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constant.DiagnosisTypeConstants;
import com.nctigba.observability.sql.mapper.DiagnosisResultMapper;
import com.nctigba.observability.sql.mapper.DiagnosisTaskMapper;
import com.nctigba.observability.sql.mapper.HisThresholdMapper;
import com.nctigba.observability.sql.model.entity.DiagnosisResultDO;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.entity.DiagnosisThresholdDO;
import com.nctigba.observability.sql.model.vo.OptionVO;
import com.nctigba.observability.sql.model.dto.TreeNodeDTO;
import com.nctigba.observability.sql.service.DiagnosisPointService;
import com.nctigba.observability.sql.service.impl.core.DiagnosisServiceImpl;
import com.nctigba.observability.sql.service.impl.point.history.AspAnalysis;
import com.nctigba.observability.sql.service.impl.point.history.LockTimeout;
import com.nctigba.observability.sql.util.LocaleStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TestDiagnosisServiceImpl
 *
 * @author luomeng
 * @since 2023/7/4
 */
@RunWith(MockitoJUnitRunner.class)
public class TestDiagnosisServiceImpl {
    @Mock
    private AspAnalysis aspAnalysis;
    @Mock
    private LockTimeout lockTimeout;
    @Mock
    private LocaleStringUtils localeToString;
    @Mock
    private HisThresholdMapper hisThresholdMapper;
    @Mock
    private DiagnosisResultMapper resultMapper;
    @Spy
    private List<DiagnosisPointService<?>> pointServiceList = new ArrayList<>();
    @Mock
    private DiagnosisTaskMapper taskMapper;
    @InjectMocks
    private DiagnosisServiceImpl hisDiagnosisService;
    private final int taskId = 1;
    private final String pointName = "AspAnalysis";

    @Before
    public void before() {
        pointServiceList.add(aspAnalysis);
        pointServiceList.add(lockTimeout);
        OptionVO optionVO = new OptionVO();
        optionVO.setOption("IS_LOCK");
        optionVO.setIsCheck(true);
        DiagnosisThresholdDO diagnosisThreshold = new DiagnosisThresholdDO();
        diagnosisThreshold.setThreshold("cpuUsageRate");
        diagnosisThreshold.setThresholdValue("20");
        DiagnosisTaskDO diagnosisTaskDO = new DiagnosisTaskDO();
        String nodeId = "37e8a893-0b7e-49b2-a0b4-e6fdf7dc4345";
        diagnosisTaskDO.setNodeId(nodeId);
        Date sTime = new Date();
        diagnosisTaskDO.setHisDataStartTime(sTime);
        Date eTime = new Date();
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
        diagnosisTaskDO.setDiagnosisType(DiagnosisTypeConstants.HISTORY);
        DiagnosisResultDO diagnosisResultDO = new DiagnosisResultDO();
        diagnosisResultDO.setNodeId("37e8a893-0b7e-49b2-a0b4-e6fdf7dc4345");
        diagnosisResultDO.setPointName(pointName);
        diagnosisResultDO.setTaskId(taskId);
        diagnosisResultDO.setPointSuggestion("test");
    }

    @Test
    public void testGetTopologyMap() {
        DiagnosisResultDO result = mock(DiagnosisResultDO.class);
        List<DiagnosisResultDO> resultList = new ArrayList<>();
        resultList.add(result);
        when(resultMapper.selectList(any())).thenReturn(resultList);
        DiagnosisTaskDO task = new DiagnosisTaskDO();
        task.setDiagnosisType(DiagnosisTypeConstants.SQL);
        String topologyMap = "-SqlTaskInfo ROOT"
                + System.getProperty("line.separator")
                + "--ParamTuning CENTER"
                + System.getProperty("line.separator")
                + "---OsParam CENTER"
                + System.getProperty("line.separator")
                + "---DatabaseParam CENTER";
        task.setTopologyMap(topologyMap);
        when(taskMapper.selectById(taskId)).thenReturn(task);
        boolean isAll = true;
        String diagnosisType = "sql";
        TreeNodeDTO treeNodeDTO = hisDiagnosisService.getTopologyMap(1, isAll, diagnosisType);
        assertNotNull(treeNodeDTO);
    }

    @Test
    public void testGetNodeDetail_Exception() {
        String diagnosisType = "sql";
        when(taskMapper.selectById(taskId)).thenReturn(null);
        assertThrows(
                HisDiagnosisException.class, () -> hisDiagnosisService.getNodeDetail(taskId, pointName, diagnosisType));
        when(taskMapper.selectById(taskId)).thenReturn(new DiagnosisTaskDO().setThresholds(new ArrayList<>()));
        when(resultMapper.selectOne(any())).thenReturn(new DiagnosisResultDO());
        assertThrows(
                HisDiagnosisException.class, () -> hisDiagnosisService.getNodeDetail(taskId, pointName, diagnosisType));
    }

    @Test
    public void testGetNodeDetail() {
        DiagnosisTaskDO task = new DiagnosisTaskDO();
        List<LinkedHashMap<String, String>> thresholds = new ArrayList<>();
        LinkedHashMap<String, String> linkedHashMap = new LinkedHashMap<>();
        linkedHashMap.put("cpuUsageRate", "20");
        thresholds.add(linkedHashMap);
        task.setThresholds((List<DiagnosisThresholdDO>) (List<?>) thresholds);
        when(taskMapper.selectById(taskId)).thenReturn(task);
        when(resultMapper.selectOne(any())).thenReturn(mock(DiagnosisResultDO.class));
        List<DiagnosisThresholdDO> thresholdList = new ArrayList<>();
        when(hisThresholdMapper.selectList(any())).thenReturn(thresholdList);
        String diagnosisType = "sql";
        Object branch1 = hisDiagnosisService.getNodeDetail(taskId, pointName, diagnosisType);
        assertNull(branch1);
        DiagnosisResultDO result = new DiagnosisResultDO();
        result.setData("test");
        result.setPointData(new JSONObject());
        result.setPointSuggestion("test");
        when(resultMapper.selectOne(any())).thenReturn(result);
        List<DiagnosisThresholdDO> thresholdList2 = new ArrayList<>();
        DiagnosisThresholdDO threshold = new DiagnosisThresholdDO();
        threshold.setThresholdValue("20");
        threshold.setThreshold("cpuUsageRate");
        thresholdList2.add(threshold);
        when(hisThresholdMapper.selectList(any())).thenReturn(thresholdList2);
        when(localeToString.trapLanguage(result)).thenReturn(result);
        Object branch2 = hisDiagnosisService.getNodeDetail(taskId, pointName, diagnosisType);
        assertNotNull(branch2);
    }
}
