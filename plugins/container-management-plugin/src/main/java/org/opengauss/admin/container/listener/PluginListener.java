/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
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
 * openGauss-visualtool-plugin/src/main/java/org/opengauss/admin/plugin/listener/PluginListener.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.container.listener;

import com.gitee.starblues.spring.MainApplicationContext;
import com.gitee.starblues.spring.SpringBeanFactory;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.vo.MenuVo;
import org.opengauss.admin.container.config.PluginExtensionInfoConfig;
import org.opengauss.admin.system.plugin.facade.MenuFacade;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * PluginListener
 *
 * @since 2024-08-16 13:48
 **/
@Slf4j
public class PluginListener implements ApplicationListener<ApplicationEvent> {
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        String pluginId = PluginExtensionInfoConfig.PLUGIN_ID;
        if (event instanceof ApplicationEnvironmentPreparedEvent) {
            log.info("plugin init env");
        } else if (event instanceof ApplicationPreparedEvent) {
            log.info("plugin init complete");
        } else if (event instanceof ContextRefreshedEvent) {
            log.info("plugin was refreshed");
        } else if (event instanceof ApplicationReadyEvent) {
            MainApplicationContext context =
                    ((ApplicationReadyEvent) event).getApplicationContext().getBean(MainApplicationContext.class);
            SpringBeanFactory factory = context.getSpringBeanFactory();
            MenuFacade menuFacade = factory.getBean(MenuFacade.class);
            if (menuFacade != null) {
                MenuVo parentMenu = menuFacade.savePluginMenu(pluginId, "容器管理", "test menu", 50, "");
                menuFacade.savePluginMenu(pluginId, "集群列表", "ClusterList", 1, "cluster-manage/cluster-list",
                        parentMenu.getMenuId());
                menuFacade.savePluginRoute(pluginId, "集群详情", "cluster-manage/cluster-list/details",
                        parentMenu.getMenuId());
            }
            log.info("plugin start complete");
        } else if (event instanceof ContextClosedEvent) {
            MainApplicationContext context =
                    ((ContextClosedEvent) event).getApplicationContext().getBean(MainApplicationContext.class);
            SpringBeanFactory factory = context.getSpringBeanFactory();
            MenuFacade menuFacade = factory.getBean(MenuFacade.class);
            if (menuFacade != null) {
                menuFacade.deletePluginMenu(pluginId);
            }
            log.info("plugin is stopped");
        }
    }
}
