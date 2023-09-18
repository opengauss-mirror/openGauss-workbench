/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.history.controller;

import com.nctigba.observability.sql.constants.history.DiagnosisTypeCommon;
import com.nctigba.observability.sql.controller.HisThresholdController;
import com.nctigba.observability.sql.model.history.HisDiagnosisThreshold;
import com.nctigba.observability.sql.model.history.query.HisThresholdQuery;
import com.nctigba.observability.sql.service.history.HisThresholdService;
import com.nctigba.observability.sql.util.LocaleString;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TestHisThresholdController
 *
 * @author luomeng
 * @since 2023/8/24
 */
@RunWith(MockitoJUnitRunner.class)
public class TestHisThresholdController {
    @Mock
    private HisThresholdService service;
    @Mock
    private LocaleString localeToString;
    @InjectMocks
    private HisThresholdController controller;

    @Test
    public void testSelect() {
        HisDiagnosisThreshold threshold = mock(HisDiagnosisThreshold.class);
        List<HisDiagnosisThreshold> list = new ArrayList<>(Collections.singleton(threshold));
        String diagnosisType = DiagnosisTypeCommon.SQL;
        when(service.select(diagnosisType)).thenReturn(list);
        Object result = controller.select(diagnosisType);
        assertNotNull(result);
    }

    @Test
    public void testInsertOrUpdate() {
        HisThresholdQuery hisThresholdQuery = mock(HisThresholdQuery.class);
        doNothing().when(service).insertOrUpdate(hisThresholdQuery);
        controller.insertOrUpdate(hisThresholdQuery);
    }

    @Test
    public void testDelete() {
        int thresholdId = 1;
        doNothing().when(service).delete(thresholdId);
        controller.delete(thresholdId);
    }
}
