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
 *  StringUtils.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/InstanceExporter/
 *  src/main/java/com/nctigba/observability/instance/agent/util/StringUtils.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.agent.util;

/**
 * String util
 *
 * @since 2023/12/1
 */
public class StringUtils {
    /**
     * Split string by black
     *
     * @param str Original string
     * @return Split result array
     * @since 2023/12/1
     */
    public static final String[] splitByBlank(String str) {
        return str.trim().split("\\s+");
    }

    /**
     * replace "()" and " " to "_" and remove last ":", example: "Active(anon):" ->
     * "Active_anon"
     *
     * @since 2023/12/1
     */
    public static final String replaceParenthesis(String str) {
        return str.split(":")[0].replaceAll(" ", "_").replaceAll("\\((.*)\\)", "_$1").replaceAll("__", "_");
    }

    /**
     * remove last on char
     *
     * @param str Original string
     * @return Remove result
     * @since 2023/12/1
     */
    public static final String removeLast(String str) {
        return str.substring(0, str.length() - 1);
    }
}