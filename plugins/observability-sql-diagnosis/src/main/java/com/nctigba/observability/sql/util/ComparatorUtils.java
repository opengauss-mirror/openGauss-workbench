/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package com.nctigba.observability.sql.util;

import com.nctigba.observability.sql.model.vo.point.WaitEventInfoVO;

import java.util.Comparator;

/**
 * ComparatorUtil
 *
 * @author luomeng
 * @since 2023/6/9
 */
public class ComparatorUtils implements Comparator<WaitEventInfoVO> {
    @Override
    public int compare(WaitEventInfoVO o1, WaitEventInfoVO o2) {
        int compareByCount = Integer.compare(o1.getEventCount(), o2.getEventCount());
        if (compareByCount != 0) {
            return compareByCount;
        }
        return o1.getEventName().compareTo(o2.getEventName());
    }
}
