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
public class ComparatorUtil implements Comparator<WaitEventInfo> {
    @Override
    public int compare(WaitEventInfo o1, WaitEventInfo o2) {
        int compareByCount = Integer.compare(o1.getEventCount(), o2.getEventCount());
        if (compareByCount != 0) {
            return compareByCount;
        }
        return o1.getEventName().compareTo(o2.getEventName());
    }
}
