/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

/**
 * TextParserTest
 *
 * @since 2023/8/28 11:14
 */
@RunWith(SpringRunner.class)
public class TextParserTest {
    @Test
    public void testParse() {
        String text = "test ${title} and ${content}";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("title", "title1");
        paramMap.put("content", "content1");
        TextParser textParser = new TextParser("${", "}");
        textParser.parse(text, paramMap);
    }

    @Test
    public void testParseEmptyText() {
        String text = "";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("title", "title1");
        paramMap.put("content", "content1");
        TextParser textParser = new TextParser("${", "}");
        textParser.parse(text, paramMap);
    }
}
