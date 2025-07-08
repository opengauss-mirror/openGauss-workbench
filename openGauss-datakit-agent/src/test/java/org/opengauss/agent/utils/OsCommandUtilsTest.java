/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
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
 */

package org.opengauss.agent.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ReflectionUtils;

import java.util.List;

/**
 * OsCommandUtilsTest
 *
 * @author: wangchao
 * @Date: 2025/7/7 10:16
 * @since 7.0.0-RC2
 **/
public class OsCommandUtilsTest {
    @Test
    public void testAllowedCommandReflection() {
        String command = "echo hello";
        ReflectionUtils.findMethod(OsCommandUtils.class, "isSafeCommand", String.class).ifPresent(method -> {
            ReflectionUtils.makeAccessible(method);
            assertEquals(true, ReflectionUtils.invokeMethod(method, OsCommandUtils.class, command));
        });
    }

    @Test
    public void testAllowedCommandProtected() {
        String command = "echo hello";
        assertEquals(true, OsCommandUtils.isSafeCommand(command));
    }

    @Test
    public void testForceRefreshAllowedCommand() {
        String command = "msg ax";
        OsCommandUtils.forceRefreshAllowedCommand(List.of(command));
        assertEquals(true, OsCommandUtils.isSafeCommand(command));
    }
}
