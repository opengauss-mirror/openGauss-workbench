/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.nctigba.observability.instance.config.runner;

import com.gitee.starblues.bootstrap.annotation.AutowiredType;

import lombok.extern.slf4j.Slf4j;

import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * EncryptionRunner
 *
 * @author: wangchao
 * @Date: 2025/9/11 21:21
 * @since 7.0.0-RC2
 **/
@Component
@Slf4j
public class EncryptionRunner implements ApplicationRunner {
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    EncryptionUtils encryptionUtils;

    @Override
    public void run(ApplicationArguments args) {
        encryptionUtils.getKey();
    }
}
