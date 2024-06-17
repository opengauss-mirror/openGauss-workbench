/*
 * Copyright (c) 2024 Huawei Technologies Co.,Ltd.
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
 * OauthLoginPluginListener.java
 *
 * IDENTIFICATION
 * oauth-login/src/main/java/org/opengauss/admin/plugin/listener/OauthLoginPluginListener.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * @date 2024/6/17 20:39
 * @since 0.0
 */
@Slf4j
public class OauthLoginPluginListener implements ApplicationListener<ApplicationEvent> {

    /**
     * register plugin menu information
     *
     * @param event applicationEvent
     */
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationEnvironmentPreparedEvent) {
            log.info("oauth login init env");
        } else if (event instanceof ApplicationPreparedEvent) {
            log.info("oauth login init complete");
        } else if (event instanceof ContextRefreshedEvent) {
            log.info("oauth login was refreshed");
        } else if (event instanceof ApplicationReadyEvent) {
            log.info("oauth login start complete");
        } else if (event instanceof ContextClosedEvent) {
            log.info("oauth login is stopped");
        }
    }
}
