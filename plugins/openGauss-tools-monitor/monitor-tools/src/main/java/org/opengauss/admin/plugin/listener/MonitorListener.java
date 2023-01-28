package org.opengauss.admin.plugin.listener;

import com.gitee.starblues.spring.MainApplicationContext;
import com.gitee.starblues.spring.SpringBeanFactory;
import lombok.extern.slf4j.Slf4j;
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
 * @author liu
 * @since 2022-10-01
 */
@Slf4j
public class MonitorListener implements ApplicationListener<ApplicationEvent> {

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationEnvironmentPreparedEvent) {
            log.info("plugin init env");
        } else if (event instanceof ApplicationPreparedEvent) {
            log.info("plugin init complete");
        } else if (event instanceof ContextRefreshedEvent) {
            log.info("plugin was refreshed");
        } else if (event instanceof ApplicationReadyEvent) {
            MainApplicationContext applicationContext = ((ApplicationReadyEvent) event).getApplicationContext().getBean(MainApplicationContext.class);
            SpringBeanFactory springBeanFactory = applicationContext.getSpringBeanFactory();
            MenuFacade acade = springBeanFactory.getBean(MenuFacade.class);
            if (acade != null) {
                acade.savePluginMenu("monitor-tools","监控插件工具","MonitoringPluginTool", 8,"index");
            }
            log.info("plugin monitor-tools start complete");
        } else if (event instanceof ContextClosedEvent) {
            MainApplicationContext applicationContext = ((ContextClosedEvent) event).getApplicationContext().getBean(MainApplicationContext.class);
            SpringBeanFactory springBeanFactory = applicationContext.getSpringBeanFactory();
            MenuFacade menu = springBeanFactory.getBean(MenuFacade.class);
            if (menu != null) {
                menu.deletePluginMenu("monitor-tools");
            }
            log.info("plugin monitor-tools is stopped");
        }
    }
}
