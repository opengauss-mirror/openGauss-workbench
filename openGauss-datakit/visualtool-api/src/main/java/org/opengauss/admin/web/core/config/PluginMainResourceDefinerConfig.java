/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
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
 * PluginMainResourceDefinerConfig.java
 *
 * IDENTIFICATION
 * visualtool-api/src/main/java/org/opengauss/admin/web/core/config/PluginMainResourceDefinerConfig.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.web.core.config;

import com.gitee.starblues.core.classloader.MainResourcePatternDefiner;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Plugin main resource pattern config
 *
 * @author openGauss
 * @since 7.0.0
 */
@Configuration
public class PluginMainResourceDefinerConfig {
    /**
     * Allow plugins to load mybatis and commons-pool2 classes from the main application.
     * The plugin classloader drops its cached class bytes after plugin startup and cannot
     * lazily load these classes from the bundled jars afterwards, so the classes must be
     * resolvable from the main application classloader. The main application and the
     * plugins use the same versions (mybatis 3.5.19, commons-pool2 2.11.1).
     * Note: spring-coupled libraries such as mybatis-plus (com/baomidou) must NOT be
     * added here, because their classes would be mixed between the main application and
     * the plugin and break plugin startup with ClassCastException-like assignability
     * errors (e.g. SafetyEncryptProcessor vs EnvironmentPostProcessor).
     *
     * @return the main resource pattern definer
     */
    @Bean
    public MainResourcePatternDefiner pluginCommonLibMainResourceDefiner() {
        return new MainResourcePatternDefiner() {
            @Override
            public Set<String> getIncludePatterns() {
                Set<String> patterns = new HashSet<>();
                patterns.add("org/apache/ibatis/**");
                patterns.add("org/apache/commons/pool2/**");
                return patterns;
            }

            @Override
            public Set<String> getExcludePatterns() {
                return Collections.emptySet();
            }
        };
    }
}
