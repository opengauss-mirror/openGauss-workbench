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
 *  TestThresholdServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/test/java/com/nctigba/observability/sql/history/core/TestThresholdServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.history.core;

import com.nctigba.observability.sql.mapper.HisThresholdMapper;
import com.nctigba.observability.sql.model.entity.DiagnosisThresholdDO;
import com.nctigba.observability.sql.service.impl.core.ThresholdServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * TestThresholdServiceImplDO
 *
 * @author luomeng
 * @since 2023/7/5
 */
@RunWith(MockitoJUnitRunner.class)
public class TestThresholdServiceImpl {
    @Mock
    private HisThresholdMapper hisThresholdMapper;
    @InjectMocks
    private ThresholdServiceImpl service;

    @Test
    public void testSelect() {
        String diagnosisType = "sql";
        List<DiagnosisThresholdDO> list = service.select(diagnosisType);
        assertNotNull(list);
    }

    @Test
    public void testGetThresholdValue_NotNull() {
        List<String> list = new ArrayList<>();
        list.add("test");
        when(hisThresholdMapper.selectOne(any())).thenReturn(new DiagnosisThresholdDO());
        HashMap<String, String> map = service.getThresholdValue(list);
        assertNotNull(map);
    }

    @Test
    public void testGetThresholdValue_Null() {
        List<String> list = new ArrayList<>();
        list.add("test");
        when(hisThresholdMapper.selectOne(any())).thenReturn(null);
        HashMap<String, String> map = service.getThresholdValue(list);
        assertNotNull(map);
    }
}
