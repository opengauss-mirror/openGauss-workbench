/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 *
 * -------------------------------------------------------------------------
 *
 * PermitAllPaths.java
 *
 * IDENTIFICATION
 * visualtool-framework/src/main/java/org/opengauss/admin/framework/security/constant/PermitAllPaths.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.framework.security.constant;

/**
 * Centralized registry of anonymous-access (permitAll) paths.
 *
 * <p>Rules:
 * <ul>
 * <li>Every entry MUST carry a reason comment.</li>
 * <li>Whole-plugin wildcards such as "/plugins/xxx/**" are FORBIDDEN,
 * enumerate exact endpoints instead.</li>
 * <li>Cross-segment "**" is only allowed for paths that cannot be enumerated
 * and are explicitly documented with a follow-up hardening TODO.</li>
 * </ul>
 *
 * @author wangchao
 * @date 2026/11/20
 * @since 7.0.0-RC2
 */
public final class PermitAllPaths {
    /**
     * login / register / captcha / public key endpoints
     */
    public static final String[] AUTH = {
        "/login",
        "/register",
        "/captchaImage",
        "/pubKey"
    };

    /**
     * static resources (mapped with HttpMethod.GET)
     * <ul>
     * <li>/fonts/** : web fonts used by the login page (hashed filenames, no business data)</li>
     * </ul>
     */
    public static final String[] STATIC_RESOURCES = {
        "/",
        "/*.html",
        "/*/*.html",
        "/*/*.css",
        "/*/*.js",
        "/img/**",
        "/fonts/**",
        "/profile/**"
    };

    /**
     * misc public endpoints
     * <ul>
     * <li>/*.ico favicon</li>
     * <li>/common/download** file download links opened via window.open (no auth header possible)</li>
     * </ul>
     */
    public static final String[] MISC = {
        "/*.ico",
        "/common/download**",
        "/common/download/resource**"
    };

    /**
     * websocket handshake: progress push channels use dynamic business ids,
     * TODO(T7): enforce token in handshake query then tighten to single-segment "/websocket/*"
     */
    public static final String[] WEBSOCKET = {
        "/websocket/**",
        "/ws/**"
    };

    /**
     * plugin endpoints, enumerated explicitly:
     * <ul>
     * <li>/static-plugin/** : plugin static files only, no plugin API is mounted here</li>
     * <li>oauth-login : exact OAuth flow endpoints (authorize/token/health)</li>
     * <li>alert-monitor : alert report endpoint called by deployed alert agents</li>
     * <li>sql-diagnosis : open API called by the deployed diagnosis agent,
     * TODO(T7): add agent credential check then tighten</li>
     * </ul>
     */
    public static final String[] PLUGIN = {
        "/static-plugin/**",
        "/plugins/oauth-login/oauth/authorize",
        "/plugins/oauth-login/oauth/token",
        "/plugins/oauth-login/oauth/health",
        "/plugins/alert-monitor/api/v1/alerts",
        "/plugins/observability-sql-diagnosis/sqlDiagnosis/api/open/v1/diagnosisTasks/**"
    };

    /**
     * public service endpoints:
     * <ul>
     * <li>/agent/**, /receive/** : data report endpoints for deployed monitoring agents
     * (non-interactive clients), TODO(T7): tighten to exact sub paths + agent credential</li>
     * <li>/prometheus : metrics endpoint scraped by prometheus server</li>
     * <li>/system/plugins : plugin list used by the login page</li>
     * <li>/modeling/visualization/report/share/** : shared report links with dynamic share tokens</li>
     * </ul>
     */
    public static final String[] PUBLIC_SERVICE = {
        "/webjars/*",
        "/agent/**",
        "/receive/**",
        "/prometheus",
        "/system/plugins",
        "/modeling/visualization/report/share/**"
    };

    private PermitAllPaths() {
    }
}
