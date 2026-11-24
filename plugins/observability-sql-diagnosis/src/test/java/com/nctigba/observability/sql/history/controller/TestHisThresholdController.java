/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package com.nctigba.observability.sql.history.controller;

import com.nctigba.observability.sql.constant.DiagnosisTypeConstants;
import com.nctigba.observability.sql.controller.ThresholdController;
import com.nctigba.observability.sql.model.entity.DiagnosisThresholdDO;
import com.nctigba.observability.sql.service.ThresholdService;
import com.nctigba.observability.sql.util.LocaleStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TestHisThresholdControllerDO
 *
 * @author luomeng
 * @since 2023/8/24
 */
@RunWith(MockitoJUnitRunner.class)
public class TestHisThresholdController {
    @Mock
    private ThresholdService service;
    @Mock
    private LocaleStringUtils localeToString;
    @InjectMocks
    private ThresholdController controller;

    @Test
    public void testSelect() {
        DiagnosisThresholdDO threshold = mock(DiagnosisThresholdDO.class);
        List<DiagnosisThresholdDO> list = new ArrayList<>(Collections.singleton(threshold));
        String diagnosisType = DiagnosisTypeConstants.SQL;
        when(service.select(diagnosisType)).thenReturn(list);
        Object result = controller.select(diagnosisType);
        assertNotNull(result);
    }
}
