/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
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
 */

package org.opengauss.agent.event.handler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;

/**
 * HistoricalStorageCleanerManager
 *
 * @author: wangchao
 * @Date: 2025/6/10 09:39
 * @since 7.0.0-RC2
 **/
public class HistoricalStorageCleanerManager {
    private static final ConcurrentMap<String, HistoryDataCleaner> CLEANER_CONCURRENT_MAP = new ConcurrentHashMap<>();

    /**
     * register a cleaner
     *
     * @param keepPeriod keep period
     * @param remappingFunction remapping function
     */
    public static void registerCleanScheduledFuture(String keepPeriod,
        BiFunction<String, ? super HistoryDataCleaner, ? extends HistoryDataCleaner> remappingFunction) {
        CLEANER_CONCURRENT_MAP.compute(keepPeriod, remappingFunction);
    }

    /**
     * unregister all cleaners
     */
    public static void unregister() {
        CLEANER_CONCURRENT_MAP.forEach((period, cleaner) -> {
            if (cleaner != null) {
                cleaner.shutdownNow();
            }
        });
        CLEANER_CONCURRENT_MAP.clear();
    }
}
