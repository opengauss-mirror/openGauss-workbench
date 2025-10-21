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

import org.eclipse.microprofile.config.ConfigProvider;
import org.opengauss.agent.constant.AgentConstants;

import java.net.URI;
import java.util.Locale;

/**
 * ServerUrlBuilder
 *
 * @author: wangchao
 * @Date: 2025/10/21 11:02
 * @since 7.0.0-RC2
 **/
public class ServerUrlBuilder {
    /**
     * parse agent server protocol
     *
     * @return protocol
     */
    public static String getAgentServerProtocol() {
        String server = getAgentServerUrl();
        return server.split("://")[0];
    }

    private static String getAgentServerUrl() {
        return ConfigProvider.getConfig().getValue(AgentConstants.ConfigProperties.AGENT_SERVER, String.class);
    }

    /**
     * get agent server base uri
     *
     * @return uri
     */
    public static URI buildBaseUri() {
        String server = getAgentServerUrl();
        return URI.create(server);
    }

    /**
     * get agent endpoint uri
     *
     * @param path interface path
     * @return path uri
     */
    public static URI buildEndpointUri(String path) {
        String server = getAgentServerUrl();
        return URI.create(String.format(Locale.getDefault(), "%s/%s", server, path));
    }
}
