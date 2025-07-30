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

import org.opengauss.admin.common.core.domain.entity.agent.AgentInstallEntity;
import org.opengauss.admin.common.exception.ops.AgentTaskException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Locale;

import javax.annotation.Resource;

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

    @Resource
    private WebClient webClient;

    /**
     * fetch agent public key
     *
     * @param agent agent
     */
    public void fetchAgentPubKey(AgentInstallEntity agent) {
        String url = String.format(Locale.getDefault(), AGENT_PUB_KEY_URL, agent.getAgentIp(), agent.getAgentPort());
        webClient.get()
            .uri(url)
            .accept(MediaType.TEXT_PLAIN)
            .retrieve()
            .onStatus(HttpStatus::isError, response -> handleHttpError(response, agent))
            .bodyToMono(String.class)
            .timeout(Duration.ofSeconds(2))
            .doOnNext(agent::setPublicKey)
            .doOnError(error -> log.error("Failed to fetch public key from {}:{} - {}", agent.getAgentIp(),
                agent.getAgentPort(), error.getMessage()))
            .then()
            .block();
    }

    private Mono<Throwable> handleHttpError(ClientResponse response, AgentInstallEntity agent) {
        return response.bodyToMono(String.class).defaultIfEmpty("No error details").flatMap(body -> {
            String errorMsg = String.format(Locale.getDefault(), "HTTP %d from %s:%d - %s",
                response.statusCode().value(), agent.getAgentIp(), agent.getAgentPort(), body);
            log.error(errorMsg);
            return Mono.error(new AgentTaskException(errorMsg));
        });
    }
}
