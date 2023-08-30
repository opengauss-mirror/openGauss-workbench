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
 * IdUtils.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/utils/uuid/IdUtils.java
 *
 * -------------------------------------------------------------------------
 */


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
