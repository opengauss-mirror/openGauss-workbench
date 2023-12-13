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
 *  ObservabilityPluginBootApplication.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/ObservabilityPluginBootApplication.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor;

import org.mybatis.spring.annotation.MapperScan;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.plugin.facade.HostUserFacade;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @Author wuyuebin
 * @Date 2023/4/7 16:36
 * @Description
 */
@Profile({"dev", "gba"})
@EnableScheduling
@EnableAsync
@SpringBootApplication(exclude = {
        HibernateJpaAutoConfiguration.class,
        RedisAutoConfiguration.class,
        RedisRepositoriesAutoConfiguration.class})
@ComponentScan(basePackages = {"com.nctigba.alert.monitor",
        "org.opengauss.admin.system.service.ops",
        "org.opengauss.admin.common.utils.ops",
        "org.opengauss.admin.common.core.handler.ops.cache"},
        basePackageClasses = {HostFacade.class,
                HostUserFacade.class},
        excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes =
                {org.opengauss.admin.system.plugin.facade.MenuFacade.class,
                        org.opengauss.admin.system.plugin.facade.MonitorToolsFacade.class,
                        org.opengauss.admin.system.plugin.facade.PluginFacade.class,
                        org.opengauss.admin.system.plugin.facade.SysSettingFacade.class,
                        org.opengauss.admin.system.plugin.facade.TaskFacade.class,
                        org.opengauss.admin.system.plugin.facade.WsFacade.class})})
@MapperScan({"com.nctigba.alert.monitor.mapper", "org.opengauss.admin.system.mapper"})
public class ObservabilityPluginBootApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ObservabilityPluginBootApplication.class);
        app.setAdditionalProfiles("dev");
        app.run(args);
    }
}
