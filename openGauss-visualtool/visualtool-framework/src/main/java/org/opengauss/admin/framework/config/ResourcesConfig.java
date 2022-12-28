package org.opengauss.admin.framework.config;

import com.google.common.collect.Lists;
import org.opengauss.admin.common.config.SystemConfig;
import org.opengauss.admin.common.constant.Constants;
import org.opengauss.admin.framework.interceptor.BaseRepeatSubmitInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Web configuration
 *
 * @author xielibo
 */
@Configuration
public class ResourcesConfig implements WebMvcConfigurer {
    @Autowired
    private BaseRepeatSubmitInterceptor repeatSubmitInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(Constants.RESOURCE_PREFIX + "/**").addResourceLocations("file:" + SystemConfig.getStoragePath() + "/");
        registry.addResourceHandler("/web/**").addResourceLocations("classpath:/static/");
    }

    /**
     * Add a custom intercept
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(repeatSubmitInterceptor).addPathPatterns("/**");
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