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

package org.opengauss.agent.client;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.opengauss.agent.config.AgentSslContext;
import org.opengauss.agent.constant.AgentConstants;
import org.opengauss.agent.exception.AgentException;

import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

/**
 * ServerClientFactory
 *
 * @author: wangchao
 * @Date: 2025/10/21 11:05
 * @since 7.0.0-RC3
 **/
@ApplicationScoped
public class ServerClientFactory {
    /**
     * create heartbeat client
     *
     * @return HeartbeatServerClient
     */
    public HeartbeatServerClient createHeartbeatClient() {
        return createClient(HeartbeatServerClient.class);
    }

    /**
     * create agent server client
     *
     * @return AgentServerClient
     */
    public AgentServerClient createAgentServerClient() {
        return createClient(AgentServerClient.class);
    }

    /**
     * create HostFixedMetricsClient client
     *
     * @return HostFixedMetricsClient
     */
    public HostFixedMetricsClient createHostFixedMetricsClient() {
        return createClient(HostFixedMetricsClient.class);
    }

    /**
     * create DataStreamService client
     *
     * @return DataStreamService
     */
    public DataStreamService createDataStreamService() {
        return createClient(DataStreamService.class);
    }

    private <T> T createClient(Class<T> clientClass) {
        try {
            URI baseUri = ServerUrlBuilder.buildBaseUri();
            RestClientBuilder builder = RestClientBuilder.newBuilder().baseUri(baseUri);
            String protocol = ServerUrlBuilder.getAgentServerProtocol();
            if (AgentConstants.ConfigProperties.HTTPS.equalsIgnoreCase(protocol)) {
                builder.sslContext(AgentSslContext.configureSslContext()).hostnameVerifier((hostname, session) -> true);
            }
            return builder.build(clientClass);
        } catch (NoSuchAlgorithmException | KeyManagementException ex) {
            throw new AgentException("build agent server client error", ex);
        }
    }
}
