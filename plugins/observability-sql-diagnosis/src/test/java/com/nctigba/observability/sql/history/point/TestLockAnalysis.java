/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.history.point;

import com.nctigba.observability.sql.constants.history.OptionCommon;
import com.nctigba.observability.sql.model.history.HisDiagnosisResult;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.dto.AnalysisDTO;
import com.nctigba.observability.sql.service.history.DataStoreService;
import com.nctigba.observability.sql.service.history.collection.CollectionItem;
import com.nctigba.observability.sql.service.history.point.LockAnalysis;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

/**
 * TestLockTimeout
 *
 * @author luomeng
 * @since 2023/7/7
 */
@RunWith(MockitoJUnitRunner.class)
public class TestLockAnalysis {
    @Mock
    private DataStoreService dataStoreService;
    @InjectMocks
    private LockAnalysis lockAnalysis;

    @Test
    public void testGetOption() {
        List<String> list = lockAnalysis.getOption();
        list.add(String.valueOf(OptionCommon.IS_LOCK));
        assertNotNull(list);
    }

    @Test
    public void testGetSourceDataKeys() {
        List<CollectionItem<?>> result = lockAnalysis.getSourceDataKeys();
        assertNull(result);
    }

    @Test
    public void testAnalysisData() {
        AnalysisDTO result = lockAnalysis.analysis(mock(HisDiagnosisTask.class), dataStoreService);
        Assertions.assertEquals(HisDiagnosisResult.ResultState.NO_ADVICE, result.getIsHint());
        Assertions.assertEquals(HisDiagnosisResult.PointType.CENTER, result.getPointType());
        assertNotNull(result);
    }

    @Test
    public void testGetShowData() {
        String data = lockAnalysis.getShowData(1);
        assertNull(data);
    }
}
