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
 * ResourcesConfig.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-framework/src/main/java/org/opengauss/admin/framework/config/ResourcesConfig.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.framework.config;

import com.google.common.collect.Lists;

import jakarta.annotation.Resource;

import org.opengauss.admin.common.config.SystemConfig;
import org.opengauss.admin.common.constant.Constants;
import org.opengauss.admin.framework.interceptor.impl.SameUrlDataInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Web configuration
 *
 * @author xielibo
 */
@Configuration
public class ResourcesConfig implements WebMvcConfigurer {
    @Autowired
    private SameUrlDataInterceptor sameUrlDataInterceptor;

    @Resource
    private PluginProperties pluginProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(Constants.RESOURCE_PREFIX + "/**")
            .addResourceLocations("file:" + SystemConfig.getStoragePath() + "/");
        registry.addResourceHandler("/web/**").addResourceLocations("classpath:/static/");
        String mainPluginPath = pluginProperties.getPluginPath().get(0);
        loadPluginDirectoryJars(registry, mainPluginPath);
    }

    private static void loadPluginDirectoryJars(ResourceHandlerRegistry registry, String mainPluginPath) {
        File pluginDirectory = new File(mainPluginPath);
        if (pluginDirectory.exists() && pluginDirectory.isDirectory()) {
            File[] jars = pluginDirectory.listFiles((dir, name) -> name.endsWith(".jar"));
            if (jars != null) {
                Arrays.stream(jars).forEach(jar -> {
                    String jarPath = "jar:file:" + jar.getAbsolutePath() + "!/classes/resources/";
                    registry.addResourceHandler("/static-plugin/**").addResourceLocations(jarPath);
                });
            }
        }
    }

    /**
     * Add a custom intercept
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(sameUrlDataInterceptor).addPathPatterns("/**");
    }

    /**
     * cors config
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        List<String> patterns = Lists.newArrayList();
        patterns.add("*");
        config.setAllowedOriginPatterns(patterns);
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
