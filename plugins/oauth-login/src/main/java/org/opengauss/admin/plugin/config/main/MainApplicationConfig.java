/*
 * Copyright (c) 2024 Huawei Technologies Co.,Ltd.
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
 * MainApplicationConfig.java
 *
 * IDENTIFICATION
 * oauth-login/src/main/java/org/opengauss/admin/plugin/config/main/MainApplicationConfig.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.config.main;

import com.gitee.starblues.spring.MainApplicationContext;
import com.gitee.starblues.spring.environment.EnvironmentProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @date 2024/6/14 9:30
 * @since 0.0
 */
@Component
public class MainApplicationConfig {
    @Autowired
    private ConfigurableListableBeanFactory configurableListableBeanFactory;

    /**
     * create main environment provider bean
     *
     * @return EnvironmentProvider main environment provider
     */
    @Bean
    public EnvironmentProvider getAppApplicationContext() {
        MainApplicationContext mainApplicationContext =
                (MainApplicationContext) configurableListableBeanFactory.getBean("mainApplicationContext");
        return mainApplicationContext.getEnvironmentProvider();
    }
}
