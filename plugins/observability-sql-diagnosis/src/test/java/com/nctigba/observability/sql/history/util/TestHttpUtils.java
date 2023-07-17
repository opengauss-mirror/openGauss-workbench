/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
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
