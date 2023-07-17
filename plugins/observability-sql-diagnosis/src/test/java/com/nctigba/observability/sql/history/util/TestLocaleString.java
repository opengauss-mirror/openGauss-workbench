/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.history.util;

import com.nctigba.observability.sql.util.LocaleString;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * TestLocaleString
 *
 * @author luomeng
 * @since 2023/7/14
 */
//@RunWith(MockitoJUnitRunner.class)
public class TestLocaleString {
    @InjectMocks
    private LocaleString localeString;

    /*@Test
    public void TestSetMessageSource() {
        localeString.setMessageSource(any());
    }

    @Test
    public void TestTrapLanguage() {
        try (MockedStatic<RequestContextHolder> mockStatic = mockStatic(RequestContextHolder.class);
             MockedStatic<LocaleString> mock = mockStatic(LocaleString.class)) {
            ServletRequestAttributes attributes = mock(ServletRequestAttributes.class);
            mockStatic.when(RequestContextHolder::currentRequestAttributes)
                    .thenReturn(attributes);
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(attributes.getRequest()).thenReturn(request);
            mock.when()
            String str = "test";
            localeString.trapLanguage(str);
        }
    }

    @Test
    public void TestTrapLanguage2() {
        localeString.setMessageSource(any());
    }

    @Test
    public void TestTrapLanguage3() {
        localeString.setMessageSource(any());
    }

    @Test
    public void TestGetLocale() {
        localeString.setMessageSource(any());
    }

    @Test
    public void TestGetObjectMapper() {
        localeString.setMessageSource(any());
    }

    @Test
    public void TestFormat() {
        localeString.setMessageSource(any());
    }*/
}
