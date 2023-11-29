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
 * DataMigrationPluginListener.java
 *
 * IDENTIFICATION
 * data-migration/src/main/java/org/opengauss/admin/plugin/listener/DataMigrationPluginListener.java
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
 * @author xielibo
 * @date 2023/01/14 09:01
 */
@Slf4j
public class DataMigrationPluginListener implements ApplicationListener<ApplicationEvent> {

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationEnvironmentPreparedEvent) {
            log.info("DataMigration init env");
        } else if (event instanceof ApplicationPreparedEvent) {
            log.info("DataMigration init complete");
        } else if (event instanceof ContextRefreshedEvent) {
            log.info("DataMigration was refreshed");
        } else if (event instanceof ApplicationReadyEvent) {
            MainApplicationContext context = ((ApplicationReadyEvent) event).getApplicationContext().getBean(MainApplicationContext.class);
            SpringBeanFactory factory = context.getSpringBeanFactory();
            MenuFacade menuFacade = factory.getBean(MenuFacade.class);
            if (menuFacade != null) {
                String pluginId = "data-migration";
                MenuVo parentMenu = menuFacade.savePluginMenu(pluginId, "数据迁移", "data migration tool", 8, "migration");
                menuFacade.savePluginMenu(pluginId,"迁移任务中心","task center",1, "index",parentMenu.getMenuId());
                menuFacade.savePluginMenu(pluginId, "消息队列配置中心",
                        "Message Queue Software config center", 2, "thirdPartySoftwareConfig",
                        parentMenu.getMenuId());
                menuFacade.savePluginRoute(pluginId, "创建迁移任务", "taskConfig", parentMenu.getMenuId());
                menuFacade.savePluginRoute(pluginId, "任务详情", "taskDetail", parentMenu.getMenuId());
            }
            log.info("DataMigration start complete");
        } else if (event instanceof ContextClosedEvent) {
            MainApplicationContext context = ((ContextClosedEvent) event).getApplicationContext().getBean(MainApplicationContext.class);
            SpringBeanFactory factory = context.getSpringBeanFactory();
            MenuFacade menuFacade = factory.getBean(MenuFacade.class);
            if (menuFacade != null) {
                menuFacade.deletePluginMenu("data-migration");
            }
            log.info("DataMigration is stopped");
        }
    }
}
