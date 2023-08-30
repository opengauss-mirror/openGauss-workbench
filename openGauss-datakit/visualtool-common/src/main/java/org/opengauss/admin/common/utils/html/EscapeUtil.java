/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
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
 * EscapeUtil.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/utils/html/EscapeUtil.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.common.utils.html;

import org.opengauss.admin.common.utils.StringUtils;

/**
 * EscapeUtil
 *
 * @author xielibo
 */
public class EscapeUtil {
    public static final String RE_HTML_MARK = "(<[^<]*?>)|(<[\\s]*?/[^<]*?>)|(<[^<]*?/[\\s]*?>)";

    private static final char[][] TEXT = new char[64][];

    static {
        for (int i = 0; i < 64; i++) {
            TEXT[i] = new char[]{(char) i};
        }

        // special HTML characters
        TEXT['\''] = "&#039;".toCharArray();
        TEXT['"'] = "&#34;".toCharArray();
        TEXT['&'] = "&#38;".toCharArray();
        TEXT['<'] = "&#60;".toCharArray();
        TEXT['>'] = "&#62;".toCharArray();
    }

    /**
     * Escape HTML characters in text to safe characters
     *
     */
    public static String escape(String text) {
        return encode(text);
    }

    /**
     * Restore escaped HTML special characters
     *
     */
    public static String unescape(String content) {
        return decode(content);
    }

    /**
     * Clear all HTML tags, but do not delete the content inside the tags
     *
     */
    public static String clean(String content) {
        return new HTMLFilter().filter(content);
    }

    /**
     * Escape Encode
     *
     */
    private static String encode(String text) {
        int len;
        if ((text == null) || ((len = text.length()) == 0)) {
            return StringUtils.EMPTY;
        }
        StringBuilder buffer = new StringBuilder(len + (len >> 2));
        char c;
        for (int i = 0; i < len; i++) {
            c = text.charAt(i);
            if (c < 64) {
                buffer.append(TEXT[c]);
            } else {
                buffer.append(c);
            }
        }
        return buffer.toString();
    }

    /**
     * Escape Decode
     *
     */
    public static String decode(String content) {
        if (StringUtils.isEmpty(content)) {
            return content;
        }

        StringBuilder tmp = new StringBuilder(content.length());
        int lastPos = 0, pos = 0;
        char ch;
        while (lastPos < content.length()) {
            pos = content.indexOf("%", lastPos);
            if (pos == lastPos) {
                if (content.charAt(pos + 1) == 'u') {
                    ch = (char) Integer.parseInt(content.substring(pos + 2, pos + 6), 16);
                    tmp.append(ch);
                    lastPos = pos + 6;
                } else {
                    ch = (char) Integer.parseInt(content.substring(pos + 1, pos + 3), 16);
                    tmp.append(ch);
                    lastPos = pos + 3;
                }
            } else {
                if (pos == -1) {
                    tmp.append(content.substring(lastPos));
                    lastPos = content.length();
                } else {
                    tmp.append(content.substring(lastPos, pos));
                    lastPos = pos;
                }
            }
        }
        return tmp.toString();
    }
}
