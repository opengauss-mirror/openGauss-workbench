/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.env;

import lombok.extern.slf4j.Slf4j;

import org.opengauss.global.AppConfigLoader;
import org.opengauss.global.Constants;
import org.testng.ISuite;
import org.testng.ISuiteListener;

/**
 * TestEnvironmentListener
 *
 * @author: wangchao
 * @Date: 2025/9/11 21:21
 * @since 7.0.0-RC2
 **/
@Slf4j
public class TestEnvironmentListener implements ISuiteListener {
    @Override
    public void onStart(ISuite suite) {
        log.info("testng start " + suite.getName());
        // 可以在套件级别进行额外的环境设置
        AppConfigLoader.loadConfig();
        Constants.loadToken();
    }

    @Override
    public void onFinish(ISuite suite) {
        log.info("testng finished " + suite.getName());
        // 可以在套件级别进行额外的环境清理
    }
}
