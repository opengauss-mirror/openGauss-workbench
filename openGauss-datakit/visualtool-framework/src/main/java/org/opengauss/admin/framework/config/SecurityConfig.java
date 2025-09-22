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
            // 添加过滤器
            .addFilterBefore(authenticationTokenFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(corsFilter, JwtAuthenticationTokenFilter.class)
            .addFilterBefore(corsFilter, LogoutFilter.class)
            // 禁用 CSRF
            .csrf(AbstractHttpConfigurer::disable)
            // 异常处理
            .exceptionHandling(exceptionHandling -> exceptionHandling.authenticationEntryPoint(unauthorizedHandler))
            // 无状态会话
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 授权配置（关键修改）
            .authorizeHttpRequests(authorize -> authorize
                // 允许所有用户访问的端点
                .requestMatchers("/login", "/register", "/captchaImage", "/pubKey")
                .permitAll()
                // 静态资源
                .requestMatchers(HttpMethod.GET, "/", "/*.html", "/*/*.html", "/*/*.css", "/*/*.js", "/img/**",
                    "/profile/**")
                .permitAll()
                .requestMatchers("/*.ico", "/notice/send", "/common/download**", "/common/download/resource**")
                .permitAll()
                // WebSocket
                .requestMatchers("/websocket/**", "/ws/**")
                .permitAll()
                // 插件相关  添加需要的静态资源路径（关键）
                .requestMatchers("/static-plugin/**", "/plugins/base-ops/**", "/plugins/oauth-login/oauth/**",
                    "/plugins/alert-monitor/api/v1/alerts",
                    "/plugins/observability-sql-diagnosis/sqlDiagnosis/api/open/v1/diagnosisTasks/**")
                .permitAll()
                // 其他公共端点
                .requestMatchers("/webjars/**", "/agent/**", "/receive/**", "/prometheus",
                    "/modeling/visualization/report/share/**")
                .permitAll()
                // 其他请求需要认证
                .anyRequest()
                .authenticated())
            // 禁用 X-Frame-Options 以便嵌入 iframe
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));
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
