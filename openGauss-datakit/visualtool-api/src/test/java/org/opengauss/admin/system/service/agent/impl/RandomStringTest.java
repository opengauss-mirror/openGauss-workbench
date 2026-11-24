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

package org.opengauss.admin.system.service.agent.impl;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Test;
import org.opengauss.admin.common.utils.RandomCharGenerator;

/**
 * RandomStringTest
 *
 * @author: wangchao
 * @Date: 2025/9/25 09:39
 * @since 7.0.0-RC2
 **/
@Slf4j
public class RandomStringTest {
    @Test
    public void testGeneratorRandomString() {
        log.info(RandomCharGenerator.generateRandomBased128String());
        log.info(RandomCharGenerator.generateRandomBased128String());
        log.info(RandomCharGenerator.generateRandomBased128String());
        log.info(RandomCharGenerator.generateRandomBased128String());
    }
}
