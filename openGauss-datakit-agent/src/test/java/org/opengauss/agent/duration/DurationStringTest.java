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

/**
 * DurationStringTest
 *
 * @author: wangchao
 * @Date: 2025/4/3 16:23
 * @Description: DurationStringTest
 * @since 7.0.0-RC2
 **/
public class DurationStringTest {
    @Test
    void testDurationToString() {
        assertEquals("1S", DurationUtils.formatInterval(1000));
        assertEquals("1.2S", DurationUtils.formatInterval(1200));
        assertEquals("0.2S", DurationUtils.formatInterval(200));
    }
}