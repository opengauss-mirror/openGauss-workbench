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
 *  ListUtils.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/main/java/com/nctigba/observability/instance/util/ListUtils.java
 *
 *  -------------------------------------------------------------------------
 */
package com.nctigba.observability.instance.util;

import java.util.ArrayList;
import java.util.List;

public class ListUtils {
    /**
     * parse prom data to list<br>
     * missing metric parse to -1
     * 
     * @param list     (prometheus metric list): {{1685323348,1},{1685323468,2}}
     * @param timeline (time seconds): {1685323288,1685323348,1685323408,1685323468}
     * @return {-1,1,-1,2}
     */
    public static List<Number> collect(List<List<Number>> list, List<Long> timeline) {
        List<Number> result = new ArrayList<>();
        Long before = -1L;
        var iter = list.iterator();
        var curr = iter.next();
        for (Long now : timeline) {
            var t = (curr != null ? curr.get(0).longValue() : 0);
            if (t > before && t <= now) {
                result.add(curr.get(1));
                curr = iter.hasNext() ? iter.next() : null;
            } else
                result.add(0);
            before = now;
        }
        return result;
    }
}