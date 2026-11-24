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

package org.opengauss.tun.cache;

/**
 * CacheService
 *
 * @author liu
 * @since 2023-12-08
 */
public interface TunCache<K, V> {
    /**
     * obtain
     *
     * @param key key
     * @return object
     */
    V obtain(K key);

    /**
     * put
     *
     * @param key   key
     * @param value value
     * @return V
     */
    V put(K key, V value);

    /**
     * includeKey
     *
     * @param key key
     * @return boolean
     */
    boolean includeKey(K key);

    /**
     * detach
     *
     * @param key key
     * @return V
     */
    V detach(K key);

    /**
     * cleanUp
     *
     * @return boolean
     */
    boolean cleanUp();
}
