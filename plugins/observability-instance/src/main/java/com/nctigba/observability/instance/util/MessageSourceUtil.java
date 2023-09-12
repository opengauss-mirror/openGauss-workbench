/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package com.nctigba.observability.instance.util;

import cn.hutool.core.util.StrUtil;
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
    private static final ThreadLocal<Locale> LOC = new ThreadLocal<>();

    @Autowired
    public void setMessageSource(MessageSource messageSource) {
        MessageSourceUtil.messageSource = messageSource;
    }

    public static void set(String language) {
        Locale locale;
        if (StrUtil.isBlank(language))
            locale = Locale.CHINA;
        else {
            var str = language.split("_");
            locale = new Locale(str[0], str[1]);
        }
        LOC.set(locale);
    }

    public static void set(Locale locale) {
        LOC.set(locale);
    }

    public static String get(String key) {
        return messageSource.getMessage(key, null, key, getLocale());
    }

    public static String get(String key, Object... objs) {
        return messageSource.getMessage(key, objs, key, getLocale());
    }

    public static void reset() {
        LOC.remove();
    }

    private static Locale getLocale() {
        if (LOC.get() != null)
            return LOC.get();
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
        LOC.set(locale);
        return locale;
    }

    public static String getMsg(String key) {
        return messageSource.getMessage(key, null, null, getRequestLocale());
    }

    public static String getMsg(String key, Object... objs) {
        return messageSource.getMessage(key, objs, key, getRequestLocale());
    }

    private static Locale getRequestLocale() {
        final HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        return request.getLocale();
    }
}