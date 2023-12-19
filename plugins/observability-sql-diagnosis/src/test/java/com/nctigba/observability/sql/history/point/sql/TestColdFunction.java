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
 *  TestColdFunction.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/test/java/com/nctigba/observability/sql/history/point/sql/TestColdFunction.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.history.point.sql;

import com.nctigba.observability.sql.constant.TestTxtConstants;
import com.nctigba.observability.sql.enums.OptionEnum;
import com.nctigba.observability.sql.model.entity.DiagnosisResultDO;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.vo.DataStoreVO;
import com.nctigba.observability.sql.model.dto.point.AnalysisDTO;
import com.nctigba.observability.sql.service.DataStoreService;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.service.impl.collection.ebpf.OffCpuItem;
import com.nctigba.observability.sql.service.impl.point.sql.ColdFunction;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
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
 * TestColdFunction
 *
 * @author luomeng
 * @since 2023/9/19
 */
@RunWith(MockitoJUnitRunner.class)
public class TestColdFunction {
    @Mock
    private OffCpuItem item;
    @Mock
    private DataStoreService dataStoreService;
    @InjectMocks
    private ColdFunction pointService;

    @Test
    public void testGetOption() {
        String actual = String.valueOf(OptionEnum.IS_BCC);
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
        DataStoreVO config = mock(DataStoreVO.class);
        config.setCollectionItem(item);
        config.setCount(1);
        when(dataStoreService.getData(item)).thenReturn(config);
        String ioTraceData = TestTxtConstants.SVG;
        String originalFilename = "1offcputime.svg";
        String contentType = "text/plain";
        byte[] content = ioTraceData.getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile("file", originalFilename, contentType, content);
        when(config.getCollectionData()).thenReturn(file);
        DiagnosisTaskDO task = new DiagnosisTaskDO();
        task.setId(1);
        AnalysisDTO result = pointService.analysis(task, dataStoreService);
        Assertions.assertEquals(DiagnosisResultDO.ResultState.SUGGESTIONS, result.getIsHint());
        Assertions.assertEquals(DiagnosisResultDO.PointType.DISPLAY, result.getPointType());
        assertNotNull(result);
    }

    @Test
    public void testGetShowData() {
        Object data = pointService.getShowData(1);
        assertNull(data);
    }
}
