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
 *  LocaleStringUtils.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/util/LocaleStringUtils.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ReUtil;

@Component
public class LocaleStringUtils {
	private static final String I18N = "\\{\\{(.+?)\\}\\}";
	private static final Map<Locale, ObjectMapper> OBJECTMAPPERS = new HashMap<>();
	private static final Map<Locale, ObjectMapper> OBJECTMAPPERSWITHOUTTYPE = new HashMap<>();
	private static MessageSource messageSource;

	@Autowired
	public void setMessageSource(MessageSource messageSource) {
		LocaleStringUtils.messageSource = messageSource;
	}

	public static String trapLanguage(String str) {
		Locale locale = getLocale();
		return messageSource.getMessage(str, null, "{{" + str + "}}", locale);
	}

	public <T, V> V trapLanguage(T obj) {
		return trapLanguage(obj, obj.getClass());
	}

	@SuppressWarnings("unchecked")
	public <T, V> V trapLanguage(T obj, Class<?> clazz) {
		Locale locale = getLocale();
		var objectMapper = getObjectMapper(locale, clazz != ObjectNode.class);
		try {
			String r = objectMapper.writeValueAsString(obj);
			return (V) objectMapper.readValue(r, clazz);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static Locale getLocale() {
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

	private ObjectMapper getObjectMapper(Locale locale, boolean withType) {
		if (withType && OBJECTMAPPERS.containsKey(locale))
			return OBJECTMAPPERS.get(locale);
		if (!withType && OBJECTMAPPERSWITHOUTTYPE.containsKey(locale))
			return OBJECTMAPPERSWITHOUTTYPE.get(locale);
		var objectMapper = new ObjectMapper();
		var module = new SimpleModule();
		module.addSerializer(String.class, new JsonSerializer<String>() {
			@Override
			public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
				// {{i18n, key, param1, param2}}
				// =>
				// help: value1 is param1%, value2 is param2%
				String replaceAll = ReUtil.replaceAll(value, I18N, m -> {
					String group = m.group(1);
					var s = group.split(",");
					var key = s[1].trim();
					String[] args;
					if (s.length == 2)
						args = null;
					else
						args = Arrays.copyOfRange(s, 2, s.length);
					return messageSource.getMessage(key, args, "{{" + key + "}}", locale);
				});
				gen.writeString(replaceAll);
			}

			@Override
			public void serializeWithType(String value, JsonGenerator gen, SerializerProvider serializers,
					TypeSerializer typeSer) throws IOException {
				serialize(value, gen, serializers);
			}

			@Override
			public Class<String> handledType() {
				return String.class;
			}
		});
		objectMapper.registerModule(module);
		if (withType) {
			objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
					ObjectMapper.DefaultTyping.NON_FINAL, As.PROPERTY);
			OBJECTMAPPERS.put(locale, objectMapper);
		} else {
			OBJECTMAPPERSWITHOUTTYPE.put(locale, objectMapper);
		}
		return objectMapper;
	}

	public static final String format(Object... str) {
		Assert.isTrue(str.length > 0);
		return "{{i18n," + StringUtils.join(str, ',') + "}}";
	}
}