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
 *  TextParserUtilsTest.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/test/java/com/nctigba/alert/monitor/util/TextParserUtilsTest.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.util;

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
public class TextParserUtilsTest {
    @Test
    public void testParse() {
        String text = "test ${title} and ${content}";
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("title", "title1");
        paramMap.put("content", "content1");
        TextParserUtils.parse(text, paramMap);
    }

    @Test
    public void testParseEmptyText() {
        String text = "";
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("title", "title1");
        paramMap.put("content", "content1");
        TextParserUtils.parse(text, paramMap, "${", "}");
    }
}
