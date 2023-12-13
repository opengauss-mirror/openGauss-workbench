/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  TextParserUtils.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/util/TextParserUtils.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.util;

import cn.hutool.core.util.StrUtil;
import org.opengauss.admin.common.exception.ServiceException;

import java.util.Map;

/**
 * @author wuyuebin
 * @date 2023/5/2 22:25
 * @description
 */
public class TextParserUtils {
    private static final String PARAM_OPEN = "${";
    private static final String PARAM_CLOSE = "}";


    /**
     * Parsing text based on parameters
     *
     * @param text   such as "this is a ${car}"
     * @param paramMap such as {car: "Ragdoll"}
     * @return  "this is a Ragdoll"
     */
    public static String parse(String text, Map<String, String> paramMap) {
        return parse(text, paramMap, PARAM_OPEN, PARAM_CLOSE);
    }

    /**
     * Parsing text based on parameters
     *
     * @param text String
     * @param paramMap Map
     * @param paramOpen such as "${"
     * @param paramClose such as "}"
     * @return String
     */
    public static String parse(String text, Map<String, String> paramMap, String paramOpen, String paramClose) {
        if (StrUtil.isBlank(text)) {
            return "";
        }
        int start = text.indexOf(paramOpen);
        if (start == -1) {
            return text;
        } else {
            char[] src = text.toCharArray();
            int offset = 0;
            StringBuilder builder = new StringBuilder();

            do {
                builder.append(src, offset, start - offset);
                offset = start + paramOpen.length();

                int end = text.indexOf(paramClose, offset);
                if (end == -1) {
                    throw new ServiceException("text parser error");
                }
                String paramName = text.substring(offset, end);
                Object paramVal = paramMap.getOrDefault(paramName, "");
                builder.append(paramVal);
                offset = end + paramClose.length();
                start = text.indexOf(paramOpen, offset);
            } while (start > -1);

            if (offset < src.length) {
                builder.append(src, offset, src.length - offset);
            }
            return builder.toString();
        }
    }
}