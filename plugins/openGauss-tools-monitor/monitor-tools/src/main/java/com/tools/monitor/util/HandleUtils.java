package com.tools.monitor.util;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import io.micrometer.core.instrument.Tags;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * HandleUtils
 *
 * @author liu
 * @since 2022-10-01
 */
public class HandleUtils {
    private static final String ISNUM = "^(\\-|\\+)?\\d+(\\.\\d+)?$";

    private static final String KEXUE = "^[+-]?\\d+\\.?\\d*[Ee][+-]?\\d+$";

    private static final String time = "^[1-9]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])$";

    /**
     * getMapToString01
     *
     * @param map map
     * @return Tags
     */
    public static Tags getMapToString01(Map<String, Object> map) {
        Set<String> keySet = map.keySet();
        String[] keyArray = keySet.toArray(new String[keySet.size()]);
        Arrays.sort(keyArray);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keyArray.length; i++) {
            if ((String.valueOf(map.get(keyArray[i]))).trim().length() > 0) {
                sb.append(keyArray[i]).append("&").append(String.valueOf(map.get(keyArray[i])).trim());
            }
            if (i != keyArray.length - 1) {
                sb.append("&");
            }
        }
        String[] array = sb.toString().split("&");
        return Tags.of(array);
    }

    /**
     * getMapToString
     *
     * @param map map
     * @return Tags
     */
    public static Tags getMapToString(Map<String, Object> map) {
        Map<String, Object> collect = map.entrySet().stream()
                .filter(item -> !item.getValue().toString().matches(ISNUM) && !item.getValue().toString().matches(KEXUE))
                .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
        List<String> list = new ArrayList<>();
        for (Map.Entry<String, Object> entry : collect.entrySet()) {
            list.add(entry.getKey());
            list.add(entry.getValue().toString());
        }
        String[] arry = list.stream().toArray(String[]::new);
        return Tags.of(arry);
    }

    /**
     * getMap
     *
     * @param map map
     * @return map
     */
    public static Map<String, Object> getMap(Map<String, Object> map) {
        Map<String, Object> collect = map.entrySet().stream()
                .filter(item -> ObjectUtil.isNotEmpty(item.getValue()) && !item.getValue().toString().matches(ISNUM) && !item.getValue().toString().matches(KEXUE) && !item.getKey().equals("time") && !SqlUtil.contain(item.getKey()))
                .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
        return collect;
    }


    public static BigDecimal getBigDecimal(Object value) {
        BigDecimal ret = null;
        if (value != null) {
            if (value instanceof BigDecimal) {
                ret = (BigDecimal) value;
            } else if (value instanceof String) {
                ret = new BigDecimal((String) value);
            } else if (value instanceof Integer) {
                ret = new BigDecimal((Integer) value);
            } else if (value instanceof Number) {
                ret = new BigDecimal(((Number) value).doubleValue());
            } else {
                throw new ClassCastException("Not possible to coerce [" + value + "] from class " + value.getClass() + " into a BigDecimal.");
            }
        }
        return ret.setScale(1, BigDecimal.ROUND_HALF_UP);
    }

    public static String getZabbixMap(Map<String, Object> map, int num) {
        dealMetric(map, num);
        Set<String> keySet = map.keySet();
        String[] keyArray = keySet.toArray(new String[keySet.size()]);
        Arrays.sort(keyArray);
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i = 0; i < keyArray.length; i++) {
            if ((String.valueOf(map.get(keyArray[i]))).trim().length() > 0) {
                sb.append(keyArray[i]).append("=").append("\"").append(String.valueOf(map.get(keyArray[i])).trim()).append("\"").append(",");
            }
        }
        sb.append("}");
        return sb.toString();
    }

    private static void dealMetric(Map<String, Object> metric, int i) {
        if (CollectionUtil.isEmpty(metric)) {
            metric.put("instance", "node" + i);
        }
    }
}

