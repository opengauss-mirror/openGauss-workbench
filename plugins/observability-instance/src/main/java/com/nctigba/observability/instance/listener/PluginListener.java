/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package com.nctigba.observability.instance.listener;

import org.opengauss.admin.common.core.vo.MenuVo;
import org.opengauss.admin.system.plugin.facade.MenuFacade;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

import com.gitee.starblues.spring.MainApplicationContext;
import com.gitee.starblues.spring.SpringBeanFactory;

/**
 * @description: Plugin listener
 * @author: YangZiHao
 * @date: 2022/12/4 15:05
 */
public class PluginListener implements ApplicationListener<ApplicationEvent> {
    public static final String pluginId = "observability-instance";

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationReadyEvent) {
            MainApplicationContext context = ((ApplicationReadyEvent) event).getApplicationContext()
                    .getBean(MainApplicationContext.class);
            SpringBeanFactory factory = context.getSpringBeanFactory();
            MenuFacade menuFacade = factory.getBean(MenuFacade.class);
            if (menuFacade != null) {
                MenuVo firstMenu = menuFacade.savePluginMenu(pluginId, "智能运维", "Intelligent OPS", 4, "monitor");
                menuFacade.savePluginMenu(pluginId, "实例监控", "Instance Monitoring", 11, "vem/dashboard/instance",
                        firstMenu.getMenuId());
                menuFacade.saveIndexInstanceRoute(pluginId, "实例监控", "vem/dashboard/instance");
                menuFacade.savePluginRoute(pluginId, "实例监控详情", "Instance Monitoring Details", "vem/sql_detail",
                        firstMenu.getMenuId());
                menuFacade.savePluginRoute(pluginId, "会话详情", "Session Details", "vem/sessionDetail",
                        firstMenu.getMenuId());
            }
        } else if (event instanceof ContextClosedEvent) {
            MainApplicationContext context = ((ContextClosedEvent) event).getApplicationContext()
                    .getBean(MainApplicationContext.class);
            SpringBeanFactory factory = context.getSpringBeanFactory();
            MenuFacade menuFacade = factory.getBean(MenuFacade.class);
            if (menuFacade != null) {
                menuFacade.deletePluginMenu(pluginId);
            }
        }
    }
}
