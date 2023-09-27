/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.history.point.sql;

import com.nctigba.common.web.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constants.history.OptionCommon;
import com.nctigba.observability.sql.mapper.DiagnosisResourceMapper;
import com.nctigba.observability.sql.model.history.DataStoreConfig;
import com.nctigba.observability.sql.model.history.HisDiagnosisResult;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.dto.AnalysisDTO;
import com.nctigba.observability.sql.service.history.DataStoreService;
import com.nctigba.observability.sql.service.history.collection.CollectionItem;
import com.nctigba.observability.sql.service.history.collection.ebpf.ProfileItem;
import com.nctigba.observability.sql.service.history.point.sql.OnCpu;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TestOnCpu
 *
 * @author luomeng
 * @since 2023/9/19
 */
@RunWith(MockitoJUnitRunner.class)
public class TestOnCpu {
    @Mock
    private DiagnosisResourceMapper resourceMapper;
    @Mock
    private ProfileItem item;
    @Mock
    private DataStoreService dataStoreService;
    @InjectMocks
    private OnCpu pointService;

    @Test
    public void testGetOption() {
        String actual = String.valueOf(OptionCommon.IS_BCC);
        List<String> list = pointService.getOption();
        assertEquals(actual, list.get(0));
    }

    @Test
    public void testGetSourceDataKeys() {
        List<CollectionItem<?>> result = pointService.getSourceDataKeys();
        assertEquals(1, result.size());
        assertEquals(item, result.get(0));
    }

    @Test
    public void testAnalysisData() {
        try {
            DataStoreConfig config = mock(DataStoreConfig.class);
            config.setCollectionItem(item);
            config.setCount(1);
            when(dataStoreService.getData(item)).thenReturn(config);
            MultipartFile file = mock(MultipartFile.class);
            when(config.getCollectionData()).thenReturn(file);
            InputStream inputStream = mock(InputStream.class);
            when(file.getInputStream()).thenReturn(inputStream);
            when(resourceMapper.insert(any())).thenReturn(1);
            HisDiagnosisTask task = new HisDiagnosisTask();
            task.setId(1);
            AnalysisDTO result = pointService.analysis(task, dataStoreService);
            Assertions.assertEquals(HisDiagnosisResult.ResultState.SUGGESTIONS, result.getIsHint());
            Assertions.assertEquals(HisDiagnosisResult.PointType.DIAGNOSIS, result.getPointType());
            assertNotNull(result);
        } catch (IOException e) {
            throw new HisDiagnosisException("error:", e);
        }
    }

    @Test
    public void testGetShowData() {
        Object data = pointService.getShowData(1);
        assertNull(data);
    }
}
