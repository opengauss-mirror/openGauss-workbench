/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
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
 */

package org.opengauss.admin.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Description: DataKit base tools common class
 * reference : org.opengauss.admin.plugin.service.impl.MigrationTaskHostRefServiceImpl
 * #checkJavaEnv(org.opengauss.admin.system.plugin.beans.SshLogin)
 *
 * @author: wangchao
 * @Date: 2025/6/17 17:31
 * @since 7.0.0-RC2
 */
@Slf4j
public class CommonUtils {
    private static final Pattern JDK_VERSION_PATTERN = Pattern.compile(
        "(?:openjdk|java) version \"(?:1\\.)?([8-9]|1\\d|\\d{2,})(?:[\\.-]|$|\\\")");
    private static final Pattern JDK_VERSION_SEC_PATTERN = Pattern.compile(
        "(?:1\\.)?([8-9]|1\\d|\\d{2,})(?:[\\.-]|$|\\\")");

    /**
     * java version info by command "java -version"
     * some example {@link org.opengauss.admin.system.service.agent.impl.JavaVersionParseTest}
     *
     * @param version version info by command "java -version"
     * @return java version major
     */
    public static int getJavaVersionMajor(String version) {
        if (version == null || version.isEmpty()) {
            return -1;
        }
        Matcher matcher = JDK_VERSION_PATTERN.matcher(version);
        if (matcher.find()) {
            String majorVersionStr = matcher.group(1);
            try {
                return Integer.parseInt(majorVersionStr);
            } catch (NumberFormatException e) {
                log.error("Failed to parse Java version: {}", majorVersionStr, e);
            }
        }
        // 备选匹配
        matcher = JDK_VERSION_SEC_PATTERN.matcher(version);
        if (matcher.find()) {
            String majorVersionStr = matcher.group(1);
            try {
                return Integer.parseInt(majorVersionStr);
            } catch (NumberFormatException e) {
                log.error("Failed to parse Java version: {}", majorVersionStr, e);
            }
        }
        log.error("Unrecognized Java version format: {}", version);
        return -1;
    }
}
