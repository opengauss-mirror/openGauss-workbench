/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.history.point;

import com.nctigba.observability.sql.constants.history.DiagnosisTypeCommon;
import com.nctigba.observability.sql.model.history.DataStoreConfig;
import com.nctigba.observability.sql.model.history.HisDiagnosisResult;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.HisDiagnosisThreshold;
import com.nctigba.observability.sql.model.history.data.AgentData;
import com.nctigba.observability.sql.model.history.dto.AgentDTO;
import com.nctigba.observability.sql.model.history.dto.AnalysisDTO;
import com.nctigba.observability.sql.model.history.query.OptionQuery;
import com.nctigba.observability.sql.service.history.DataStoreService;
import com.nctigba.observability.sql.service.history.collection.CollectionItem;
import com.nctigba.observability.sql.service.history.collection.agent.CurrentCpuUsageItem;
import com.nctigba.observability.sql.service.history.point.CurrentCpuUsage;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TestCurrentCpuUsage
 *
 * @author luomeng
 * @since 2023/7/7
 */
@RunWith(MockitoJUnitRunner.class)
public class TestCurrentCpuUsage {
    @Mock
    private CurrentCpuUsageItem item;
    @Mock
    private DataStoreService dataStoreService;
    @InjectMocks
    private CurrentCpuUsage currentCpuUsage;
    private HisDiagnosisTask hisDiagnosisTask;

    @Before
    public void before() {
        String nodeId = "37e8a893-0b7e-49b2-a0b4-e6fdf7dc4345";
        Date sTime = new Date();
        Date eTime = new Date();
        OptionQuery optionQuery = new OptionQuery();
        optionQuery.setOption("IS_LOCK");
        optionQuery.setIsCheck(true);
        List<OptionQuery> config = new ArrayList<>() {{
            add(optionQuery);
        }};
        HisDiagnosisThreshold diagnosisThreshold = new HisDiagnosisThreshold();
        diagnosisThreshold.setThreshold("cpuUsageRate");
        diagnosisThreshold.setThresholdValue("20");
        List<HisDiagnosisThreshold> threshold = new ArrayList<>() {{
            add(diagnosisThreshold);
        }};
        hisDiagnosisTask = new HisDiagnosisTask();
        hisDiagnosisTask.setNodeId(nodeId);
        hisDiagnosisTask.setHisDataStartTime(sTime);
        hisDiagnosisTask.setHisDataEndTime(eTime);
        hisDiagnosisTask.setConfigs(config);
        hisDiagnosisTask.setThresholds(threshold);
        hisDiagnosisTask.setSpan("50s");
    }

    @Test
    public void testGetOption() {
        List<String> list = currentCpuUsage.getOption();
        assertNull(list);
    }

    @Test
    public void testGetDiagnosisType() {
        String type = currentCpuUsage.getDiagnosisType();
        Assertions.assertEquals(DiagnosisTypeCommon.CURRENT, type);
    }

    @Test
    public void testGetSourceDataKeys() {
        List<CollectionItem<?>> result = currentCpuUsage.getSourceDataKeys();
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(item, result.get(0));
    }

    @Test
    public void testAnalysis_NoAgentData() {
        DataStoreConfig config = mock(DataStoreConfig.class);
        when(dataStoreService.getData(item)).thenReturn(config);
        when(config.getCollectionData()).thenReturn(new ArrayList<>());
        AnalysisDTO result = currentCpuUsage.analysis(hisDiagnosisTask, dataStoreService);
        assertNotNull(result);
    }

    @Test
    public void testAnalysis_haAgentData() {
        DataStoreConfig config = mock(DataStoreConfig.class);
        config.setCollectionItem(item);
        config.setCount(1);
        when(dataStoreService.getData(item)).thenReturn(config);
        AgentData agentData = new AgentData();
        agentData.setParamName("test");
        List<AgentDTO> dbValue = new ArrayList<>();
        AgentDTO agentDTO = new AgentDTO();
        agentDTO.setCpu("10");
        dbValue.add(agentDTO);
        agentData.setDbValue(dbValue);
        agentData.setSysValue(new ArrayList<>());
        when(config.getCollectionData()).thenReturn(agentData);
        AnalysisDTO result = currentCpuUsage.analysis(hisDiagnosisTask, dataStoreService);
        Assertions.assertEquals(HisDiagnosisResult.ResultState.NO_ADVICE, result.getIsHint());
        Assertions.assertEquals(HisDiagnosisResult.PointType.CENTER, result.getPointType());
        assertNotNull(result.getPointData());
    }

    @Test
    public void testGetShowData() {
        AgentDTO agentDTO = currentCpuUsage.getShowData(1);
        assertNull(agentDTO);
    }
}
