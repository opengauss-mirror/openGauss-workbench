/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 * HostRealtimeStatistics.java
 *
 * IDENTIFICATION
 * org.opengauss.admin.common.core.vo.HostRealtimeStatistics
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.common.core.vo;

import lombok.Data;

import java.util.Locale;

/**
 * HostRealtimeStatistics
 *
 * @author wangchao
 * @since 2024/10/29 09:26
 */
@Data
public class HostRealtimeStatistics {
    /**
     * disk unit size 1024*1024,to format disk GB unit
     */
    private static final long DISK_UNIT = 1024 * 1024;

    private float total;
    private float used;
    private float available;
    private String use;

    /**
     * build disk realtime statistics
     *
     * @param diskMonitor disk monitor string
     * @return HostRealtimeStatistics
     */
    public static HostRealtimeStatistics ofDisk(String diskMonitor) {
        HostRealtimeStatistics bean = new HostRealtimeStatistics();
        String[] dev = diskMonitor.split(" ");
        if (dev.length == 7) {
            bean.total += Math.round((float) Long.parseLong(dev[2]) / DISK_UNIT);
            bean.used += Math.round((float) Long.parseLong(dev[3]) / DISK_UNIT);
            bean.available += Math.round((float) Long.parseLong(dev[4]) / DISK_UNIT);
            bean.use = dev[5].replace("%", "");
        }
        return bean;
    }

    /**
     * build memory realtime statistics
     *
     * @param memory memory monitor string
     * @return HostRealtimeStatistics
     */
    public static HostRealtimeStatistics ofMemory(String memory) {
        HostRealtimeStatistics bean = new HostRealtimeStatistics();
        String[] split = memory.split(" ");
        bean.total += Float.parseFloat(split[1]);
        bean.used += Float.parseFloat(split[2]);
        bean.available += Float.parseFloat(split[3]);
        bean.use = toString(((bean.total - bean.available) / bean.total) * 100);
        return bean;
    }

    /**
     * host statistics format to string %.2f
     *
     * @param metric metric
     * @return formatted string
     */
    public static String toString(float metric) {
        return String.format(Locale.ROOT, "%.2f", metric);
    }
}
