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
