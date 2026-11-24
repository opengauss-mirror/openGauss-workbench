/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
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
