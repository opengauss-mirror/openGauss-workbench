/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  PluginListener.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/listener/PluginListener.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.listener;

import com.gitee.starblues.spring.MainApplicationContext;
import com.gitee.starblues.spring.SpringBeanFactory;
import com.nctigba.alert.monitor.schedule.TaskRegistrar;
import com.nctigba.alert.monitor.util.SpringContextUtils;
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
                menuFacade.savePluginMenu(pluginId, "告警屏蔽", "Alert Shielding", 26, "vem/alert/AlertShielding",
                        firstMenu.getMenuId());
                menuFacade.savePluginMenu(pluginId, "通知模板", "Notify Template", 27, "vem/notify/notifyTemplate",
                    firstMenu.getMenuId());
                menuFacade.savePluginMenu(pluginId, "通知方式", "Notify Way", 28, "vem/notify/notifyWay",
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
            // clear the schedule
            TaskRegistrar taskRegistrar = SpringContextUtils.getBean(TaskRegistrar.class);
            taskRegistrar.destroy();
        }
    }
}
