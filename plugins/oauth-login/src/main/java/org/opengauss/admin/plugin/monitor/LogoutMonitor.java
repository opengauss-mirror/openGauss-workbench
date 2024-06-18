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
 * LogoutMonitor.java
 *
 * IDENTIFICATION
 * oauth-login/src/main/java/org/opengauss/admin/plugin/monitor/LogoutMonitor.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.monitor;

import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.model.LoginUser;
import org.opengauss.admin.framework.web.service.TokenService;
import org.opengauss.admin.plugin.constants.MyConstants;
import org.opengauss.admin.plugin.service.oauth.OauthLogoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

/**
 * @date 2024/5/30 10:18
 * @since 0.0
 */
@Slf4j
@Component
public class LogoutMonitor {
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private TokenService tokenService;

    @Autowired
    private Set<String> tokenCacheSet;

    @Autowired
    private TaskScheduler taskScheduler;

    @Autowired
    private OauthLogoutService oauthLogoutService;

    private ScheduledFuture<?> scheduledFuture;

    /**
     * monitor whether the sso mapping user logout
     */
    public void monitorCacheKey() {
        log.info("Oauth logout monitor is running.");
        for (String token : tokenCacheSet) {
            LoginUser loginUser = tokenService.getLoginUser(getHttpServletRequest(token));
            if (loginUser == null) {
                // Example Delete the token of the unified login user.
                tokenCacheSet.remove(token);
                // Logout oauth login.
                oauthLogoutService.logout(token);
            }
        }
        if (tokenCacheSet.isEmpty()) {
            stopTask();
        }
    }

    /**
     * start monitor
     */
    public void startTask() {
        if (scheduledFuture == null || scheduledFuture.isCancelled()) {
            // The task is triggered by a Cron expression, in this case every 30 seconds.
            scheduledFuture = taskScheduler.schedule(this::monitorCacheKey,
                    new CronTrigger("0/" + MyConstants.ACCESS_TOKEN_EXPIRES_TOME + " * * * * *"));
        }
    }

    /**
     * stop monitor
     */
    public void stopTask() {
        if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
            scheduledFuture.cancel(true);
        }
    }

    /**
     * generate httpServletRequest containing token
     *
     * @param token token
     * @return HttpServletRequest
     */
    public HttpServletRequest getHttpServletRequest(String token) {
        // Create an instance of HttpServletRequest.
        HttpServletRequest request = new MockHttpServletRequest();

        return new HttpServletRequestWrapper(request) {
            @Override
            public String getHeader(String name) {
                if ("Authorization".equalsIgnoreCase(name)) {
                    return "Bearer " + token;
                }
                return super.getHeader(name);
            }
        };
    }
}
