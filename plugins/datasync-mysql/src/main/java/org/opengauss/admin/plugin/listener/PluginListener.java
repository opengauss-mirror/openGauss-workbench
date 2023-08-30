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
 * PluginListener.java
 *
 * IDENTIFICATION
 * datasync-mysql/src/main/java/org/opengauss/admin/plugin/listener/PluginListener.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.plugin.listener;

import com.gitee.starblues.spring.MainApplicationContext;
import com.gitee.starblues.spring.SpringBeanFactory;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.vo.MenuVo;
import org.opengauss.admin.system.plugin.facade.MenuFacade;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * @className: Listener
 * @description:
 * @author: xielibo
 * @date: 2022-08-16 3:48 PM
 **/
@Slf4j
public class PluginListener implements ApplicationListener<ApplicationEvent> {

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationEnvironmentPreparedEvent) {
            log.info("mysql sync-tool init env");
        } else if (event instanceof ApplicationPreparedEvent) {
            log.info("mysql sync-tool init complete");
        } else if (event instanceof ContextRefreshedEvent) {
            log.info("mysql sync-tool was refreshed");
        } else if (event instanceof ApplicationReadyEvent) {
            MainApplicationContext context = ((ApplicationReadyEvent) event).getApplicationContext().getBean(MainApplicationContext.class);
            SpringBeanFactory factory = context.getSpringBeanFactory();
            MenuFacade menuFacade = factory.getBean(MenuFacade.class);
            if (menuFacade != null) {
                MenuVo syncParent = menuFacade.savePluginMenu("datasync-mysql", "迁移工具", "sync tool", 5, "sync");
                menuFacade.savePluginMenu("datasync-mysql", "Mysql数据迁移", "mysql migration", 1, "index", syncParent.getMenuId());
            }
            log.info("mysql sync-tool start complete");
        } else if (event instanceof ContextClosedEvent) {
            MainApplicationContext context = ((ContextClosedEvent) event).getApplicationContext().getBean(MainApplicationContext.class);
            SpringBeanFactory factory = context.getSpringBeanFactory();
            MenuFacade menuFacade = factory.getBean(MenuFacade.class);
            if (menuFacade != null) {
                menuFacade.deletePluginMenu("datasync-mysql");
            }
            log.info("mysql sync-tool is stopped");
        }
    }
}
