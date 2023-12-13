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
 *  MessageSourceUtils.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/util/MessageSourceUtils.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

@Component
public class MessageSourceUtils {
    private static MessageSource messageSource;

    @Autowired
    public void setMessageSource(MessageSource messageSource) {
        MessageSourceUtils.messageSource = messageSource;
    }

    /**
     * i18n by key
     *
     * @param key String
     * @return String
     */
    public static String get(String key) {
        return messageSource.getMessage(key, null, key, getLocale());
    }

    /**
     * i18n by key and locale
     *
     * @param key String
     * @param locale Locale
     * @return String
     */
    public static String get(String key, Locale locale) {
        return messageSource.getMessage(key, null, key, locale);
    }

    /**
     *  get Locale
     *
     * @return Locale
     */
    public static Locale getLocale() {
        String language = null;
        if (RequestContextHolder.currentRequestAttributes() instanceof ServletRequestAttributes) {
            final HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            language = request.getHeader(HttpHeaders.CONTENT_LANGUAGE);
        }
        Locale locale;
        if (language == null || language.isBlank()) {
            locale = Locale.getDefault();
        } else {
            String[] str = language.split("_");
            locale = new Locale(str[0], str[1]);
        }
        return locale;
    }
}