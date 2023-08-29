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
 * TextUtil.java
 *
 * IDENTIFICATION
 * datasync-mysql/src/main/java/org/opengauss/admin/plugin/util/TextUtil.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.plugin.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

/**
 * @className: TextUtil
 * @description: TextUtil
 * @author: xielibo
 * @date: 2022-10-28 17:31
 **/
public class TextUtil {

    public static void replaceTemplateWriteFile(Map<String, Object> params, String writeFilePath) {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("template/shell.sh");
        String content = IoUtil.read(inputStream, "UTF-8");
        ExpressionParser parser = new SpelExpressionParser();
        TemplateParserContext parserContext = new TemplateParserContext();
        String replaceContent = parser.parseExpression(content,parserContext).getValue(params, String.class);
        FileUtil.writeString(replaceContent, new File(writeFilePath), "UTF-8");
    }
    public static void writeFile(String writeFilePath) {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("template/shell-close.sh");
        String content = IoUtil.read(inputStream, "UTF-8");
        FileUtil.writeString(content, new File(writeFilePath), "UTF-8");
    }
}
