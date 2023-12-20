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
 *  TestHttpUtils.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/test/java/com/nctigba/observability/sql/history/util/TestHttpUtils.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.history.util;

import com.nctigba.observability.sql.util.HttpUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

/**
 * TestHttpUtils
 *
 * @author luomeng
 * @since 2023/7/14
 */
@RunWith(MockitoJUnitRunner.class)
public class TestHttpUtils {
    @Test
    public void testSendGet() {
        String result = "";
        try (MockedStatic<HttpUtils> mockStatic = mockStatic(HttpUtils.class)) {
            mockStatic.when(() -> HttpUtils.sendGet(anyString(), anyString())).thenReturn(result);
        }
    }

    @Test
    public void testSendGet2() {
        String result = "";
        try (MockedStatic<HttpUtils> mockStatic = mockStatic(HttpUtils.class)) {
            mockStatic.when(() -> HttpUtils.sendGet(anyString(), anyString(), anyString())).thenReturn(result);
        }
    }
}
