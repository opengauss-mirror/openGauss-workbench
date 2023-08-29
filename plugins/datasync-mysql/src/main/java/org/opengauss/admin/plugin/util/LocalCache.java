/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
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
 * LocalCache.java
 *
 * IDENTIFICATION
 * datasync-mysql/src/main/java/org/opengauss/admin/plugin/util/LocalCache.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.plugin.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @className: LocalCache
 * @description: LocalCache
 * @author: xielibo
 * @date: 2022-10-28 13:51
 **/
public class LocalCache {


    private final static Map<String, Entity> map = new HashMap<>();

    /**
     * add cache
     *
     */
    public synchronized static void put(String key, Object data) {
        LocalCache.put(key, data, 0);
    }

    /**
     * add cache
     *
     */
    public synchronized static void put(String key, Object data, long expire) {
        LocalCache.remove(key);
        map.put(key, new Entity(data));
    }

    /**
     * get cache
     *
     */
    public synchronized static Object get(String key) {
        Entity entity = map.get(key);
        return entity == null ? null : entity.getValue();
    }

    /**
     * get cache
     *
     */
    public synchronized static <T> T get(String key, Class<T> clazz) {
        return clazz.cast(LocalCache.get(key));
    }

    /**
     * remove cache
     *
     * @param key
     * @return
     */
    public synchronized static Object remove(String key) {
        Entity entity = map.remove(key);
        if (entity == null) {
            return null;
        }
        return entity.getValue();

    }

    private static class Entity {
        private Object value;

        public Entity(Object value) {
            this.value = value;
        }

        /**
         * get
         *
         * @return
         */
        public Object getValue() {
            return value;
        }
    }
}
