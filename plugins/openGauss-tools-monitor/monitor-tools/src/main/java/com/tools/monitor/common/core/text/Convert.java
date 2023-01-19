package com.tools.monitor.common.core.text;

import com.tools.monitor.util.StringUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.util.Set;

/**
 * Convert
 *
 * @author liu
 * @since 2022-10-01
 */
public class Convert {
    /**
     * toStr
     *
     * @param value
     * @param defaultValue
     * @return
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
     * toStr
     *
     * @param value value
     * @return String
     */
    public static String toStr(Object value) {
        return toStr(value, null);
    }

    /**
     * toChar
     *
     * @param value
     * @param defaultValue
     * @return
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
     * toChar
     *
     * @param value
     * @return
     */
    public static Character toChar(Object value) {
        return toChar(value, null);
    }

    /**
     * toByte
     *
     * @param value
     * @param defaultValue
     * @return
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
     * toByte
     *
     * @param value
     * @return
     */
    public static Byte toByte(Object value) {
        return toByte(value, null);
    }

    /**
     * toShort
     *
     * @param value
     * @param defaultValue
     * @return
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
     * toShort
     *
     * @param value
     * @return
     */
    public static Short toShort(Object value) {
        return toShort(value, null);
    }

    /**
     * toNumber
     *
     * @param value
     * @param defaultValue
     * @return
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
     * toNumber
     *
     * @param value
     * @return
     */
    public static Number toNumber(Object value) {
        return toNumber(value, null);
    }

    /**
     * toInt
     *
     * @param value
     * @param defaultValue
     * @return
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
     * toInt
     *
     * @param value
     * @return
     */
    public static Integer toInt(Object value) {
        return toInt(value, null);
    }

    /**
     * toIntArray
     *
     * @param str
     * @return
     */
    public static Integer[] toIntArray(String str) {
        return toIntArray(",", str);
    }

    /**
     * toLongArray
     *
     * @param str
     * @return
     */
    public static Long[] toLongArray(String str) {
        return toLongArray(",", str);
    }

    /**
     * toIntArray
     *
     * @param split
     * @param str
     * @return
     */
    public static Integer[] toIntArray(String split, String str) {
        if (StringUtils.isEmpty(str)) {
            return new Integer[]{};
        }
        String[] arr = str.split(split);
        final Integer[] ints = new Integer[arr.length];
        for (int i = 0; i < arr.length; i++) {
            final Integer v = toInt(arr[i], 0);
            ints[i] = v;
        }
        return ints;
    }

    /**
     * toLongArray
     *
     * @param split
     * @param str
     * @return
     */
    public static Long[] toLongArray(String split, String str) {
        if (StringUtils.isEmpty(str)) {
            return new Long[]{};
        }
        String[] arr = str.split(split);
        final Long[] longs = new Long[arr.length];
        for (int i = 0; i < arr.length; i++) {
            final Long v = toLong(arr[i], null);
            longs[i] = v;
        }
        return longs;
    }

    /**
     * toStrArray
     *
     * @param str
     * @return
     */
    public static String[] toStrArray(String str) {
        return toStrArray(",", str);
    }

    /**
     * toStrArray
     *
     * @param split
     * @param str
     * @return
     */
    public static String[] toStrArray(String split, String str) {
        return str.split(split);
    }

    /**
     * toLong
     *
     * @param value
     * @param defaultValue
     * @return
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
     * toLong
     *
     * @param value
     * @return
     */
    public static Long toLong(Object value) {
        return toLong(value, null);
    }

    /**
     * toDouble
     *
     * @param value
     * @param defaultValue
     * @return
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
     * toDouble
     *
     * @param value
     * @return
     */
    public static Double toDouble(Object value) {
        return toDouble(value, null);
    }

    /**
     * toFloat
     *
     * @param value
     * @param defaultValue
     * @return
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
     * toFloat
     *
     * @param value
     * @return
     */
    public static Float toFloat(Object value) {
        return toFloat(value, null);
    }

    /**
     * toBool
     *
     * @param value
     * @param defaultValue
     * @return
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
            case "yes":
            case "ok":
            case "1":
                return true;
            case "false":
            case "no":
            case "0":
                return false;
            default:
                return defaultValue;
        }
    }

    /**
     * toBool
     *
     * @param value
     * @return
     */
    public static Boolean toBool(Object value) {
        return toBool(value, null);
    }

    /**
     * toEnum
     *
     * @param clazz
     * @param value
     * @param defaultValue
     * @return
     * @param <E>
     */
    public static <E extends Enum<E>> E toEnum(Class<E> clazz, Object value, E defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (clazz.isAssignableFrom(value.getClass())) {
            @SuppressWarnings("unchecked")
            E myE = (E) value;
            return myE;
        }
        final String valueStr = toStr(value, null);
        if (StringUtils.isEmpty(valueStr)) {
            return defaultValue;
        }
        try {
            return Enum.valueOf(clazz, valueStr);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * toEnum
     *
     * @param clazz
     * @param value
     * @return
     * @param <E>
     */
    public static <E extends Enum<E>> E toEnum(Class<E> clazz, Object value) {
        return toEnum(clazz, value, null);
    }

    /**
     * toInteger
     *
     * @param value
     * @param defaultValue
     * @return
     */
    public static Integer toInteger(Object value, Integer defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Long) {
            return Integer.valueOf(Math.toIntExact((Long) value));
        }
        final String valueStr = toStr(value, null);
        if (StringUtils.isEmpty(valueStr)) {
            return defaultValue;
        }
        try {
            return new Integer(valueStr);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * toInteger
     *
     * @param value
     * @return
     */
    public static Integer toInteger(Object value) {
        return toInteger(value, null);
    }

    /**
     * toBigDecimal
     *
     * @param value
     * @param defaultValue
     * @return
     */
    public static BigDecimal toBigDecimal(Object value, BigDecimal defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Long) {
            return new BigDecimal((Long) value);
        }
        if (value instanceof Double) {
            return new BigDecimal((Double) value);
        }
        if (value instanceof Integer) {
            return new BigDecimal((Integer) value);
        }
        final String valueStr = toStr(value, null);
        if (StringUtils.isEmpty(valueStr)) {
            return defaultValue;
        }
        try {
            return new BigDecimal(valueStr);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * toBigDecimal
     *
     * @param value
     * @return
     */
    public static BigDecimal toBigDecimal(Object value) {
        return toBigDecimal(value, null);
    }

    /**
     * utf8Str
     *
     * @param obj
     * @return
     */
    public static String utf8Str(Object obj) {
        return str(obj, CharsetKit.CHARSET_UTF_8);
    }

    /**
     * str
     * @param obj
     * @param charsetName
     * @return
     */
    public static String str(Object obj, String charsetName) {
        return str(obj, Charset.forName(charsetName));
    }

    /**
     * str
     *
     * @param obj
     * @param charset
     * @return
     */
    public static String str(Object obj, Charset charset) {
        if (null == obj) {
            return null;
        }
        if (obj instanceof String) {
            return (String) obj;
        } else if (obj instanceof byte[]) {
            return str((byte[]) obj, charset);
        } else if (obj instanceof Byte[]) {
            byte[] bytes = ArrayUtils.toPrimitive((Byte[]) obj);
            return str(bytes, charset);
        } else if (obj instanceof ByteBuffer) {
            return str((ByteBuffer) obj, charset);
        }
        return obj.toString();
    }

    /**
     * str
     *
     * @param bytes
     * @param charset
     * @return
     */
    public static String str(byte[] bytes, String charset) {
        return str(bytes, StringUtils.isEmpty(charset) ? Charset.defaultCharset() : Charset.forName(charset));
    }

    /**
     * str
     *
     * @param data
     * @param charset
     * @return
     */
    public static String str(byte[] data, Charset charset) {
        if (data == null) {
            return null;
        }

        if (null == charset) {
            return new String(data);
        }
        return new String(data, charset);
    }

    /**
     * str
     *
     * @param data
     * @param charset
     * @return
     */
    public static String str(ByteBuffer data, String charset) {
        if (data == null) {
            return null;
        }

        return str(data, Charset.forName(charset));
    }

    /**
     * str
     *
     * @param data
     * @param charset
     * @return
     */
    public static String str(ByteBuffer data, Charset charset) {
        if (null == charset) {
            charset = Charset.defaultCharset();
        }
        return charset.decode(data).toString();
    }

    /**
     * toSBC
     *
     * @param input
     * @return
     */
    public static String toSBC(String input) {
        return toSBC(input, null);
    }

    /**
     * bc
     *
     * @param input
     * @param notConvertSet
     * @return
     */
    public static String toSBC(String input, Set<Character> notConvertSet) {
        char c[] = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (null != notConvertSet && notConvertSet.contains(c[i])) {
                continue;
            }

            if (c[i] == ' ') {
                c[i] = '\u3000';
            } else if (c[i] < '\177') {
                c[i] = (char) (c[i] + 65248);

            }
        }
        return new String(c);
    }

    /**
     * toDBC
     *
     * @param input
     * @return
     */
    public static String toDBC(String input) {
        return toDBC(input, null);
    }

    /**
     * toDBC
     *
     * @param text
     * @param notConvertSet
     * @return
     */
    public static String toDBC(String text, Set<Character> notConvertSet) {
        char c[] = text.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (null != notConvertSet && notConvertSet.contains(c[i])) {
                continue;
            }

            if (c[i] == '\u3000') {
                c[i] = ' ';
            } else if (c[i] > '\uFF00' && c[i] < '\uFF5F') {
                c[i] = (char) (c[i] - 65248);
            }
        }
        String returnString = new String(c);

        return returnString;
    }
}
