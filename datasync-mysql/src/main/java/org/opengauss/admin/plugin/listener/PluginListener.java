package org.opengauss.admin.plugin.listener;

import com.gitee.starblues.spring.MainApplicationContext;
import com.gitee.starblues.spring.SpringBeanFactory;
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
public class PluginListener implements ApplicationListener<ApplicationEvent> {

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationEnvironmentPreparedEvent) {
            System.out.println("mysql sync-tool init env");
        } else if (event instanceof ApplicationPreparedEvent) {
            System.out.println("mysql sync-tool init complete");
        } else if (event instanceof ContextRefreshedEvent) {
            System.out.println("mysql sync-tool was refreshed");
        } else if (event instanceof ApplicationReadyEvent) {
            MainApplicationContext context = ((ApplicationReadyEvent) event).getApplicationContext().getBean(MainApplicationContext.class);
            SpringBeanFactory factory = context.getSpringBeanFactory();
            MenuFacade menuFacade = factory.getBean(MenuFacade.class);
            if (menuFacade != null) {
                MenuVo syncParent = menuFacade.savePluginMenu("datasync-mysql", "迁移工具", "sync tool", 5, "sync");
                menuFacade.savePluginMenu("datasync-mysql","Mysql数据迁移","mysql migration",1, "index",syncParent.getMenuId());
            }
            System.out.println("mysql sync-tool start complete");
        } else if (event instanceof ContextClosedEvent) {
            MainApplicationContext context = ((ContextClosedEvent) event).getApplicationContext().getBean(MainApplicationContext.class);
            SpringBeanFactory factory = context.getSpringBeanFactory();
            MenuFacade menuFacade = factory.getBean(MenuFacade.class);
            if (menuFacade != null) {
                menuFacade.deletePluginMenu("datasync-mysql");
            }
            System.out.println("mysql sync-tool is stopped");
        }
    }
}
