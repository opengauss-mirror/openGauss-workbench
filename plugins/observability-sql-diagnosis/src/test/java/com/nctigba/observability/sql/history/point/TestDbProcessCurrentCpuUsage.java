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
 *  TestDbProcessCurrentCpuUsage.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/test/java/com/nctigba/observability/sql/history/point/TestDbProcessCurrentCpuUsage.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.history.point;

import com.nctigba.observability.sql.constant.PointTypeConstants;
import com.nctigba.observability.sql.constant.ThresholdConstants;
import com.nctigba.observability.sql.model.entity.DiagnosisResultDO;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.entity.DiagnosisThresholdDO;
import com.nctigba.observability.sql.model.vo.DataStoreVO;
import com.nctigba.observability.sql.model.vo.OptionVO;
import com.nctigba.observability.sql.model.vo.collection.AgentVO;
import com.nctigba.observability.sql.model.dto.point.AgentDTO;
import com.nctigba.observability.sql.model.dto.point.AnalysisDTO;
import com.nctigba.observability.sql.service.DataStoreService;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.service.impl.collection.agent.DbProcessCurrentCpuItem;
import com.nctigba.observability.sql.service.impl.collection.metric.CpuCoreNumItem;
import com.nctigba.observability.sql.service.impl.point.history.DbProcessCurrentCpuUsage;
import com.nctigba.observability.sql.util.PointUtils;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TestDbProcessCurrentCpuUsage
 *
 * @author luomeng
 * @since 2023/7/7
 */
@RunWith(MockitoJUnitRunner.class)
public class TestDbProcessCurrentCpuUsage {
    @Mock
    private DbProcessCurrentCpuItem item;
    @Mock
    private CpuCoreNumItem cpuCoreNumItem;
    @Mock
    private DataStoreService dataStoreService;
    @Mock
    private PointUtils util;
    @InjectMocks
    private DbProcessCurrentCpuUsage currentCpuUsage;
    private DiagnosisTaskDO diagnosisTaskDO;

    @Before
    public void before() {
        OptionVO optionVO = new OptionVO();
        optionVO.setOption("IS_LOCK");
        optionVO.setIsCheck(true);
        DiagnosisThresholdDO diagnosisThreshold = new DiagnosisThresholdDO();
        diagnosisThreshold.setThreshold(ThresholdConstants.DB_CPU_USAGE_RATE);
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
        List<String> list = currentCpuUsage.getOption();
        assertNull(list);
    }

    @Test
    public void testGetDiagnosisType() {
        String type = currentCpuUsage.getDiagnosisType();
        Assertions.assertEquals(PointTypeConstants.CURRENT, type);
    }

    @Test
    public void testGetSourceDataKeys() {
        List<CollectionItem<?>> result = currentCpuUsage.getSourceDataKeys();
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(item, result.get(0));
    }

    @Test
    public void testAnalysis_NoAgentData() {
        DataStoreVO config = mock(DataStoreVO.class);
        when(dataStoreService.getData(item)).thenReturn(config);
        when(config.getCollectionData()).thenReturn(new ArrayList<>());
        AnalysisDTO result = currentCpuUsage.analysis(diagnosisTaskDO, dataStoreService);
        assertNotNull(result);
    }

    @Test
    public void testAnalysis_haAgentData() {
        DataStoreVO config = mock(DataStoreVO.class);
        config.setCollectionItem(item);
        config.setCount(1);
        when(dataStoreService.getData(item)).thenReturn(config);
        DataStoreVO proConfig = mock(DataStoreVO.class);
        when(dataStoreService.getData(cpuCoreNumItem)).thenReturn(proConfig);
        AgentVO agentVO = new AgentVO();
        agentVO.setParamName("test");
        List<AgentDTO> dbValue = new ArrayList<>();
        AgentDTO agentDTO = new AgentDTO();
        agentDTO.setCpu("10");
        dbValue.add(agentDTO);
        agentVO.setDbValue(dbValue);
        agentVO.setSysValue(new ArrayList<>());
        when(config.getCollectionData()).thenReturn(agentVO);
        when(util.getCpuCoreNum(any())).thenReturn(8);
        AnalysisDTO result = currentCpuUsage.analysis(diagnosisTaskDO, dataStoreService);
        Assertions.assertEquals(DiagnosisResultDO.ResultState.NO_ADVICE, result.getIsHint());
        Assertions.assertEquals(DiagnosisResultDO.PointType.CENTER, result.getPointType());
        assertNotNull(result.getPointData());
    }

    @Test
    public void testGetShowData() {
        AgentDTO agentDTO = currentCpuUsage.getShowData(1);
        assertNull(agentDTO);
    }
}
