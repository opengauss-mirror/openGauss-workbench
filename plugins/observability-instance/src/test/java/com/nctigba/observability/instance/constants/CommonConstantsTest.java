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