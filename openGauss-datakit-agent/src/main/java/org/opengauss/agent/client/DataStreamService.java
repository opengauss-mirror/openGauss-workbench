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

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.opengauss.agent.entity.ProcessedData;

import java.util.List;

/**
 * DataStreamService
 *
 * @author: wangchao
 * @Date: 2025/2/28 09:26
 * @Description: DataStreamService
 * @since 7.0.0-RC2
 **/
@Path("/agent")
@RegisterRestClient
public interface DataStreamService {
    /**
     * push data to downstream service
     *
     * @param data data
     */
    @POST
    @Path("/data")
    @Produces(MediaType.APPLICATION_JSON)
    void pushData(ProcessedData data);

    /**
     * batch push data to downstream service
     *
     * @param dataList data list
     */
    @POST
    @Path("/batch/data")
    @Produces(MediaType.APPLICATION_JSON)
    void batchPushData(List<ProcessedData> dataList);
}
