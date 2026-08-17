/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.tool.log4j2.desensitize.converter;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.core.pattern.PatternConverter;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Log4j2DesensitizingConverter
 *
 * @author: wangchao
 * @Date: 2025/11/27 10:23
 * @since 7.0.0-RC3
 **/
@Plugin(name = "DesensitizeConverter", category = PatternConverter.CATEGORY)
@ConverterKeys({"desensitize", "mask"})
public class Log4j2DesensitizingConverter extends LogEventPatternConverter {
    private static final String SENSITIVE_KEY_WORDS =
            "(?:password|pwd|passwd|密码|口令|密钥|秘钥|rootPassword|dbPassword|databasePassword|"
                    + "dadInstallPassword|ssInstallPassword|dbUserPassword|installUserPassword|"
                    + "secret|token|key|credential|authentication|凭证|令牌)";
    private static final Pattern KEY_VALUE_PATTERN = Pattern.compile(
            "\\b(" + SENSITIVE_KEY_WORDS + ")\\s*([:=])\\s*([^,\\s\\n\\r\"]+|\"[^\"]*\")",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern JSON_PATTERN = Pattern.compile(
            "\"(" + SENSITIVE_KEY_WORDS + ")\"\\s*:\\s*\"([^\"]*)\"",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern XML_PATTERN = Pattern.compile(
            "<(" + SENSITIVE_KEY_WORDS + ")>([^<]*)</\\1>",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    private static final Pattern SENSITIVE_KEY_CHECK = Pattern.compile(
            SENSITIVE_KEY_WORDS,
            Pattern.CASE_INSENSITIVE);
    private static final String TRUE = "true";
    private static final String FALSE = "false";
    private static final String NULL = "null";
    private static final String YES = "yes";
    private static final String NO = "no";

    protected Log4j2DesensitizingConverter(String[] options) {
        super("Desensitize", "desensitize");
    }

    /**
     * instance construct
     *
     * @param options options
     * @return converter
     */
    public static Log4j2DesensitizingConverter newInstance(final String[] options) {
        return new Log4j2DesensitizingConverter(options);
    }

    @Override
    public void format(LogEvent event, StringBuilder toAppendTo) {
        String message = event.getMessage().getFormattedMessage();
        if (message == null || message.trim().isEmpty()) {
            toAppendTo.append(message);
            return;
        }
        String desensitized = desensitizeMessage(message);
        toAppendTo.append(desensitized);
    }

    private String desensitizeMessage(String message) {
        String[] lines = message.split("\\r?\\n");
        if (lines.length == 1) {
            return desensitizeLine(lines[0]);
        }
        StringBuilder result = new StringBuilder(message.length());
        for (int i = 0; i < lines.length; i++) {
            result.append(desensitizeLine(lines[i]));
            if (i < lines.length - 1) {
                result.append('\n');
            }
        }
        return result.toString();
    }

    private String desensitizeLine(String line) {
        if (line == null || line.isEmpty()) {
            return line;
        }
        String processed = desensitizeKeyValuePairs(line);
        processed = desensitizeJson(processed);
        processed = desensitizeXml(processed);
        return processed;
    }

    private String desensitizeKeyValuePairs(String line) {
        StringBuffer result = new StringBuffer(line.length());
        Matcher matcher = KEY_VALUE_PATTERN.matcher(line);
        while (matcher.find()) {
            String key = matcher.group(1);
            String separator = matcher.group(2);
            String value = matcher.group(3);
            if (isSensitiveKey(key) && !isConstantValue(value)) {
                matcher.appendReplacement(result, key + separator + "******");
            } else {
                matcher.appendReplacement(result, matcher.group());
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private String desensitizeJson(String line) {
        StringBuffer result = new StringBuffer(line.length());
        Matcher matcher = JSON_PATTERN.matcher(line);
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2);
            if (isSensitiveKey(key) && !isConstantValue(value)) {
                matcher.appendReplacement(result, "\"" + key + "\":\"******\"");
            } else {
                matcher.appendReplacement(result, matcher.group());
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private String desensitizeXml(String line) {
        StringBuffer result = new StringBuffer(line.length());
        Matcher matcher = XML_PATTERN.matcher(line);
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2);
            if (isSensitiveKey(key) && !isConstantValue(value)) {
                matcher.appendReplacement(result, "<" + key + ">******</" + key + ">");
            } else {
                matcher.appendReplacement(result, matcher.group());
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private boolean isSensitiveKey(String key) {
        return SENSITIVE_KEY_CHECK.matcher(key).find();
    }

    private boolean isConstantValue(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        String lower = value.toLowerCase(Locale.ROOT);
        return TRUE.equals(lower) || FALSE.equals(lower) || NULL.equals(lower)
                || YES.equals(lower) || NO.equals(lower);
    }
}