/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
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
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @Author wuyuebin
 * @Date 2023/4/7 16:36
 * @Description
 */
@Profile({"dev", "gba"})
@EnableScheduling
@SpringBootApplication(exclude = {
        HibernateJpaAutoConfiguration.class,
        RedisAutoConfiguration.class,
        RedisRepositoriesAutoConfiguration.class})
@ComponentScan(basePackages = {"org.opengauss.plugin.alertcenter",
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
@MapperScan({"org.opengauss.plugin.alertcenter.mapper", "org.opengauss.admin.system.mapper"})
public class ObservabilityPluginBootApplication {
    public static void main(String[] args) {
        SpringApplication.run(ObservabilityPluginBootApplication.class, args);
    }
}
