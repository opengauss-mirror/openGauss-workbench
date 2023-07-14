/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

@Component
public class MessageSourceUtil {
    private static MessageSource messageSource;

    @Autowired
    public void setMessageSource(MessageSource messageSource) {
        MessageSourceUtil.messageSource = messageSource;
    }

    public static String get(String key) {
        return messageSource.getMessage(key, null, key, getLocale());
    }

    private static Locale getLocale() {
        String language = null;
        if (RequestContextHolder.currentRequestAttributes() instanceof ServletRequestAttributes) {
            final HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            language = request.getHeader(HttpHeaders.CONTENT_LANGUAGE);
        }
        Locale locale;
        if (language == null || language.isBlank()) {
            locale = Locale.CHINA;
        } else {
            String[] str = language.split("_");
            locale = new Locale(str[0], str[1]);
        }
        return locale;
    }
}