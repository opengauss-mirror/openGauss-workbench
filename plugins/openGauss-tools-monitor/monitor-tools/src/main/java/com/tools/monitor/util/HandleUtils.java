/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */

package com.tools.monitor.util;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.util.CollectionUtils;

/**
 * HandleUtils
 *
 * @author liu
 * @since 2022-10-01
 */
public class HandleUtils {
    private static final String ISNUM = "^(\\-|\\+)?\\d+(\\.\\d+)?$";

    private static final String KEXUE = "^[+-]?\\d+\\.?\\d*[Ee][+-]?\\d+$";

    /**
     * handelTag
     *
     * @param tags tags
     * @return String
     */
    public static String[] handelTag(List<Tag> tags) {
        if (CollectionUtils.isEmpty(tags)) {
            return new String[0];
        }
        int size = tags.size() * 2 + 2;
        String[] tagArray = new String[size];
        int num = 0;
        for (Tag tag : tags) {
            tagArray[++num] = tag.getKey();
            tagArray[++num] = tag.getValue();
        }
        return tagArray;
    }

    /**
     * getMapToString
     *
     * @param map map
     * @return Tags
     */
    public static Tags getMapToString(Map<String, Object> map) {
        Map<String, Object> collect = map.entrySet().stream()
                .filter(item -> !item.getValue().toString().matches(ISNUM)
                        && !item.getValue().toString().matches(KEXUE))
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
        return map.entrySet().stream()
                .filter(item ->
                        ObjectUtil.isNotEmpty(item.getValue())
                                && !item.getValue().toString().matches(ISNUM)
                                && !item.getValue().toString().matches(KEXUE)
                                && !item.getKey().equals("time")
                                && !SqlUtil.contain(item.getKey()))
                .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
    }

    /**
     * getBigDecimal
     *
     * @param value value
     * @return BigDecimal
     */
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
                throw new ClassCastException("getBigDecimal fail");
            }
        }
        return ret.setScale(1, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * getZabbixMap
     *
     * @param map map
     * @param num num
     * @return String
     */
    public static String getZabbixMap(Map<String, Object> map, int num) {
        dealMetric(map, num);
        Set<String> keySet = map.keySet();
        String[] keyArray = keySet.toArray(new String[keySet.size()]);
        Arrays.sort(keyArray);
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i = 0; i < keyArray.length; i++) {
            if ((String.valueOf(map.get(keyArray[i]))).trim().length() > 0) {
                sb.append(keyArray[i]).append("=")
                        .append("\"").append(String.valueOf(map.get(keyArray[i])).trim())
                        .append("\"").append(",");
            }
        }
        sb.append("}");
        return sb.toString();
    }

    private static void dealMetric(Map<String, Object> metric, int num) {
        if (CollectionUtil.isEmpty(metric)) {
            metric.put("instance", "node" + num);
        }
    }
}

