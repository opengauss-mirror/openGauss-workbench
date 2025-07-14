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

import static org.junit.jupiter.api.Assertions.assertEquals;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Test;
import org.opengauss.admin.common.utils.MathUtils;

/**
 * MathUtilsTest
 *
 * @author: wangchao
 * @Date: 2025/6/21 18:45
 * @since 7.0.0-RC2
 **/
@Slf4j
public class MathUtilsTest {
    @Test
    public void testAdd() {
        String s = "1";
        String s2 = "2";
        String result = MathUtils.add(s, s2);
        log.info("{} add {} result: {}", s, s2, result);
        assertEquals("3.00", result);
    }

    @Test
    public void testFormatPrecisionFour() {
        String s = "1";
        String result = MathUtils.formatScaleTwo(s);
        log.info("{} formatScaleTwo result: {}", s, result);
        assertEquals("1.00", result);
    }

    @Test
    public void testPercent() {
        String s = "1";
        String s2 = "10";
        String result = MathUtils.percent(s, s2);
        log.info("{}/{} percent   result: {}", s, s2, result);
        assertEquals("10.00", result);
    }

    @Test
    public void testPercent_0() {
        String s = "1";
        String s2 = "0";
        String result = MathUtils.percent(s, s2);
        log.info("{}/{} percent   result: {}", s, s2, result);
        assertEquals("0.00", result);
    }

    @Test
    public void testPercent_3() {
        String s = "1";
        String s2 = "3";
        String result = MathUtils.percent(s, s2);
        log.info("{}/{} percent   result: {}", s, s2, result);
        assertEquals("33.33", result);
    }
}
