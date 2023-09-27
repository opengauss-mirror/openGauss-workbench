/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.history.point.sql;

import com.nctigba.observability.sql.constants.TestTxtCommon;
import com.nctigba.observability.sql.constants.history.OptionCommon;
import com.nctigba.observability.sql.model.history.DataStoreConfig;
import com.nctigba.observability.sql.model.history.HisDiagnosisResult;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.dto.AnalysisDTO;
import com.nctigba.observability.sql.service.history.DataStoreService;
import com.nctigba.observability.sql.service.history.collection.CollectionItem;
import com.nctigba.observability.sql.service.history.collection.ebpf.XfsDistItem;
import com.nctigba.observability.sql.service.history.point.sql.XfsDist;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TestXfsDist
 *
 * @author luomeng
 * @since 2023/9/15
 */
@RunWith(MockitoJUnitRunner.class)
public class TestXfsDist {
    @Mock
    private XfsDistItem item;
    @Mock
    private DataStoreService dataStoreService;
    @InjectMocks
    private XfsDist pointService;

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
    public void testAnalysis() {
        DataStoreConfig config = mock(DataStoreConfig.class);
        config.setCollectionItem(item);
        config.setCount(1);
        when(dataStoreService.getData(item)).thenReturn(config);
        String ioTraceData = TestTxtCommon.XFSDIST;
        String originalFilename = "test.txt";
        String contentType = "text/plain";
        byte[] content = ioTraceData.getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile("file", originalFilename, contentType, content);
        when(config.getCollectionData()).thenReturn(file);
        AnalysisDTO result = pointService.analysis(mock(HisDiagnosisTask.class), dataStoreService);
        assertEquals(HisDiagnosisResult.ResultState.SUGGESTIONS, result.getIsHint());
        assertEquals(HisDiagnosisResult.PointType.DIAGNOSIS, result.getPointType());
        assertNotNull(result.getPointData());
    }


    @Test
    public void testGetShowData() {
        Object result = pointService.getShowData(1);
        assertNull(result);
    }
}
