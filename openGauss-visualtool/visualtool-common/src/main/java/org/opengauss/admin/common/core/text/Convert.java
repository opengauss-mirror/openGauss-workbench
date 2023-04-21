/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 * Convert.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/core/text/Convert.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.common.core.text;

import org.opengauss.admin.common.utils.StringUtils;

import java.math.BigDecimal;
import java.text.NumberFormat;

/**
 * type converter
 *
 * @author xielibo
 */
public class Convert {
    /**
     * convert to string<br>
     * If the given value is null, or the conversion fails, return the default value<br>
     *
     * @param value        value
     * @param defaultValue defaultValue
     * @return result
     */
    public static String toStr(Object value, String defaultValue) {
        if (null == value) {
            return defaultValue;
        }
        if (value instanceof String) {
            return (String) value;
        }
        return value.toString();
    }

    /**
     * convert to string<br>
     * If the given value is null, or the conversion fails, return null<br>
     *
     * @param value value
     * @return result
     */
    public static String toStr(Object value) {
        return toStr(value, null);
    }

    /**
     * convert to string<br>
     * If the given value is null, or the conversion fails, return the default value<br>
     *
     * @param value        value
     * @param defaultValue defaultValue
     * @return result
     */
    public static Character toChar(Object value, Character defaultValue) {
        if (null == value) {
            return defaultValue;
        }
        if (value instanceof Character) {
            return (Character) value;
        }

        final String valueStr = toStr(value, null);
        return StringUtils.isEmpty(valueStr) ? defaultValue : valueStr.charAt(0);
    }

    /**
     * convert to char<br>
     * If the given value is null, or the conversion fails, return the null<br>
     *
     * @param value value
     * @return result
     */
    public static Character toChar(Object value) {
        return toChar(value, null);
    }

    /**
     * convert to byte<br>
     * If the given value is null, or the conversion fails, return the default value<br>
     *
     * @param value        value
     * @param defaultValue defaultValue
     * @return result
     */
    public static Byte toByte(Object value, Byte defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Byte) {
            return (Byte) value;
        }
        if (value instanceof Number) {
            return ((Number) value).byteValue();
        }
        final String valueStr = toStr(value, null);
        if (StringUtils.isEmpty(valueStr)) {
            return defaultValue;
        }
        try {
            return Byte.parseByte(valueStr);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * convert to byte<br>
     * If the given value is null, or the conversion fails, return the default value<br>
     *
     */
    public static Byte toByte(Object value) {
        return toByte(value, null);
    }

    /**
     * convert to short<br>
     * If the given value is null, or the conversion fails, return the default value<br>
     *
     */
    public static Short toShort(Object value, Short defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Short) {
            return (Short) value;
        }
        if (value instanceof Number) {
            return ((Number) value).shortValue();
        }
        final String valueStr = toStr(value, null);
        if (StringUtils.isEmpty(valueStr)) {
            return defaultValue;
        }
        try {
            return Short.parseShort(valueStr.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * convert to byte<br>
     * If the given value is null, or the conversion fails, return null<br>
     *
     * @param value value
     */
    public static Short toShort(Object value) {
        return toShort(value, null);
    }

    /**
     * convert to number<br>
     * If the given value is null, or the conversion fails, return the default value<br>
     *
     * @param value        value
     * @param defaultValue defaultValue
     * @return result
     */
    public static Number toNumber(Object value, Number defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return (Number) value;
        }
        final String valueStr = toStr(value, null);
        if (StringUtils.isEmpty(valueStr)) {
            return defaultValue;
        }
        try {
            return NumberFormat.getInstance().parse(valueStr);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * convert to number<br>
     * If the given value is null, or the conversion fails, return null<br>
     *
     * @param value value
     * @return result
     */
    public static Number toNumber(Object value) {
        return toNumber(value, null);
    }

    /**
     * convert to int<br>
     * If the given value is null, or the conversion fails, return the default value<br>
     *
     * @param value        value
     * @param defaultValue defaultValue
     * @return result
     */
    public static Integer toInt(Object value, Integer defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        final String valueStr = toStr(value, null);
        if (StringUtils.isEmpty(valueStr)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(valueStr.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * convert to number<br>
     * If the given value is null, or the conversion fails, return null<br>
     *
     * @param value value
     * @return result
     */
    public static Integer toInt(Object value) {
        if (value != null) {
            return Integer.parseInt(value.toString());
        }
        return null;
    }



    /**
     * convert to long<br>
     * If the given value is null, or the conversion fails, return the default value<br>
     *
     * @param value        value
     * @param defaultValue defaultValue
     * @return result
     */
    public static Long toLong(Object value, Long defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        final String valueStr = toStr(value, null);
        if (StringUtils.isEmpty(valueStr)) {
            return defaultValue;
        }
        try {
            return new BigDecimal(valueStr.trim()).longValue();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * convert to long<br>
     * If the given value is null, or the conversion fails, return null<br>
     *
     * @param value value
     * @return result
     */
    public static Long toLong(Object value) {
        return toLong(value, null);
    }

    /**
     * convert to double<br>
     * If the given value is null, or the conversion fails, return the default value<br>
     *
     * @param value        value
     * @param defaultValue defaultValue
     * @return result
     */
    public static Double toDouble(Object value, Double defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Double) {
            return (Double) value;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        final String valueStr = toStr(value, null);
        if (StringUtils.isEmpty(valueStr)) {
            return defaultValue;
        }
        try {
            return new BigDecimal(valueStr.trim()).doubleValue();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * convert to number<br>
     * If the given value is null, or the conversion fails, return null<br>
     *
     * @param value value
     * @return result
     */
    public static Double toDouble(Object value) {
        return toDouble(value, null);
    }

    /**
     * convert to float<br>
     * If the given value is null, or the conversion fails, return the default value<br>
     *
     * @param value        value
     * @param defaultValue defaultValue
     * @return result
     */
    public static Float toFloat(Object value, Float defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Float) {
            return (Float) value;
        }
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        final String valueStr = toStr(value, null);
        if (StringUtils.isEmpty(valueStr)) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(valueStr.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * convert to float<br>
     * If the given value is null, or the conversion fails, return the default value<br>
     *
     * @param value value
     * @return result
     */
    public static Float toFloat(Object value) {
        return toFloat(value, null);
    }

    /**
     * convert to boolean<br>
     * The supported values of String are: true, false, yes, ok, no, 1,0 If the given value is empty or the conversion fails, the default value will be returned<br>
     *
     * @param value        value
     * @param defaultValue defaultValue
     * @return result
     */
    public static Boolean toBool(Object value, Boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        String valueStr = toStr(value, null);
        if (StringUtils.isEmpty(valueStr)) {
            return defaultValue;
        }
        valueStr = valueStr.trim().toLowerCase();
        switch (valueStr) {
            case "true":
                return true;
            case "false":
                return false;
            case "yes":
                return true;
            case "ok":
                return true;
            case "no":
                return false;
            case "1":
                return true;
            case "0":
                return false;
            default:
                return defaultValue;
        }
    }

    /**
     * convert to boolean<br>
     * The supported values of String are: true, false, yes, ok, no, 1,0 If the given value is empty or the conversion fails, the default value will be returned<br>
     *
     * @param value value
     * @return result
     */
    public static Boolean toBool(Object value) {
        return toBool(value, null);
    }

}
