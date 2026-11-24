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
