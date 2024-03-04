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
 *  CommonUtils.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/main/java/com/nctigba/observability/instance/util/CommonUtils.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.util;

import cn.hutool.core.util.StrUtil;
import org.opengauss.admin.common.exception.ServiceException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * CommonUtils
 *
 * @since 2024/2/6 15:18
 */
public class CommonUtils {
    /**
     * parseText
     *
     * @param source String
     * @param paramMap Map<String, Object>
     * @return String
     */
    public static String parseText(String source, Map<String, Object> paramMap) {
        return parseText(source, paramMap, "${", "}");
    }

    /**
     * parseText
     *
     * @param source String
     * @param paramMap Map<String, Object>
     * @param paramOpen String
     * @param paramClose String
     * @return String
     */
    public static String parseText(String source, Map<String, Object> paramMap, String paramOpen, String paramClose) {
        if (StrUtil.isBlank(source)) {
            return "";
        }
        int start = source.indexOf(paramOpen);
        if (start == -1) {
            return source;
        } else {
            char[] src = source.toCharArray();
            int offset = 0;
            StringBuilder builder = new StringBuilder();

            do {
                builder.append(src, offset, start - offset);
                offset = start + paramOpen.length();

                int end = source.indexOf(paramClose, offset);
                if (end == -1) {
                    throw new ServiceException("text parser error");
                }
                String paramName = source.substring(offset, end);
                Object paramVal = paramMap.getOrDefault(paramName, "");
                builder.append(paramVal);
                offset = end + paramClose.length();
                start = source.indexOf(paramOpen, offset);
            } while (start > -1);

            if (offset < src.length) {
                builder.append(src, offset, src.length - offset);
            }
            return builder.toString();
        }
    }

    /**
     * processCommand
     *
     * @param commands command array
     * @return String
     * @throws IOException IOException
     * @throws InterruptedException InterruptedException
     */
    public static String processCommand(String... commands) throws IOException, InterruptedException {
        return processCommand(null, commands);
    }

    /**
     * processCommand
     *
     * @param dir ProcessBuilder directory
     * @param commands command array
     * @return String
     * @throws IOException IOException
     * @throws InterruptedException InterruptedException
     */
    public static String processCommand(File dir, String... commands) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(commands);
        if (dir != null) {
            builder.directory(dir);
        }
        Process process = builder.start();
        process.waitFor(2L, TimeUnit.SECONDS);
        StringBuilder strBuilder = new StringBuilder();
        try (BufferedReader bufferedReader =
                new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.defaultCharset()))) {
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                strBuilder.append(line);
            }
        } finally {
            if (process != null && process.isAlive()) {
                process.destroy();
            }
        }
        return strBuilder.toString();
    }
}
