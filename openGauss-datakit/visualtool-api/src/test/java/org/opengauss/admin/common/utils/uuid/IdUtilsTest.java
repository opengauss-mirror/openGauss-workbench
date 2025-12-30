/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.common.utils.uuid;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * IdUtilsTest
 *
 * @author wangchao
 * @date 2025-12-11
 * @since 7.0.0-RC3
 */
public class IdUtilsTest {
    @Test
    public void testUUID() {
        String randomId = IdUtils.randomUuid();
        String fastId = IdUtils.fastUuid();
        Assertions.assertEquals(randomId.length(), fastId.length());
        Assertions.assertEquals(randomId.contains("-"), fastId.contains("-"));
        Assertions.assertEquals(randomId.split("-").length, fastId.split("-").length);
    }
}
