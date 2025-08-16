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

package org.opengauss.admin.system.service.agent.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.common.utils.CommonUtils;

/**
 * PathValidateTest
 *
 * @author: wangchao
 * @Date: 2025/8/15 11:52
 * @since 7.0.0-RC2
 **/
public class PathValidateTest {
    @BeforeAll
    static void initEnv() {
        System.setProperty("os.name", "Linux");
    }

    @Test
    void testPathValidate_ValidPath() {
        // Test with valid path
        assertDoesNotThrow(() -> CommonUtils.pathValidate("/valid/path"));
    }

    @Test
    void testPathValidate_NullPath() {
        // Test with null path
        OpsException exception = assertThrows(OpsException.class, () -> {
            CommonUtils.pathValidate(null);
        });
        assertNotNull(exception.getMessage());
    }

    @Test
    void testPathValidate_EmptyPath() {
        // Test with empty path
        OpsException exception = assertThrows(OpsException.class, () -> {
            CommonUtils.pathValidate("");
        });
        assertNotNull(exception.getMessage());
    }

    @Test
    void testPathValidate_InvalidCharacters() {
        // Test with path containing invalid characters
        OpsException exception = assertThrows(OpsException.class, () -> {
            CommonUtils.pathValidate("/invalid/|path");
        });
        assertNotNull(exception.getMessage());
    }

    @Test
    void testPathValidate_RelativePath() {
        // Test with relative path
        OpsException exception = assertThrows(OpsException.class, () -> {
            CommonUtils.pathValidate("../relative/path");
        });
        assertNotNull(exception.getMessage());
    }

    @Test
    void testPathValidate_TooLongPath() {
        // Test with path exceeding maximum allowed length
        String longPath = "/" + "a".repeat(255); // Assuming max length is 255
        OpsException exception = assertThrows(OpsException.class, () -> {
            CommonUtils.pathValidate(longPath);
        });
        assertNotNull(exception.getMessage());
    }

    @Test
    void testPathValidate_LongPath() {
        // Test with path exceeding maximum allowed length
        String longPath = "/" + "a".repeat(254); // Assuming max length is 255
        assertDoesNotThrow(() -> CommonUtils.pathValidate(longPath));
    }

    @Test
    void testPathValidate_TooDeepPath() {
        // Test with path exceeding maximum allowed deep
        String deepPath = "/a/b/c/d/e/f"; // Assuming max deep is 6
        OpsException exception = assertThrows(OpsException.class, () -> {
            CommonUtils.pathValidate(deepPath);
        });
        assertNotNull(exception.getMessage());
    }

    @Test
    void testPathValidate_DeepPath() {
        // Test with path exceeding maximum allowed deep
        String deepPath = "/a/b/c/d/e"; // Assuming max deep is 5
        assertDoesNotThrow(() -> CommonUtils.pathValidate(deepPath));
    }
}
