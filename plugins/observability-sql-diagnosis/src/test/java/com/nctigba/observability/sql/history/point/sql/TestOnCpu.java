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
 *  TestOnCpu.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/test/java/com/nctigba/observability/sql/history/point/sql/TestOnCpu.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.history.point.sql;

import com.nctigba.observability.sql.enums.OptionEnum;
import com.nctigba.observability.sql.mapper.DiagnosisResourceMapper;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.service.DataStoreService;
import com.nctigba.observability.sql.service.impl.collection.ebpf.ProfileItem;
import com.nctigba.observability.sql.service.impl.point.sql.OnCpu;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
    public void testGetShowData() {
        Object data = pointService.getShowData(1);
        assertNull(data);
    }
}
