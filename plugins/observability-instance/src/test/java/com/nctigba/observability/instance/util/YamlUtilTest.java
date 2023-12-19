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
 *  YamlUtilTest.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/test/java/com/nctigba/observability/instance/util/YamlUtilTest.java
 *
 *  -------------------------------------------------------------------------
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
        var str = YamlUtils.dump(map);
        YamlUtils.loadAs(str, Map.class);
        YamlUtils.dump(null);
    }
}