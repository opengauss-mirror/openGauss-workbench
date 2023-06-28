/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package com.nctigba.observability.instance.util;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class Language {
    public static Locale getLocale() {
        final HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        var language = request.getHeader(HttpHeaders.CONTENT_LANGUAGE);
        Locale locale;
        if (language == null || language.isBlank())
            locale = Locale.CHINA;
        else {
            var str = language.split("_");
            locale = new Locale(str[0], str[1]);
        }
        return locale;
    }
}