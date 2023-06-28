/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package com.nctigba.observability.instance.util;

import java.util.ArrayList;
import java.util.List;

public class ListUtil {
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