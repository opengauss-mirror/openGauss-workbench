package com.nctigba.observability.sql.listener;

import com.gitee.starblues.spring.MainApplicationContext;
import com.gitee.starblues.spring.SpringBeanFactory;
import org.opengauss.admin.common.core.vo.MenuVo;
import org.opengauss.admin.system.plugin.facade.MenuFacade;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

/**
 * @description: Plugin listener
 * @author: YangZiHao
 * @date: 2022/12/4 15:05
 */
public class PluginListener implements ApplicationListener<ApplicationEvent> {
    public static final String pluginId = "observability-sql-diagnosis";

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationReadyEvent) {
            MainApplicationContext context = ((ApplicationReadyEvent) event).getApplicationContext()
                    .getBean(MainApplicationContext.class);
            SpringBeanFactory factory = context.getSpringBeanFactory();
            MenuFacade menuFacade = factory.getBean(MenuFacade.class);
            if (menuFacade != null) {
                MenuVo firstMenu = menuFacade.savePluginMenu(pluginId, "智能运维", "Intelligent OPS", 4, "monitor");
                menuFacade.savePluginMenu(pluginId, "SQL诊断", "SQL Diagnosis", 13, "vem/log/track",
                        firstMenu.getMenuId());
                menuFacade.savePluginRoute(pluginId, "SQL诊断详情", "SQL Diagnosis Detail", "vem/track_detail", firstMenu.getMenuId());
                menuFacade.savePluginMenu(pluginId, "历史数据诊断", "History Diagnosis", 13, "vem/historyDiagnosis",
                        firstMenu.getMenuId());
                menuFacade.savePluginRoute(pluginId, "历史数据诊断详情", "History Diagnosis Detail", "vem/diagnosisTaskDetail", firstMenu.getMenuId());
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