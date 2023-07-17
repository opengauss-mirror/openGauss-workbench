/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.history.point;

import com.nctigba.observability.sql.constants.history.OptionCommon;
import com.nctigba.observability.sql.model.history.DataStoreConfig;
import com.nctigba.observability.sql.model.history.HisDiagnosisResult;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.dto.AnalysisDTO;
import com.nctigba.observability.sql.model.history.dto.LogDetailInfoDTO;
import com.nctigba.observability.sql.model.history.dto.LogInfoDTO;
import com.nctigba.observability.sql.model.history.point.LockDTO;
import com.nctigba.observability.sql.service.history.DataStoreService;
import com.nctigba.observability.sql.service.history.collection.CollectionItem;
import com.nctigba.observability.sql.service.history.collection.elastic.LockTimeoutItem;
import com.nctigba.observability.sql.service.history.point.LockTimeout;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TestLockAnalysis
 *
 * @author luomeng
 * @since 2023/7/7
 */
@RunWith(MockitoJUnitRunner.class)
public class TestLockTimeout {
    @Mock
    private LockTimeoutItem item;
    @Mock
    private DataStoreService dataStoreService;
    @InjectMocks
    private LockTimeout lockTimeout;

    @Test
    public void testGetOption() {
        List<String> list = lockTimeout.getOption();
        list.add(String.valueOf(OptionCommon.IS_LOCK));
        assertNotNull(list);
    }

    @Test
    public void testGetSourceDataKeys() {
        List<CollectionItem<?>> result = lockTimeout.getSourceDataKeys();
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(item, result.get(0));
    }

    @Test
    public void testAnalysis_NoEsData() {
        DataStoreConfig config = mock(DataStoreConfig.class);
        when(dataStoreService.getData(item)).thenReturn(config);
        when(config.getCollectionData()).thenReturn(new Object());
        AnalysisDTO result = lockTimeout.analysis(mock(HisDiagnosisTask.class), dataStoreService);
        Assertions.assertEquals(HisDiagnosisResult.ResultState.NO_ADVICE, result.getIsHint());
        Assertions.assertEquals(HisDiagnosisResult.PointType.DIAGNOSIS, result.getPointType());
        assertNull(result.getPointData());
    }

    @Test
    public void testAnalysis_hasEsData() {
        DataStoreConfig config = mock(DataStoreConfig.class);
        config.setCollectionItem(item);
        config.setCount(1);
        when(dataStoreService.getData(item)).thenReturn(config);
        LogInfoDTO logInfoDTO = new LogInfoDTO();
        List<LogDetailInfoDTO> logList = new ArrayList<>();
        LogDetailInfoDTO infoDTO = new LogDetailInfoDTO();
        infoDTO.setLogData(new Object());
        infoDTO.setId("1");
        infoDTO.setLogTime(new Object());
        infoDTO.setLogClusterId("");
        infoDTO.setLogLevel(new Object());
        infoDTO.setLogNodeId("");
        infoDTO.setLogType(new Object());
        logList.add(infoDTO);
        logInfoDTO.setLogs(logList);
        logInfoDTO.setScrollId("");
        logInfoDTO.setSorts(new ArrayList<>());
        when(config.getCollectionData()).thenReturn(logInfoDTO);
        AnalysisDTO result = lockTimeout.analysis(mock(HisDiagnosisTask.class), dataStoreService);
        Assertions.assertEquals(HisDiagnosisResult.ResultState.SUGGESTIONS, result.getIsHint());
        Assertions.assertEquals(HisDiagnosisResult.PointType.DIAGNOSIS, result.getPointType());
        assertNotNull(result.getPointData());
    }

    @Test
    public void testGetShowData() {
        LockDTO data = lockTimeout.getShowData(1);
        assertNull(data);
    }
}
