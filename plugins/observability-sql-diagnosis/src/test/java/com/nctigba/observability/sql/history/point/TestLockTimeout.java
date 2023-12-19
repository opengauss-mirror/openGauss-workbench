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
 *  TestLockTimeout.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/test/java/com/nctigba/observability/sql/history/point/TestLockTimeout.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.history.point;

import com.nctigba.observability.sql.enums.OptionEnum;
import com.nctigba.observability.sql.model.entity.DiagnosisResultDO;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.vo.DataStoreVO;
import com.nctigba.observability.sql.model.dto.point.AnalysisDTO;
import com.nctigba.observability.sql.model.vo.point.LogDetailInfoVO;
import com.nctigba.observability.sql.model.vo.point.LogInfoVO;
import com.nctigba.observability.sql.model.dto.point.LockDTO;
import com.nctigba.observability.sql.service.DataStoreService;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.service.impl.collection.elastic.LockTimeoutItem;
import com.nctigba.observability.sql.service.impl.point.history.LockTimeout;
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
        list.add(String.valueOf(OptionEnum.IS_LOCK));
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
        DataStoreVO config = mock(DataStoreVO.class);
        when(dataStoreService.getData(item)).thenReturn(config);
        when(config.getCollectionData()).thenReturn(new Object());
        AnalysisDTO result = lockTimeout.analysis(mock(DiagnosisTaskDO.class), dataStoreService);
        Assertions.assertEquals(DiagnosisResultDO.ResultState.NO_ADVICE, result.getIsHint());
        Assertions.assertEquals(DiagnosisResultDO.PointType.DIAGNOSIS, result.getPointType());
        assertNull(result.getPointData());
    }

    @Test
    public void testAnalysis_hasEsData() {
        DataStoreVO config = mock(DataStoreVO.class);
        config.setCollectionItem(item);
        config.setCount(1);
        when(dataStoreService.getData(item)).thenReturn(config);
        LogInfoVO logInfoVO = new LogInfoVO();
        List<LogDetailInfoVO> logList = new ArrayList<>();
        LogDetailInfoVO infoDTO = new LogDetailInfoVO();
        infoDTO.setLogData(new Object());
        infoDTO.setId("1");
        infoDTO.setLogTime(new Object());
        infoDTO.setLogClusterId("");
        infoDTO.setLogLevel(new Object());
        infoDTO.setLogNodeId("");
        infoDTO.setLogType(new Object());
        logList.add(infoDTO);
        logInfoVO.setLogs(logList);
        logInfoVO.setScrollId("");
        logInfoVO.setSorts(new ArrayList<>());
        when(config.getCollectionData()).thenReturn(logInfoVO);
        AnalysisDTO result = lockTimeout.analysis(mock(DiagnosisTaskDO.class), dataStoreService);
        Assertions.assertEquals(DiagnosisResultDO.ResultState.SUGGESTIONS, result.getIsHint());
        Assertions.assertEquals(DiagnosisResultDO.PointType.DIAGNOSIS, result.getPointType());
        assertNotNull(result.getPointData());
    }

    @Test
    public void testGetShowData() {
        LockDTO data = lockTimeout.getShowData(1);
        assertNull(data);
    }
}
