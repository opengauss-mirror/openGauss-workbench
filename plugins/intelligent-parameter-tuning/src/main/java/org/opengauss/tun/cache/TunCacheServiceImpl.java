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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;

/**
 * CaffeineCacheServiceImpl
 *
 * @author liu
 * @since 2023-12-08
 */
public class TunCacheServiceImpl<K, V> implements TunCache<K, V> {
    private final Cache<K, V> tunBuffer;

    /**
     * CaffeineCacheServiceImpl
     *
     * @param initialValue initialValue
     * @param maxValue maximumValue
     * @param isFlag       isFlag
     */
    public TunCacheServiceImpl(final int initialValue, final long maxValue, final boolean isFlag) {
        if (isFlag) {
            this.tunBuffer = Caffeine.newBuilder()
                    .weakKeys()
                    .initialCapacity(initialValue)
                    .maximumSize(maxValue)
                    .expireAfterWrite(Long.MAX_VALUE, TimeUnit.MINUTES)
                    .build();
        } else {
            this.tunBuffer = Caffeine.newBuilder()
                    .initialCapacity(initialValue)
                    .maximumSize(maxValue)
                    .expireAfterWrite(Long.MAX_VALUE, TimeUnit.MINUTES)
                    .build();
        }
    }

    @Override
    public V obtain(K key) {
        return tunBuffer.getIfPresent(key);
    }

    @Override
    public V put(K key, V value) {
        V oldValue = tunBuffer.getIfPresent(key);
        tunBuffer.put(key, value);
        return oldValue;
    }

    @Override
    public boolean includeKey(K key) {
        return tunBuffer.asMap().containsKey(key);
    }

    @Override
    public V detach(K key) {
        V value = tunBuffer.getIfPresent(key);
        this.tunBuffer.invalidate(key);
        this.tunBuffer.cleanUp();
        return value;
    }

    @Override
    public boolean cleanUp() {
        this.tunBuffer.invalidateAll();
        this.tunBuffer.cleanUp();
        return true;
    }
}
