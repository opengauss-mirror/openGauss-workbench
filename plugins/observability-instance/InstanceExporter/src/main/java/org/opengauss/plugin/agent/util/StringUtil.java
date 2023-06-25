/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package org.opengauss.plugin.agent.util;

public class StringUtil {
    public static final String[] splitByBlank(String str) {
        return str.trim().split("\\s+");
    }

    /**
     * replace "()" and " " to "_" and remove last ":", example: "Active(anon):" ->
     * "Active_anon"
     */
    public static final String replaceParenthesis(String str) {
        return str.split(":")[0].replaceAll(" ", "_").replaceAll("\\((.*)\\)", "_$1").replaceAll("__", "_");
    }

    public static final String removeLast(String str) {
        return str.substring(0, str.length() - 1);
    }
}