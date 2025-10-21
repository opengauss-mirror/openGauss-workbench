/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
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
 */

package org.opengauss.agent.service.heartbeat;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.net.NetUtil;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import org.opengauss.agent.client.HeartbeatServerClient;
import org.opengauss.agent.client.ServerClientFactory;
import org.opengauss.agent.config.AppConfig;
import org.opengauss.agent.constant.AgentConstants;
import org.opengauss.agent.entity.HeartbeatHeader;
import org.opengauss.agent.entity.HeartbeatReport;

import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * HeartbeatScheduler
 *
 * @author: wangchao
 * @Date: 2025/2/26 18:45
 * @Description: HeartbeatScheduler
 * @since 7.0.0-RC2
 **/
@Slf4j
@ApplicationScoped
public class HeartbeatScheduler {
    private static final String INSTANCE_ID = UUID.randomUUID().toString();
    private static final AtomicInteger HEARTBEAT_BREAK_TIMES = new AtomicInteger(0);

    HeartbeatHeader heartbeatHeader;
    ScheduledExecutorService scheduler;
    @Inject
    AppConfig appConfig;
    @Inject
    ServerClientFactory clientFactory;
    HeartbeatServerClient heartbeatServerClient = null;

    /**
     * onStart
     *
     * @param event startup event
     */
    void onStart(@Observes StartupEvent event) {
        log.info("agent config : {}", appConfig);
        initHeartbeatHeader();
        scheduler = Executors.newSingleThreadScheduledExecutor();
        heartbeatServerClient = clientFactory.createHeartbeatClient();
        scheduler.scheduleAtFixedRate(this::sendHeartbeat, 1, appConfig.getHeartbeatInterval(), TimeUnit.SECONDS);
    }

    /**
     * stop ,release resources
     *
     * @param event shutdown event
     */
    void onStop(@Observes ShutdownEvent event) {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
        HeartbeatReport report = new HeartbeatReport(appConfig.getAgentId(), Instant.now());
        try {
            report.setStatus(AgentConstants.HEARTBEAT_STATUS_DOWN);
            heartbeatServerClient.deregister(heartbeatHeader.toHeartbeatHeader(), report);
            log.info("server deregister");
        } catch (Exception ex) {
            log.warn("Failed to deregister server:" + report);
        }
    }

    private void initHeartbeatHeader() {
        heartbeatHeader = new HeartbeatHeader(appConfig.getAppName(), INSTANCE_ID, NetUtil.getLocalhostStr(),
            appConfig.getAppServerUrl());
    }

    private void sendHeartbeat() {
        HeartbeatReport report = new HeartbeatReport(appConfig.getAgentId(), Instant.now());
        try {
            heartbeatServerClient.heartbeat(heartbeatHeader.toHeartbeatHeader(), report);
            HEARTBEAT_BREAK_TIMES.set(0);
            log.info("agent heartbeat name=[{}] local=[{}] server=[{}]", heartbeatHeader.getAgentName(),
                heartbeatHeader.getAgentAddress(), heartbeatHeader.getTarget());
        } catch (Exception e) {
            if (HEARTBEAT_BREAK_TIMES.get() <= appConfig.getHeartbeatBreakWaitMaxTimes()) {
                HEARTBEAT_BREAK_TIMES.incrementAndGet();
                log.error("Failed to send heartbeat name=[{}] local=[{}] server=[{}] {}",
                    heartbeatHeader.getAgentName(), heartbeatHeader.getAgentAddress(), heartbeatHeader.getTarget(),
                    e.getMessage());
            } else {
                log.error("Heartbeat detection failure count exceeds limit, process will exit soon");
                Quarkus.asyncExit(1); // 优雅关闭
            }
        }
    }
}
