/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.util;

import com.nctigba.observability.sql.model.history.dto.WaitEventInfo;

import java.util.Comparator;

/**
 * ComparatorUtil
 *
 * @author luomeng
 * @since 2023/6/9
 */
public class ComparatorUtil implements Comparator<Object> {
    @Override
    public int compare(Object o1, Object o2) {
        Integer count1 = 0;
        if (o1 instanceof WaitEventInfo) {
            count1 = ((WaitEventInfo) o1).getEventCount();
        }
        Integer count2 = 0;
        if (o2 instanceof WaitEventInfo) {
            count2 = ((WaitEventInfo) o2).getEventCount();
        }
        int compareByCount = Integer.compare(count2, count1);
        if (compareByCount != 0) {
            return compareByCount;
        } else {
            String name1 = "";
            if (o1 instanceof WaitEventInfo) {
                name1 = ((WaitEventInfo) o1).getEventName();
            }
            String name2 = "";
            if (o2 instanceof WaitEventInfo) {
                name2 = ((WaitEventInfo) o2).getEventName();
            }
            return name1.compareTo(name2);
        }
    }
}
