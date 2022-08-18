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
