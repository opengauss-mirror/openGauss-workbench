/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */

package com.tools.monitor.util;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

/**
 * ZabbixMap
 *
 * @author liu
 * @since 2022-10-01
 */
public class ZabbixMap {
    /**
     * getMapToString
     *
     * @param map map
     * @return String
     */
    public static String getMapToString(Map<String, Object> map) {
        Set<String> keySet = map.keySet();
        String[] keyArray = keySet.toArray(new String[keySet.size()]);
        Arrays.sort(keyArray);
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i = 0; i < keyArray.length; i++) {
            if ((String.valueOf(map.get(keyArray[i]))).trim().length() > 0) {
                sb.append(keyArray[i]).append("=")
                        .append("\"")
                        .append(String.valueOf(map.get(keyArray[i])).trim()).append("\"")
                        .append(",");
            }
        }
        sb.append("}");
        return sb.toString();
    }
}
