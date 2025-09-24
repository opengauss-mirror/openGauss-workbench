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

package org.opengauss.agent.service;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import org.opengauss.admin.common.core.domain.entity.agent.AgentInstallEntity;
import org.opengauss.admin.common.exception.ops.AgentConnectionException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.TimeoutException;

import jakarta.annotation.Resource;

/**
 * AgentHttpProxy
 *
 * @author: wangchao
 * @Date: 2025/7/28 22:24
 * @since 7.0.0-RC2
 **/
@Slf4j
@Service
public class AgentHttpProxy {
    private static final String AGENT_PUB_KEY_URL = "http://%s:%d/agent/pubKey";
    private static final String AGENT_HEALTH_URL = "http://%s:%d/agent/health";

    @Resource
    private WebClient webClient;
    private final Retry retryPolicy = Retry.backoff(3, Duration.ofSeconds(1))
        .filter(
            throwable -> throwable instanceof TimeoutException || throwable instanceof WebClientRequestException || (
                throwable instanceof WebClientResponseException ex && ex.getStatusCode().is5xxServerError()))
        .onRetryExhaustedThrow(
            (retryBackoffSpec, retrySignal) -> new AgentConnectionException("agent connection failed",
                retrySignal.failure()));

    /**
     * fetch agent public key
     *
     * @param agent agent
     */
    public void fetchAgentPubKey(AgentInstallEntity agent) {
        String url = String.format(Locale.getDefault(), AGENT_PUB_KEY_URL, agent.getAgentIp(), agent.getAgentPort());
        webClient.get()
            .uri(url)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus(HttpStatusCode::isError, response -> handleHealthHttpError(response, agent))
            .bodyToMono(String.class)
            .timeout(Duration.ofSeconds(2))
            .retryWhen(retryPolicy)
            .doOnNext(agent::setPublicKey)
            .doOnError(error -> log.error("Failed to fetch public key from {}:{} - {}", agent.getAgentIp(),
                agent.getAgentPort(), error.getMessage()))
            .then()
            .block();
    }

    /**
     * check agent health
     *
     * @param agent agent install
     * @return health status
     */
    public String checkAgentHealth(AgentInstallEntity agent) {
        if (agent == null) {
            throw new IllegalArgumentException("Agent entity cannot be null");
        }
        String url = String.format(Locale.getDefault(), AGENT_HEALTH_URL, agent.getAgentIp(), agent.getAgentPort());
        return webClient.get()
            .uri(url)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus(HttpStatusCode::isError, response -> handleHealthHttpError(response, agent))
            .bodyToMono(String.class)
            .timeout(Duration.ofSeconds(2))
            .retryWhen(retryPolicy)
            .doOnNext(
                response -> log.debug("Agent health check successful for {}:{} - Response: {}", agent.getAgentIp(),
                    agent.getAgentPort(), truncateResponse(response)))
            .doOnError(error -> logHealthError(agent, error))
            .block();
    }

    private String truncateResponse(String response) {
        if (response == null) {
            return "null";
        }
        return response.length() > 100 ? response.substring(0, 100) + "..." : response;
    }

    private void logHealthError(AgentInstallEntity agent, Throwable error) {
        String errorMessage = error.getMessage();
        if (error instanceof AgentConnectionException) {
            errorMessage = error.getMessage();
        }
        log.error("Agent health check failed for {}:{} - Reason: {}", agent.getAgentIp(), agent.getAgentPort(),
            errorMessage);
    }

    private Mono<? extends Throwable> handleHealthHttpError(ClientResponse response, AgentInstallEntity agent) {
        return response.bodyToMono(String.class).defaultIfEmpty("No error details").flatMap(body -> {
            String errorMsg = String.format(Locale.getDefault(), "agent connection failed for %s:%s - HTTP %d: %s",
                agent.getAgentIp(), agent.getInstallUser(), response.statusCode().value(), body);
            return Mono.error(new AgentConnectionException(errorMsg));
        });
    }
}
