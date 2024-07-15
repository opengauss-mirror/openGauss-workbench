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
 * RefreshTokenMonitor.java
 *
 * IDENTIFICATION
 * oauth-login/src/main/java/org/opengauss/admin/plugin/monitor/RefreshTokenMonitor.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.monitor;

import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.plugin.constants.MyConstants;
import org.opengauss.admin.plugin.service.oauth.OauthLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ScheduledFuture;

/**
 * @date 2024/6/12 20:47
 * @since 0.0
 */
@Slf4j
@Component
public class RefreshTokenMonitor {
    @Autowired
    private TaskScheduler taskScheduler;

    @Autowired
    private Set<String> tokenCacheSet;

    @Autowired
    private OauthLoginService oauthLoginService;

    private ScheduledFuture<?> scheduledFuture;

    /**
     * monitor for refresh access token
     */
    public void monitorCacheKey() {
        log.info("Oauth refresh token monitor is running.");
        for (String token : tokenCacheSet) {
            oauthLoginService.refreshToken(token);
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
            // The task is triggered as a Cron expression, in this case every 30 seconds.
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
}
