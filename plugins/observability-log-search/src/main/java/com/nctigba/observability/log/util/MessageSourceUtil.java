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
 *  MessageSourceUtil.java
 *
 *  IDENTIFICATION
 *  plugins/observability-log-search/src/main/java/com/nctigba/observability/log/util/MessageSourceUtil.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.log.util;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import cn.hutool.core.util.StrUtil;

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
		if(LOC.get()!=null)
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
}