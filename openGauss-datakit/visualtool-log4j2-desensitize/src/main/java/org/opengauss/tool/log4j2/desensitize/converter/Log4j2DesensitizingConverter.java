/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.tool.log4j2.desensitize.converter;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.core.pattern.PatternConverter;

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
    private static final Pattern KEY_VALUE_PATTERN = Pattern.compile(
        "(\\b(?:password|pwd|passwd|密码|口令|密钥|秘钥|rootPassword|dbPassword|databasePassword|"
            + "dadInstallPassword|ssInstallPassword|dbUserPassword|installUserPassword|"
            + "secret|token|key|credential|authentication|凭证|令牌)\\b)\\s*[:=]\\s*([^,\\s\\n\\r\"]+|\"[^\"]*\")",
        Pattern.CASE_INSENSITIVE);
    private static final Pattern XML_PATTERN = Pattern.compile("<([^>]+)>([^<]*)</\\1>", Pattern.MULTILINE);
    private static final Pattern JSON_PATTERN = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"([^\"]*)\"", Pattern.MULTILINE);
    private static final Pattern SENSITIVE_KEY_PATTERN = Pattern.compile(
        "(?i)(password|pwd|passwd|密码|口令|密钥|秘钥|rootPassword|dbPassword|databasePassword|"
            + "dadInstallPassword|ssInstallPassword|dbUserPassword|installUserPassword|"
            + "secret|token|key|credential|authentication|凭证|令牌)");
    private static final Pattern SENSITIVE_VALUE_PATTERN = Pattern.compile(
        "(?:(?:password|pwd|passwd|secret|token|key|credential)\\s*[:=]\\s*)([^,\\s\\n\\r\"]{6,20})",
        Pattern.CASE_INSENSITIVE);

    /**
     * Log4j2DesensitizingConverter
     *
     * @param options options
     */
    protected Log4j2DesensitizingConverter(String[] options) {
        super("Desensitize", "desensitize");
    }

    /**
     * Log4j2DesensitizingConverter
     *
     * @param options options
     * @return Log4j2DesensitizingConverter
     */
    public static Log4j2DesensitizingConverter newInstance(final String[] options) {
        return new Log4j2DesensitizingConverter(options);
    }

    @Override
    public void format(LogEvent event, StringBuilder toAppendTo) {
        String message = event.getMessage().getFormattedMessage();
        String desensitized = desensitizeComplexMessage(message);
        if (toAppendTo.toString().contains("%enc{")) {
            desensitized = processEncFormat(desensitized);
        }
        toAppendTo.append(desensitized);
    }

    private String desensitizeComplexMessage(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        String[] lines = message.split("\\r?\\n");
        if (lines.length == 1) {
            return desensitizeSingleLineMessage(message);
        }
        StringBuilder result = new StringBuilder();
        for (String line : lines) {
            result.append(desensitizeSingleLineMessage(line)).append("\n");
        }
        if (!result.isEmpty()) {
            result.setLength(result.length() - 1);
        }
        return result.toString();
    }

    private String desensitizeSingleLineMessage(String line) {
        String processed = desensitizeKeyValuePairs(line);
        if (processed.equals(line)) {
            processed = desensitizeSensitiveValues(line);
            processed = desensitizeJson(processed);
            processed = desensitizeXml(processed);
        }
        return processed;
    }

    private String desensitizeJson(String message) {
        StringBuffer result = new StringBuffer();
        Matcher matcher = JSON_PATTERN.matcher(message);
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2);
            if (isSensitiveKey(key)) {
                matcher.appendReplacement(result, "\"" + key + "\":\"******\"");
            } else {
                matcher.appendReplacement(result, matcher.group());
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private String desensitizeXml(String message) {
        StringBuffer result = new StringBuffer();
        Matcher matcher = XML_PATTERN.matcher(message);
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2);
            if (isSensitiveKey(key)) {
                matcher.appendReplacement(result, "<" + key + ">******</" + key + ">");
            } else {
                matcher.appendReplacement(result, matcher.group());
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private String desensitizeKeyValuePairs(String line) {
        StringBuffer result = new StringBuffer();
        Matcher matcher = KEY_VALUE_PATTERN.matcher(line);
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2);
            if (isSensitiveKey(key)) {
                matcher.appendReplacement(result, key + "=" + "******");
            } else if (isSensitiveValue(value)) {
                matcher.appendReplacement(result, key + "=" + "******");
            } else {
                matcher.appendReplacement(result, matcher.group());
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private String desensitizeSensitiveValues(String line) {
        StringBuffer result = new StringBuffer();
        Matcher matcher = SENSITIVE_VALUE_PATTERN.matcher(line);
        while (matcher.find()) {
            String value = matcher.group();
            if (isSensitiveValue(value)) {
                matcher.appendReplacement(result, "******");
            } else {
                matcher.appendReplacement(result, value);
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private boolean isSensitiveKey(String key) {
        return SENSITIVE_KEY_PATTERN.matcher(key).find();
    }

    private boolean isSensitiveValue(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        if (Constants.TRUE.equalsIgnoreCase(value) || Constants.FALSE.equalsIgnoreCase(value)) {
            return false;
        }
        if (Constants.NULL.equalsIgnoreCase(value)) {
            return false;
        }
        if (Constants.YES.equalsIgnoreCase(value) || Constants.NO.equalsIgnoreCase(value)) {
            return false;
        }
        return SENSITIVE_VALUE_PATTERN.matcher(value).matches();
    }

    private String processEncFormat(String message) {
        return encodeCrlf(message);
    }

    private String encodeCrlf(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);
            switch (c) {
                case '\r':
                    result.append("\\r");
                    break;
                case '\n':
                    result.append("\\n");
                    break;
                case '\t':
                    result.append("\\t");
                    break;
                case '\"':
                    result.append("\\\"");
                    break;
                case '\\':
                    result.append("\\\\");
                    break;
                default:
                    result.append(c);
                    break;
            }
        }
        return result.toString();
    }

    /**
     * constants
     */
    interface Constants {
        /**
         * true
         */
        String TRUE = "true";

        /**
         * false
         */
        String FALSE = "false";

        /**
         * null
         */
        String NULL = "null";

        /**
         * yes
         */
        String YES = "yes";

        /**
         * no
         */
        String NO = "no";
    }
}