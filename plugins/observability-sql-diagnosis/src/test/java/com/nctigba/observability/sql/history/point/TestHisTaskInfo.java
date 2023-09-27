/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.history.point;

import com.nctigba.common.web.exception.HisDiagnosisException;
import com.nctigba.observability.sql.mapper.history.HisDiagnosisTaskMapper;
import com.nctigba.observability.sql.model.history.HisDiagnosisResult;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.dto.AnalysisDTO;
import com.nctigba.observability.sql.service.ClusterManager;
import com.nctigba.observability.sql.service.history.DataStoreService;
import com.nctigba.observability.sql.service.history.collection.CollectionItem;
import com.nctigba.observability.sql.service.history.point.HisTaskInfo;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TestHisTaskInfo
 *
 * @author luomeng
 * @since 2023/9/17
 */
@RunWith(MockitoJUnitRunner.class)
public class TestHisTaskInfo {
    @Mock
    private DataStoreService dataStoreService;
    @Mock
    private HisDiagnosisTaskMapper taskMapper;
    @Mock
    private ClusterManager clusterManager;
    @InjectMocks
    private HisTaskInfo pointService;

    @Test
    public void testGetOption() {
        List<String> list = pointService.getOption();
        assertNull(list);
    }

    @Test
    public void testGetSourceDataKeys() {
        List<CollectionItem<?>> result = pointService.getSourceDataKeys();
        assertNull(result);
    }

    @Test
    public void testAnalysisData() {
        AnalysisDTO result = pointService.analysis(mock(HisDiagnosisTask.class), dataStoreService);
        Assertions.assertEquals(HisDiagnosisResult.ResultState.SUGGESTIONS, result.getIsHint());
        Assertions.assertEquals(HisDiagnosisResult.PointType.ROOT, result.getPointType());
        assertNotNull(result);
    }

    @Test
    public void testGetShowData() {
        HisDiagnosisTask task = new HisDiagnosisTask();
        task.setNodeId("1");
        when(taskMapper.selectById(1)).thenReturn(task);
        OpsClusterNodeVO nodeVO = new OpsClusterNodeVO();
        nodeVO.setNodeId("1");
        nodeVO.setDataPath("test");
        nodeVO.setAzAddress("test");
        nodeVO.setAzName("test");
        nodeVO.setClusterRole("test");
        nodeVO.setDbName("test");
        nodeVO.setDbPort(8080);
        nodeVO.setDbUser("test");
        nodeVO.setDbUserPassword("test");
        nodeVO.setHostId("test");
        nodeVO.setHostname("test");
        nodeVO.setPublicIp("test");
        nodeVO.setPrivateIp("test");
        nodeVO.setIsRemember(true);
        nodeVO.setInstallUserName("test");
        nodeVO.setHostPort(8080);
        List<OpsClusterNodeVO> nodeVOList = new ArrayList<>();
        nodeVOList.add(nodeVO);
        OpsClusterVO opsClusterVO = new OpsClusterVO();
        opsClusterVO.setClusterNodes(nodeVOList);
        List<OpsClusterVO> list = new ArrayList<>();
        list.add(opsClusterVO);
        when(clusterManager.getAllOpsCluster()).thenReturn(list);
        HisDiagnosisTask data = pointService.getShowData(1);
        assertEquals(opsClusterVO, data.getNodeVOSub());
        when(taskMapper.selectById(1)).thenReturn(null);
        try {
            pointService.getShowData(1);
        } catch (HisDiagnosisException e) {
            assertEquals("taskId is not exists!", e.getMessage());
        }
    }
}
