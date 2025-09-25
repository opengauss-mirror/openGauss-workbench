/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.system.service.agent.impl;

import lombok.extern.slf4j.Slf4j;

import org.junit.Test;
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
