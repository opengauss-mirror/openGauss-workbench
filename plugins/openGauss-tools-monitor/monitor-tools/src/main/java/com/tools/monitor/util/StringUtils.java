/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */

package com.tools.monitor.util;

import com.tools.monitor.common.contant.ConmmonShare;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

/**
 * StringUtils
 *
 * @author liu
 * @since 2022-10-01
 */
@Slf4j
public class StringUtils extends org.apache.commons.lang3.StringUtils {
    private static final String NULLSTR = "";

    private static final char SEPARATOR = '_';

    /**
     * nvl
     *
     * @param value        value
     * @param defaultValue defaultValue
     * @param <T>          t
     * @return t
     */
    public static <T> T nvl(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }

    /**
     * isEmpty
     *
     * @param coll coll
     * @return boolean
     */
    public static boolean isNothing(Collection<?> coll) {
        return isNull(coll) || coll.isEmpty();
    }

    /**
     * isNotEmpty
     *
     * @param collection collection
     * @return boolean
     */
    public static boolean isHaveSomething(Collection<?> collection) {
        return !isNothing(collection);
    }

    /**
     * isEmpty
     *
     * @param objects objects
     * @return boolean
     */
    public static boolean isNothing(Object[] objects) {
        return isNull(objects) || (objects.length == 0);
    }

    /**
     * isNotEmpty
     *
     * @param objects objects
     * @return boolean
     */
    public static boolean isHaveSomething(Object[] objects) {
        return !isNothing(objects);
    }

    /**
     * isEmpty
     *
     * @param monitorMap monitorMap
     * @return boolean
     */
    public static boolean isEmpty(Map<?, ?> monitorMap) {
        return isNull(monitorMap) || monitorMap.isEmpty();
    }

    /**
     * isNotEmpty
     *
     * @param monitorMap map
     * @return boolean
     */
    public static boolean isNotEmpty(Map<?, ?> monitorMap) {
        return !isEmpty(monitorMap);
    }

    /**
     * isEmpty
     *
     * @param str str
     * @return boolean
     */
    public static boolean isEmpty(String str) {
        return isNull(str) || NULLSTR.equals(str.trim());
    }

    /**
     * isNotEmpty
     *
     * @param str str
     * @return boolean
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * isNull
     *
     * @param object object
     * @return boolean
     */
    public static boolean isNull(Object object) {
        return object == null;
    }

    /**
     * isNotNull
     *
     * @param object object
     * @return boolean
     */
    public static boolean isNotNull(Object object) {
        return !isNull(object);
    }

    /**
     * isArray
     *
     * @param object object
     * @return boolean
     */
    public static boolean isArray(Object object) {
        return isNotNull(object) && object.getClass().isArray();
    }

    /**
     * trim
     *
     * @param str str
     * @return String
     */
    public static String trim(String str) {
        return (str == null ? "" : str.trim());
    }

    /**
     * substring
     *
     * @param st   st
     * @param star star
     * @return String
     */
    public static String substring(final String st, int star) {
        String str = st;
        int start = star;
        if (str == null) {
            return NULLSTR;
        }
        if (start < 0) {
            start = str.length() + start;
        }
        if (start < 0) {
            start = 0;
        }
        if (start > str.length()) {
            return NULLSTR;
        }
        return str.substring(start);
    }

    /**
     * substring
     *
     * @param st   st
     * @param star star
     * @param en  en
     * @return String
     */
    public static String substring(final String st, int star, int en) {
        String str = st;
        int start = star;
        int end = en;
        if (str == null) {
            return NULLSTR;
        }
        if (end < 0) {
            end = str.length() + end;
        }
        if (start < 0) {
            start = str.length() + start;
        }
        if (end > str.length()) {
            end = str.length();
        }
        if (start > end) {
            return NULLSTR;
        }
        if (start < 0) {
            start = 0;
        }
        if (end < 0) {
            end = 0;
        }
        return str.substring(start, end);
    }

    /**
     * ishttp
     *
     * @param link link
     * @return boolean
     */
    public static boolean ishttp(String link) {
        return StringUtils.startsWithAny(link, ConmmonShare.HTTP, ConmmonShare.HTTPS);
    }

    /**
     * str2Set
     *
     * @param str str
     * @param sep sep
     * @return set
     */
    public static final Set<String> str2Set(String str, String sep) {
        return new HashSet<String>(str2List(str, sep, true, false));
    }

    /**
     * str2List
     *
     * @param str         str
     * @param sep         sep
     * @param isFilterBlank isFilterBlank
     * @param isTrim        isTrim
     * @return list
     */
    public static final List<String> str2List(String str, String sep, boolean isFilterBlank, boolean isTrim) {
        List<String> list = new ArrayList<String>();
        if (StringUtils.isEmpty(str)) {
            return list;
        }
        if (isFilterBlank && StringUtils.isBlank(str)) {
            return list;
        }
        String[] split = str.split(sep);
        for (String string : split) {
            if (isFilterBlank && StringUtils.isBlank(string)) {
                continue;
            }
            if (isTrim) {
                string = string.trim();
            }
            list.add(string);
        }
        return list;
    }

    /**
     * IncludeAnyCases
     *
     * @param charSequence                  charSequence
     * @param searchCharSequences searchCharSequences
     * @return boolean
     */
    public static boolean includeAnyCases(CharSequence charSequence, CharSequence... searchCharSequences) {
        if (isEmpty(charSequence) || isNothing(searchCharSequences)) {
            return false;
        }
        for (CharSequence sequence : searchCharSequences) {
            if (containsIgnoreCase(charSequence, sequence)) {
                return true;
            }
        }
        return false;
    }

    /**
     * inStringIgnoreCase
     *
     * @param str  str
     * @param strs strs
     * @return boolean
     */
    public static boolean inStringIgnoreCase(String str, String... strs) {
        if (str != null && strs != null) {
            for (String all : strs) {
                if (str.equalsIgnoreCase(trim(all))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * convertToCamelCase
     *
     * @param name name
     * @return String
     */
    public static String convertToCamelCase(String name) {
        StringBuilder result = new StringBuilder();
        if (name == null || name.isEmpty()) {
            return "";
        } else if (!name.contains("_")) {
            return name.substring(0, 1).toUpperCase(Locale.ROOT) + name.substring(1);
        } else {
            log.error("convertToCamelCase");
        }
        String[] camels = name.split("_");
        for (String camel : camels) {
            if (camel.isEmpty()) {
                continue;
            }
            result.append(camel.substring(0, 1).toUpperCase(Locale.ROOT));
            result.append(camel.substring(1).toLowerCase(Locale.ROOT));
        }
        return result.toString();
    }

    /**
     * toCamelCase
     *
     * @param st st
     * @return String
     */
    public static String toCamelCase(String st) {
        String str = st;
        if (str == null) {
            return "";
        }
        str = str.toLowerCase(Locale.ROOT);
        StringBuilder sb = new StringBuilder(str.length());
        boolean isUpperCase = false;
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (ch == SEPARATOR) {
                isUpperCase = true;
            } else if (isUpperCase) {
                sb.append(Character.toUpperCase(ch));
                isUpperCase = false;
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }
}