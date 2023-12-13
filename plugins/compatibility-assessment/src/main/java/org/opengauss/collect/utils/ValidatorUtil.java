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

import java.util.regex.Pattern;

/**
 * ValidatorUtil
 *
 * @author liu
 * @since 2022-10-01
 */
public class ValidatorUtil {
    private static final String LINUX_PATH_REGEX = "^/([a-zA-Z0-9_]+/?)+$";
    private static final Pattern LINUX_PATH_PATTERN = Pattern.compile(LINUX_PATH_REGEX);

    /**
     * validateLinxuPath
     *
     * @param path path
     * @return boolean
     */
    public static boolean validateLinuxPath(String path) {
        return LINUX_PATH_PATTERN.matcher(path).matches();
    }

    /**
     * validatePath
     *
     * @param path path
     * @return boolean
     */
    public static boolean validatePath(String path) {
        boolean isValid = false;
        // 如果路径不为空且以 / 结尾并且符合规定文字内容
        if (path != null && path.endsWith("/") && path.matches("^/.+/.+/")) {
            isValid = true;
        }

        return isValid;
    }
}
