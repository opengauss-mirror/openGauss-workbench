/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.history.core;

import com.nctigba.observability.sql.mapper.history.HisThresholdMapper;
import com.nctigba.observability.sql.model.history.HisDiagnosisThreshold;
import com.nctigba.observability.sql.model.history.query.HisThresholdQuery;
import com.nctigba.observability.sql.service.history.core.HisThresholdServiceImpl;
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
 * TestHisThresholdServiceImpl
 *
 * @author luomeng
 * @since 2023/7/5
 */
@RunWith(MockitoJUnitRunner.class)
public class TestHisThresholdServiceImpl {
    @Mock
    private HisThresholdMapper hisThresholdMapper;
    @InjectMocks
    private HisThresholdServiceImpl service;

    @Test
    public void testSelect() {
        String diagnosisType = "sql";
        List<HisDiagnosisThreshold> list = service.select(diagnosisType);
        assertNotNull(list);
    }

    @Test
    public void testInsert() {
        service.insertOrUpdate(new HisThresholdQuery());
    }

    @Test
    public void testUpdate() {
        HisThresholdQuery query = new HisThresholdQuery();
        query.setId(1);
        service.insertOrUpdate(query);
    }

    @Test
    public void testDelete() {
        service.delete(1);
    }

    @Test
    public void testGetThresholdValue_NotNull() {
        List<String> list = new ArrayList<>();
        list.add("test");
        when(hisThresholdMapper.selectOne(any())).thenReturn(new HisDiagnosisThreshold());
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
