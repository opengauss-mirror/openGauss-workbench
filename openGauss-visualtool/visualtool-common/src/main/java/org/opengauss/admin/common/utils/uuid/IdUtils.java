package org.opengauss.admin.common.utils.uuid;

/**
 * ID generate tool
 *
 * @author xielibo
 */
public class IdUtils {
    /**
     * random UUID
     *
     * @return UUID
     */
    public static String randomUuid() {
        return UUID.randomUuid().toString();
    }

    /**
     * simple uuid
     *
     * @return UUID
     */
    public static String simpleUuid() {
        return UUID.randomUuid().toString(true);
    }

    /**
     * Get a random UUID and use ThreadLocalRandom with better performance to generate a UUID
     *
     */
    public static String fastUuid() {
        return UUID.fastUuid().toString();
    }

    /**
     * Simplified UUID, remove the horizontal line, use ThreadLocalRandom with better performance to generate UUID
     *
     */
    public static String fastSimpleUuid() {
        return UUID.fastUuid().toString(true);
    }
}
