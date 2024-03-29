/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.listener;

import com.gitee.starblues.spring.MainApplicationContext;
import com.gitee.starblues.spring.SpringBeanFactory;
import org.opengauss.admin.common.core.vo.MenuVo;
import org.opengauss.admin.system.plugin.facade.MenuFacade;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

/**
 * @Author wuyuebin
 * @Date 2023/4/13 09:57
 * @Description
 */
public class PluginListener implements ApplicationListener<ApplicationEvent> {
    private final String pluginId = "alert-monitor";

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationReadyEvent) {
            MainApplicationContext context = ((ApplicationReadyEvent) event).getApplicationContext().getBean(
                MainApplicationContext.class);
            SpringBeanFactory factory = context.getSpringBeanFactory();
            MenuFacade menuFacade = factory.getBean(MenuFacade.class);
            if (menuFacade != null) {
                MenuVo firstMenu = menuFacade.savePluginMenu(pluginId, "告警监控", "Alert Monitor", 6, "alert");
                menuFacade.savePluginMenu(pluginId, "告警记录", "Alert Record", 22, "vem/alert/alertRecord",
                    firstMenu.getMenuId());
                menuFacade.savePluginRoute(pluginId, "告警详细", "Alert Record Detail", "vem/alert/recordDetail",
                    firstMenu.getMenuId());
                menuFacade.savePluginMenu(pluginId, "告警配置", "Alert Config", 23, "vem/alert/AlertClusterNodeConf",
                    firstMenu.getMenuId());
                menuFacade.savePluginMenu(pluginId, "告警模板", "Alert Template", 24, "vem/alert/alertTemplate",
                    firstMenu.getMenuId());
                menuFacade.savePluginMenu(pluginId, "告警规则", "Alert Rule", 25, "vem/alert/alertRule",
                    firstMenu.getMenuId());
                menuFacade.savePluginMenu(pluginId, "通知模板", "Notify Template", 26, "vem/notify/notifyTemplate",
                    firstMenu.getMenuId());
                menuFacade.savePluginMenu(pluginId, "通知方式", "Notify Way", 27, "vem/notify/notifyWay",
                    firstMenu.getMenuId());
            }
        }
        if (event instanceof ContextClosedEvent) {
            MainApplicationContext context = ((ContextClosedEvent) event).getApplicationContext().getBean(
                MainApplicationContext.class);
            SpringBeanFactory factory = context.getSpringBeanFactory();
            MenuFacade menuFacade = factory.getBean(MenuFacade.class);
            if (menuFacade != null) {
                menuFacade.deletePluginMenu(pluginId);
            }
        }
    }
}
