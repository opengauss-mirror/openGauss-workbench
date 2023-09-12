/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.util;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * YamlUtilTest.java
 *
 * @since 2023年7月17日
 */
class YamlUtilTest {
    @Test
    void test() {
        Map<String, String> map = new HashMap<>();
        map.put("a", null);
        map.put("b", "c");
        map.put(null, null);
        var str = YamlUtil.dump(map);
        YamlUtil.loadAs(str, Map.class);
        YamlUtil.dump(null);
    }
}