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

package org.opengauss.admin.system.service.agent.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengauss.admin.common.core.domain.model.agent.HeartbeatReport;
import org.opengauss.agent.event.AgentStatusEvent;
import org.opengauss.agent.impl.AgentHeartbeatServiceImpl;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * AgentHeartbeatServiceImplTest
 *
 * @author: wangchao
 * @Date: 2025/4/15 11:45
 * @Description: AgentHeartbeatServiceImplTest
 * @since 7.0.0-RC2
 **/
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(MockitoExtension.class)
class AgentHeartbeatServiceImplTest {
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AgentHeartbeatServiceImpl service = new AgentHeartbeatServiceImpl();

    @BeforeEach
    void start() {
        service.startHeartbeatService();
    }

    // 心跳接收逻辑测试
    @DisplayName("心跳接收逻辑测试 1")
    @Test
    void whenReceiveFirstHeartbeat_thenRegisterNewAgent() {
        String agentId = "101";
        // 首次心跳
        HeartbeatReport report = new HeartbeatReport(agentId, Instant.now());
        service.receiveHeartbeat(report);
        assertTrue(service.isAgentOnline(agentId));
    }

    @DisplayName("心跳接收逻辑测试 2")
    @Test
    void whenReceiveExpiredHeartbeat_thenMarkOffline() {
        String agentId = "102";
        // 过期时间戳（假设超时阈值30秒）
        HeartbeatReport report = new HeartbeatReport(agentId, Instant.now().minusSeconds(31));
        service.receiveHeartbeat(report);
        service.checkAgentsStatus();
        assertFalse(service.isAgentOnline(agentId));
    }

    @DisplayName("心跳接收逻辑测试 3")
    @Test
    void whenStatusChanged_thenPublishEvent() {
        String agentId = "103";
        // 初始在线状态
        service.receiveHeartbeat(new HeartbeatReport(agentId, Instant.now()));
        // 触发离线
        service.receiveHeartbeat(new HeartbeatReport(agentId, Instant.now().minusSeconds(35)));
        ArgumentCaptor<AgentStatusEvent> captor = ArgumentCaptor.forClass(AgentStatusEvent.class);
        verify(eventPublisher, times(2)).publishEvent(captor.capture());
        List<AgentStatusEvent> events = captor.getAllValues();
        assertEquals(true, events.get(0).isOnline());  // 初始上线事件
        assertEquals(false, events.get(1).isOnline()); // 后续离线事件
    }

    @DisplayName("状态检查任务测试1")
    @Test
    void whenCheckStatus_thenUpdateOnlineState() throws InterruptedException {
        String agentId = "104";
        // 注册过期Agent
        service.receiveHeartbeat(new HeartbeatReport(agentId, Instant.now().minusSeconds(31)));
        // 手动触发状态检查
        service.checkAgentsStatus();
        assertFalse(service.isAgentOnline(agentId));
    }

    @DisplayName("边界条件测试1")
    @Test
    void whenQueryNonExistAgent_thenReturnOffline() {
        String agentId = "105";
        assertFalse(service.isAgentOnline(agentId));
    }

    @DisplayName("边界条件测试2")
    @Test
    void whenConcurrentUpdates_thenMaintainConsistency() throws Exception {
        String agentId = "106";
        ExecutorService pool = Executors.newFixedThreadPool(10);
        IntStream.range(0, 100)
            .forEach(i -> pool.submit(() -> service.receiveHeartbeat(new HeartbeatReport(agentId, Instant.now()))));
        pool.shutdown();
        assertTrue(pool.awaitTermination(3, TimeUnit.SECONDS));
        assertTrue(service.isAgentOnline(agentId));
    }

    @DisplayName("事件验证：通过ArgumentCaptor捕获事件细节")
    @Test
    void verifyEventPayloadDetails() {
        String agentId = "107";
        service.receiveHeartbeat(new HeartbeatReport(agentId, Instant.now()));
        ArgumentCaptor<AgentStatusEvent> captor = ArgumentCaptor.forClass(AgentStatusEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        AgentStatusEvent event = captor.getValue();
        assertEquals("agent-07", event.getAgentId());
        assertEquals(true, event.isOnline());
    }
}