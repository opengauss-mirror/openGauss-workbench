/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.constants;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.Test;

/**
 * CommonConstantsTest
 *
 * @since 2023年7月17日
 */
class CommonConstantsTest {
    @Test
    void test() {
        assertThrows(InvocationTargetException.class, () -> {
            var cons = CommonConstants.class.getDeclaredConstructor();
            cons.setAccessible(true);
            cons.newInstance();
        });
    }
}