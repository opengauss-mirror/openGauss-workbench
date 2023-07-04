/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import org.opengauss.admin.common.exception.ServiceException;

import java.util.Map;

/**
 * @author wuyuebin
 * @date 2023/5/2 22:25
 * @description
 */
public class TextParser {
    private String paramOpen = "${";
    private String paramClose = "}";

    public TextParser() {
    }

    public TextParser(String paramOpen, String paramClose) {
        this.paramOpen = paramOpen;
        this.paramClose = paramClose;
    }

    public String parse(String text, Map<String, Object> paramMap) {
        if (StrUtil.isBlank(text)) {
            return "";
        }
        int start = text.indexOf(this.paramOpen);
        if (start == -1) {
            return text;
        } else {
            char[] src = text.toCharArray();
            int offset = 0;
            StringBuilder builder = new StringBuilder();

            do {
                builder.append(src, offset, start - offset);
                offset = start + this.paramOpen.length();

                int end = text.indexOf(this.paramClose, offset);
                if (end == -1) {
                    throw new ServiceException("text parser error");
                }
                String paramName = text.substring(offset, end);
                Object paramVal = paramMap.getOrDefault(paramName, "");
                builder.append(paramVal);
                offset = end + this.paramClose.length();
                start = text.indexOf(this.paramOpen, offset);
            } while (start > -1);

            if (offset < src.length) {
                builder.append(src, offset, src.length - offset);
            }
            return builder.toString();
        }
    }

    public String parse(String text, Object obj) {
        Map<String, Object> paramMap = BeanUtil.beanToMap(obj);
        return parse(text, paramMap);
    }
}
