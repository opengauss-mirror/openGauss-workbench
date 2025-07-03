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

package org.opengauss.agent.duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opengauss.agent.utils.DurationUtils;

import java.time.Duration;
import java.time.Period;

/**
 * DurationTest
 *
 * @author: wangchao
 * @Date: 2025/4/16 10:43
 * @Description: DurationTest
 * @since 7.0.0-RC2
 **/
public class DurationTest {
    @Test
    public void test1() {
        Duration p0 = Duration.parse("PT-1s");
        assertEquals(-1, p0.toSeconds());
    }

    @Test
    public void test2() {
        Duration p0 = Duration.parse("PT0s");
        assertEquals(0, p0.toSeconds());
    }

    @Test
    public void test3() {
        Duration p0 = Duration.parse("P1D");
        assertEquals(86400, p0.toSeconds());
    }

    @Test
    public void test4() {
        Period p0 = Period.parse("P2W");
        long seconds = Duration.ofDays(p0.getDays()).toSeconds();
        assertEquals(1209600, seconds);
    }

    @Test
    public void test5() {
        Period p0 = Period.parse("P3M2D");
        assertEquals(3, p0.getMonths());
        assertEquals(2, p0.getDays());
    }

    @Test
    public void test6() {
        Duration p0 = Duration.parse("PT0.01s");
        assertEquals(10, p0.toMillis());
    }

    @Test
    public void test7() {
        Duration parse = Duration.parse("PT10s");
        assertEquals(10000, parse.toMillis());
    }

    @Test
    public void test8() {
        Duration parse2 = Duration.parse("PT2M10s");
        assertEquals(130000, parse2.toMillis());
    }

    @Test
    public void test9() {
        Duration parse3 = Duration.parse("PT1H2M10s");
        assertEquals(3730000, parse3.toMillis());
    }

    @Test
    public void test10() {
        Duration parse4 = Duration.parse("P1D");
        assertEquals(86400000, parse4.toMillis());
    }

    @Test
    public void test11() {
        Duration parse5 = Duration.parse("P100D");
        assertEquals(8640000000L, parse5.toMillis());
    }

    @Test
    public void test12() {
        long parse6 = DurationUtils.parseFixedTimeToMillis("P3W4DT5H6M7.5S"); // 固定时间周期 3周4天5小时6分7.5秒
        assertEquals(2178367500L, parse6);
    }

    @Test
    public void test13() {
        String input = "P1Y2M3W4DT5H6M7.5S"; // 1年2月3周4天5小时6分7.5秒
        long totalMs = DurationUtils.parseToMillis(input);
        assertEquals(38984767500L, totalMs);
    }
}
