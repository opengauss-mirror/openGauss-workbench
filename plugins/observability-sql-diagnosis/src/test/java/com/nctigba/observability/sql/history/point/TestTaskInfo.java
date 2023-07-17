/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.history.point;

import com.nctigba.common.web.exception.HisDiagnosisException;
import com.nctigba.observability.sql.mapper.history.HisDiagnosisTaskMapper;
import com.nctigba.observability.sql.model.history.HisDiagnosisResult;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.HisDiagnosisThreshold;
import com.nctigba.observability.sql.model.history.dto.AnalysisDTO;
import com.nctigba.observability.sql.model.history.query.OptionQuery;
import com.nctigba.observability.sql.service.ClusterManager;
import com.nctigba.observability.sql.service.history.DataStoreService;
import com.nctigba.observability.sql.service.history.collection.CollectionItem;
import com.nctigba.observability.sql.service.history.point.TaskInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
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
 * TestTaskInfo
 *
 * @author luomeng
 * @since 2023/7/7
 */
@RunWith(MockitoJUnitRunner.class)
public class TestTaskInfo {
    @Mock
    private DataStoreService dataStoreService;
    @Mock
    private HisDiagnosisTaskMapper taskMapper;
    @Mock
    private ClusterManager clusterManager;
    @InjectMocks
    private TaskInfo taskInfo;

    private HisDiagnosisTask hisDiagnosisTask;

    @Before
    public void before() {
        OptionQuery optionQuery = new OptionQuery();
        optionQuery.setOption("IS_LOCK");
        optionQuery.setIsCheck(true);
        HisDiagnosisThreshold diagnosisThreshold = new HisDiagnosisThreshold();
        diagnosisThreshold.setThreshold("cpuUsageRate");
        diagnosisThreshold.setThresholdValue("20");
        hisDiagnosisTask = new HisDiagnosisTask();
        String nodeId = "37e8a893-0b7e-49b2-a0b4-e6fdf7dc4345";
        Date sTime = new Date();
        Date eTime = new Date();
        hisDiagnosisTask.setNodeId(nodeId);
        hisDiagnosisTask.setHisDataStartTime(sTime);
        hisDiagnosisTask.setHisDataEndTime(eTime);
        List<OptionQuery> config = new ArrayList<>() {{
            add(optionQuery);
        }};
        hisDiagnosisTask.setConfigs(config);
        List<HisDiagnosisThreshold> threshold = new ArrayList<>() {{
            add(diagnosisThreshold);
        }};
        hisDiagnosisTask.setThresholds(threshold);
        hisDiagnosisTask.setSpan("50s");
    }

    @Test
    public void testGetOption() {
        List<String> list = taskInfo.getOption();
        assertNull(list);
    }

    @Test
    public void testGetSourceDataKeys() {
        List<CollectionItem<?>> result = taskInfo.getSourceDataKeys();
        assertNull(result);
    }

    @Test
    public void testAnalysisData() {
        AnalysisDTO result = taskInfo.analysis(mock(HisDiagnosisTask.class), dataStoreService);
        Assertions.assertEquals(HisDiagnosisResult.ResultState.SUGGESTIONS, result.getIsHint());
        Assertions.assertEquals(HisDiagnosisResult.PointType.ROOT, result.getPointType());
        assertNotNull(result);
    }

    @Test
    public void testGetShowData_Exception() {
        int taskId = 1;
        when(taskMapper.selectById(taskId)).thenReturn(null);
        assertThrows(HisDiagnosisException.class, () -> taskInfo.getShowData(taskId));
    }

    @Test
    public void testGetShowData() {
        int taskId = 1;
        when(taskMapper.selectById(taskId)).thenReturn(hisDiagnosisTask);
        List<OpsClusterVO> clusterVOList = new ArrayList<>();
        OpsClusterVO opsClusterVO = new OpsClusterVO();
        opsClusterVO.setClusterId("");
        opsClusterVO.setClusterName("");
        List<OpsClusterNodeVO> clusterNodes = new ArrayList<>();
        OpsClusterNodeVO nodeVO = new OpsClusterNodeVO();
        nodeVO.setNodeId("37e8a893-0b7e-49b2-a0b4-e6fdf7dc4345");
        clusterNodes.add(nodeVO);
        opsClusterVO.setClusterNodes(clusterNodes);
        clusterVOList.add(opsClusterVO);
        when(clusterManager.getAllOpsCluster()).thenReturn(clusterVOList);
        HisDiagnosisTask task = taskInfo.getShowData(1);
        assertNotNull(task);
    }
}
