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
 * SecurityConfig.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-framework/src/main/java/org/opengauss/admin/framework/config/SecurityConfig.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.framework.config;

import org.opengauss.admin.framework.security.constant.PermitAllPaths;
import org.opengauss.admin.framework.security.filter.JwtAuthenticationTokenFilter;
import org.opengauss.admin.framework.security.handle.AuthenticationEntryPointImpl;
import org.opengauss.admin.framework.security.handle.LogoutSuccessHandlerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.filter.CorsFilter;

/**
 * spring security configuration
 *
 * @author xielibo
 */
@EnableWebSecurity
@Configuration
public class SecurityConfig {
    /**
     * Authentication failure Handler
     */
    @Autowired
    private AuthenticationEntryPointImpl unauthorizedHandler;

    /**
     * Logout Handler
     */
    @Autowired
    private LogoutSuccessHandlerImpl logoutSuccessHandler;

    /**
     * Token Authentication Filter
     */
    @Autowired
    private JwtAuthenticationTokenFilter authenticationTokenFilter;

    /**
     * CORS Filter
     */
    @Autowired
    private CorsFilter corsFilter;

    /**
     * Autowrite AuthenticationManager
     *
     * @return
     * @throws Exception
     */
    @Bean
    public AuthenticationManager authenticationManager(BCryptPasswordEncoder bCryptPasswordEncoder,
        UserDetailsService userDetailService) {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userDetailService);
        authenticationProvider.setPasswordEncoder(bCryptPasswordEncoder);
        return new ProviderManager(authenticationProvider);
    }

    /**
     * filter chain
     *
     * @param httpSecurity httpSecurity
     * @return SecurityFilterChain
     * @throws Exception Exception
     */
    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
            // 配置注销
            .logout(logout -> logout.logoutUrl("/logout").logoutSuccessHandler(logoutSuccessHandler))
            // 添加过滤器（CORS 只注册一次，置于注销过滤器之前）
            .addFilterBefore(corsFilter, LogoutFilter.class)
            .addFilterBefore(authenticationTokenFilter, UsernamePasswordAuthenticationFilter.class)
            // 禁用 CSRF
            .csrf(AbstractHttpConfigurer::disable)
            // 异常处理
            .exceptionHandling(exceptionHandling -> exceptionHandling.authenticationEntryPoint(unauthorizedHandler))
            // 无状态会话
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 授权配置：白名单集中定义于 PermitAllPaths，禁止整插件 "**" 放行
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(PermitAllPaths.AUTH)
                .permitAll()
                .requestMatchers(HttpMethod.GET, PermitAllPaths.STATIC_RESOURCES)
                .permitAll()
                .requestMatchers(PermitAllPaths.MISC)
                .permitAll()
                .requestMatchers(PermitAllPaths.WEBSOCKET)
                .permitAll()
                .requestMatchers(PermitAllPaths.PLUGIN)
                .permitAll()
                .requestMatchers(PermitAllPaths.PUBLIC_SERVICE)
                .permitAll()
                // 其他请求需要认证
                .anyRequest()
                .authenticated())
            // 同源 iframe 允许嵌入，阻止跨站点击劫持
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin())
                .referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN)));
        return httpSecurity.build();
    }

    /**
     * Hash Encryption Implementation
     */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
