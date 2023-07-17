/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.util;

import com.nctigba.datastudio.base.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Locale;

/**
 * LocaleString
 *
 * @since 2023-6-26
 */
@Slf4j
@Component
public class LocaleString {
    private static MessageSource messageSource;

    @Autowired
    public void setMessageSource(MessageSource messageSource) {
        LocaleString.messageSource = messageSource;
    }

    /**
     * trans language
     *
     * @param str str
     * @return String
     */
    public static String transLanguage(String str) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        String language = requestAttributes.getRequest().getHeader(HttpHeaders.ACCEPT_LANGUAGE);
        return translate(str, language);
    }

    /**
     * trans language websocket
     *
     * @param str str
     * @param webSocketServer webSocketServer
     * @return String
     */
    public static String transLanguageWs(String str, WebSocketServer webSocketServer) {
        String language = webSocketServer.getLanguage();
        return translate(str, language);
    }

    /**
     * translate
     *
     * @param str str
     * @param language language
     * @return String
     */
    private static String translate(String str, String language) {
        log.info("LocaleString language is: " + language);
        Locale locale;
        if (StringUtils.isEmpty(language)) {
            locale = Locale.CHINA;
        } else {
            String[] split = language.split("-");
            locale = new Locale(split[0], split[1]);
        }
        return messageSource.getMessage(str, null, locale);
    }

}