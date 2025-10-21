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

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.opengauss.agent.entity.HeartbeatReport;

/**
 * HeartbeatServerClient
 *
 * @author: wangchao
 * @Date: 2025/2/27 12:25
 * @Description: HeartbeatServerClient
 * @since 7.0.0-RC2
 **/
@Path("/agent")
@RegisterRestClient
public interface HeartbeatServerClient {
    /**
     * heartbeat
     *
     * @param customHeader custom header
     * @param heart heartbeat report
     */
    @POST
    @Path("/heartbeat")
    @Produces(MediaType.APPLICATION_JSON)
    void heartbeat(@HeaderParam("X-Custom-Header") String customHeader, HeartbeatReport heart);

    /**
     * server deregister
     *
     * @param customHeader custom header
     * @param requestDown heartbeat report
     */
    @POST
    @Path("/deregister")
    void deregister(@HeaderParam("X-Custom-Header") String customHeader, HeartbeatReport requestDown);
}
