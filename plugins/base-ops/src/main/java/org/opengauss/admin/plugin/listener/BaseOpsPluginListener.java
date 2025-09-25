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
 * BaseOpsPluginListener.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/listener/BaseOpsPluginListener.java
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
import org.springframework.stereotype.Component;

/**
 * @className: ImplListener
 * @author: xielibo
 * @date: 2022/08/16 3:48 PM
 **/
@Slf4j
@Component
public class BaseOpsPluginListener implements ApplicationListener<ApplicationEvent> {
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationEnvironmentPreparedEvent) {
            log.info("base ops init env");
        } else if (event instanceof ApplicationPreparedEvent) {
            log.info("base ops init complete");
        } else if (event instanceof ContextRefreshedEvent) {
            log.info("base ops was refreshed");
        } else if (event instanceof ApplicationReadyEvent) {
            MainApplicationContext context = ((ApplicationReadyEvent) event).getApplicationContext().getBean(MainApplicationContext.class);
            SpringBeanFactory factory = context.getSpringBeanFactory();
            MenuFacade menuFacade = factory.getBean(MenuFacade.class);
            if (menuFacade != null) {
                String pluginId = "base-ops";

                MenuVo resourceParent = menuFacade.savePluginMenu(pluginId, "资源中心", "Resources", 1, "resource");
                menuFacade.savePluginMenu(pluginId, "集群管理", "cluster ops", 3, "monitor/dailyOps", resourceParent.getMenuId());
                menuFacade.savePluginMenu(pluginId, "OpenLooKeng管理", "openLooKeng manage", 4, "ops/olk", resourceParent.getMenuId());
                MenuVo monitorParent = menuFacade.savePluginMenu(pluginId, "基础运维", "monitor", 3, "monitor");
                menuFacade.savePluginMenu(pluginId, "集群监控", "cluster moniter", 1, "monitor/basic", monitorParent.getMenuId());
                menuFacade.savePluginMenu(pluginId, "日志分析", "log analysis", 2, "monitor/logCenter", monitorParent.getMenuId());
                menuFacade.savePluginMenu(pluginId, "WDR报告", "WDR report", 3, "monitor/wdr", monitorParent.getMenuId());
                menuFacade.savePluginMenu(pluginId, "SQL诊断", "sql analysis", 4, "monitor/slowSql", monitorParent.getMenuId());
                menuFacade.savePluginMenu(pluginId, "自定义控制台", "custom control", 6, "monitor/customControl", monitorParent.getMenuId());
                menuFacade.savePluginMenu(pluginId, "备份恢复", "backup recovery", 7, "ops/backup", monitorParent.getMenuId());

                MenuVo modelingParent = menuFacade.savePluginMenu(pluginId, "业务建模", "business modeling", 2, "modeling");
                menuFacade.savePluginMenu(pluginId, "模型设计", "model design", 1, "modeling/modelDesign", modelingParent.getMenuId());
                menuFacade.savePluginMenu(pluginId, "报表设计", "data flow design", 2, "modeling/dataflow", modelingParent.getMenuId());
                menuFacade.savePluginRoute(pluginId, "数据流详情", "modeling/dataflow/detail", modelingParent.getMenuId());

                MenuVo opsInstallParent = menuFacade.savePluginMenu(pluginId, "安装部署", "install", 1, "ops");
                menuFacade.savePluginMenu(pluginId, "一键安装", "quick install", 1, "ops/simpleInstall", opsInstallParent.getMenuId());
                menuFacade.savePluginMenu(pluginId, "集群安装", "cluster install", 2, "ops/install", opsInstallParent.getMenuId());
                menuFacade.savePluginMenu(pluginId, "批量安装", "cluster batch upgrade", 3, "ops/batchInstall", opsInstallParent.getMenuId());
                menuFacade.savePluginMenu(pluginId, "批量升级", "cluster upgrade", 4, "ops/upgrade", opsInstallParent.getMenuId());
            }
            log.info("base ops start complete");
        } else if (event instanceof ContextClosedEvent) {
            MainApplicationContext context = ((ContextClosedEvent) event).getApplicationContext().getBean(MainApplicationContext.class);
            SpringBeanFactory factory = context.getSpringBeanFactory();
            MenuFacade menuFacade = factory.getBean(MenuFacade.class);
            if (menuFacade != null) {
                menuFacade.deletePluginMenu("base-ops");
            }
            log.info("base ops is stopped");
        }
    }
}
