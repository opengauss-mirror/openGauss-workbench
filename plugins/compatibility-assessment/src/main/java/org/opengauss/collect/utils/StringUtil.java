/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
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
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.collect.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * StringUtil
 *
 * @author liu
 * @since 2022-10-01
 */
public class StringUtil {
    private static final Pattern PATTERN = Pattern.compile("pid:(\\d+)");

    /**
     * getPid
     *
     * @param pid pid
     * @return String
     */
    public static String getPid(String pid) {
        Matcher matcher = PATTERN.matcher(pid);
        AssertUtil.isTrue(!matcher.find(), "PID does not exist");
        return matcher.group(1);
    }

    /**
     * handleString
     *
     * @param str str
     * @return Map<String, String>
     */
    public static Map<String, String> handleString(String str) {
        Map<String, String> map = new HashMap<>();
        String[] strArray = str.split(",");
        for (String s : strArray) {
            String[] subArray = s.split("~");
            map.put(subArray[0], subArray[1]);
        }
        return map;
    }
}
