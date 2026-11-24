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
