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
 *  DevelopBootApplication.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/main/java/com/nctigba/observability/instance/DevelopBootApplication.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance;

import com.gitee.starblues.bootstrap.SpringPluginBootstrap;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.gitee.starblues.spring.extract.DefaultOpExtractFactory;
import com.gitee.starblues.spring.extract.ExtractFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.opengauss.admin.common.core.handler.ops.cache.WsConnectorManager;
import org.opengauss.admin.common.utils.ops.JschUtil;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.plugin.facade.HostUserFacade;
import org.opengauss.admin.system.plugin.facade.WsFacade;
import org.opengauss.admin.system.service.IWebSocketService;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Profile;

/**
 * Run for develop
 *
 * @since 2023/12/1
 */
@Profile({"dev"})
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        RedisAutoConfiguration.class,
        RedisRepositoriesAutoConfiguration.class,
        SecurityAutoConfiguration.class
})
@EnableCaching
@EnableAspectJAutoProxy
@ComponentScan(basePackages = {"com.nctigba.observability.instance",
        "org.opengauss.admin.system.service.ops",
        "org.opengauss.admin.common.utils.ops",
        "org.opengauss.admin.common.core.handler.ops.cache"},
        basePackageClasses = {HostFacade.class, HostUserFacade.class, JschUtil.class, WsFacade.class,
                WsConnectorManager.class, IWebSocketService.class},
        excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes =
                {org.opengauss.admin.system.plugin.facade.MenuFacade.class,
                        org.opengauss.admin.system.plugin.facade.MonitorToolsFacade.class,
                        org.opengauss.admin.system.plugin.facade.PluginFacade.class,
                        org.opengauss.admin.system.plugin.facade.SysSettingFacade.class,
                        org.opengauss.admin.system.plugin.facade.TaskFacade.class})})
@MapperScan({"com.nctigba.observability.instance.mapper", "org.opengauss.admin.system.mapper"})
public class DevelopBootApplication extends SpringPluginBootstrap {
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    EncryptionUtils encryptionUtils;

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(DevelopBootApplication.class);
        app.setAdditionalProfiles("dev");
        app.run(args);
    }

    /**
     * ExtractFactory bean
     *
     * @return com.gitee.starblues.spring.extract.ExtractFactory
     * @since 2023/12/1
     */
    @Bean
    public ExtractFactory extractFactory() {
        return new DefaultOpExtractFactory();
    }

    /**
     * Init encryption key
     *
     * @since 2023/12/1
     */
    @Bean
    public void initForDev() {
        encryptionUtils.getKey();
    }
}
