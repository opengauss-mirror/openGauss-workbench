/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
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
 * -------------------------------------------------------------------------
 *
 * MyPluginInitializerListener.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-api/src/main/java/org/opengauss/admin/web/listener/MyPluginInitializerListener.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.web.listener;

import com.gitee.starblues.integration.listener.PluginInitializerListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author xielibo
 * @version 1.0
 */
@Component
@Slf4j
public class MyPluginInitializerListener implements PluginInitializerListener {
    @Override
    public void before() {
        log.info("before init.");
    }

    @Override
    public void complete() {
        log.info("init complete.");
    }

    @Override
    public void failure(Throwable throwable) {
        log.error("init error:" + throwable.getMessage());
    }
}
