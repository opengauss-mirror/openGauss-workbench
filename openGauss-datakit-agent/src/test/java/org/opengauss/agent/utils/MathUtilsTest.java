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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * MathUtilsTest
 *
 * @author: wangchao
 * @Date: 2025/7/3 16:12
 * @since 7.0.0-RC2
 **/
public class MathUtilsTest {
    /**
     * Test convertBytesToGigabytes method.
     */
    @Test
    public void testConvertBytesToGigabytes() {
        // Test normal cases
        assertEquals(1.0, MathUtils.convertBytesToGigabytes(1024L * 1024 * 1024), 0.0001);
        assertEquals(1.5, MathUtils.convertBytesToGigabytes(1024L * 1024 * 1024 * 3 / 2), 0.0001);
        // Test boundary cases
        assertEquals(0.0, MathUtils.convertBytesToGigabytes(0), 0.0001);
        assertEquals(9.31323E-10, MathUtils.convertBytesToGigabytes(1), 0.0001);
        // Test large value
        assertEquals(1000.0, MathUtils.convertBytesToGigabytes(1024L * 1024 * 1024 * 1000), 0.0001);
    }

    /**
     * Test divide1024 method.
     */
    @Test
    public void testDivide1024() {
        // Test normal cases
        assertEquals(1.0, MathUtils.divide1024(1024), 0.0001);
        assertEquals(1.5, MathUtils.divide1024(1536), 0.0001);
        // Test boundary cases
        assertEquals(0.0, MathUtils.divide1024(0), 0.0001);
        assertEquals(0.000976562, MathUtils.divide1024(1), 0.0001);
        // Test large value
        assertEquals(1000.0, MathUtils.divide1024(1024 * 1000), 0.0001);
    }

    /**
     * Test calculateUtilizationPercentage method.
     */
    @Test
    public void testCalculateUtilizationPercentage() {
        // Test normal cases
        assertEquals(50.0, MathUtils.calculateUtilizationPercentage(500, 1000), 0.0001);
        assertEquals(33.4, MathUtils.calculateUtilizationPercentage(666, 1000), 0.0001);
        // Test boundary cases
        // total = 0
        assertEquals(0.0, MathUtils.calculateUtilizationPercentage(0, 0), 0.0001);
        // available = 0
        assertEquals(100.0, MathUtils.calculateUtilizationPercentage(0, 1000), 0.0001);
        // available = total
        assertEquals(0.0, MathUtils.calculateUtilizationPercentage(1000, 1000), 0.0001);
        // Test edge cases
        assertEquals(99.9999, MathUtils.calculateUtilizationPercentage(1, 1000000), 0.0001);
        assertEquals(0.0001, MathUtils.calculateUtilizationPercentage(999999, 1000000), 0.0001);
    }
}
