package com.nctigba.datastudio.listener;

import com.gitee.starblues.spring.MainApplicationContext;
import com.gitee.starblues.spring.SpringBeanFactory;
import org.opengauss.admin.system.plugin.facade.MenuFacade;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;

import static com.nctigba.datastudio.constants.CommonConstants.WEBDS_PLUGIN;

/**
 * @className: ImplListener
 * @description: TODO
 * @author: xielibo
 * @date: 2022年08月16日 3:48 PM
 **/
public class PluginListener implements ApplicationListener<ApplicationEvent> {

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationEnvironmentPreparedEvent) {
            System.out.println("扩展实现初始化环境变量");
        } else if (event instanceof ApplicationPreparedEvent) {
            System.out.println("扩展实现环境初始化完成");
        } else if (event instanceof ContextRefreshedEvent) {
            System.out.println("扩展实现ApplicationContext被刷新");
        } else if (event instanceof ApplicationReadyEvent) {
            MainApplicationContext context = ((ApplicationReadyEvent) event).getApplicationContext()
                    .getBean(MainApplicationContext.class);
            SpringBeanFactory factory = context.getSpringBeanFactory();
            MenuFacade menuFacade = factory.getBean(MenuFacade.class);
            if (menuFacade != null) {
                menuFacade.savePluginMenu(WEBDS_PLUGIN, "业务开发", "DataStudio", 10, "index");
            }
        } else if (event instanceof ContextClosedEvent) {
            MainApplicationContext context = ((ContextClosedEvent) event).getApplicationContext()
                    .getBean(MainApplicationContext.class);
            SpringBeanFactory factory = context.getSpringBeanFactory();
            MenuFacade menuFacade = factory.getBean(MenuFacade.class);
            if (menuFacade != null) {
                menuFacade.deletePluginMenu(WEBDS_PLUGIN);
            }
        }
    }
}
