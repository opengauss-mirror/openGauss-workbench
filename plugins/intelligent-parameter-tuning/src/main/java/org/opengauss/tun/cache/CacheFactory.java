/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
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
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.tun.cache;

/**
 * CacheFactory
 *
 * @author liu
 * @since 2023-12-08
 */
public class CacheFactory {
    private static final Integer INIT_VALUE = 10;

    private static final Integer MAX_VALUE = 1000;

    private static final TunCache<String, String> STATUS_CACHE =
            new TunCacheServiceImpl<>(INIT_VALUE, MAX_VALUE, false);

    private static final TunCache<String, Integer> PROCESS_CACHE =
            new TunCacheServiceImpl<>(INIT_VALUE, MAX_VALUE, false);

    private static final TunCache<String, String> PYENON_ENV_CACHE =
            new TunCacheServiceImpl<>(INIT_VALUE, MAX_VALUE, false);

    private static final TunCache<String, String> HEBO_ENV_CACHE =
            new TunCacheServiceImpl<>(INIT_VALUE, MAX_VALUE, false);

    private static final TunCache<String, String> WORKLOAD_CACHE =
            new TunCacheServiceImpl<>(INIT_VALUE, MAX_VALUE, false);

    private static final TunCache<String, Integer> LOG_CACHE =
            new TunCacheServiceImpl<>(INIT_VALUE, MAX_VALUE, false);

    private CacheFactory() {
    }

    /**
     * getStatusCache
     *
     * @return TunCache
     */
    public static TunCache<String, String> getStatusCache() {
        return STATUS_CACHE;
    }

    /**
     * getProcessCache
     *
     * @return CacheService
     */
    public static TunCache<String, Integer> getProcessCache() {
        return PROCESS_CACHE;
    }

    /**
     * getPythonCache
     *
     * @return TunCache
     */
    public static TunCache<String, String> getPythonCache() {
        return PYENON_ENV_CACHE;
    }

    /**
     * getHeboCache
     *
     * @return TunCache
     */
    public static TunCache<String, String> getHeboCache() {
        return HEBO_ENV_CACHE;
    }

    /**
     * getLogCache
     *
     * @return TunCache
     */
    public static TunCache<String, Integer> getLogCache() {
        return LOG_CACHE;
    }

    /**
     * getWorkCache
     *
     * @return TunCache
     */
    public static TunCache<String, String> getWorkCache() {
        return WORKLOAD_CACHE;
    }
}
