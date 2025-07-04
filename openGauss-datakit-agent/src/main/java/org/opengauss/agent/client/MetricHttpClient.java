/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 *
 *   openGauss is licensed under Mulan PSL v2.
 *   You can use this software according to the terms and conditions of the Mulan PSL v2.
 *   You may obtain a copy of Mulan PSL v2 at:
 *
 *   http://license.coscl.org.cn/MulanPSL2
 *
 *   THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *   EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *   MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *   See the Mulan PSL v2 for more details.
 */

package org.opengauss.agent.client;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

import org.opengauss.agent.entity.Metric;

import java.util.List;
import java.util.Map;

/**
 * MetricHttpClient
 *
 * @author: wangchao
 * @Date: 2025/5/16 11:41
 * @Description: MetricHttpClient
 * @since 7.0.0-RC2
 **/
public interface MetricHttpClient {
    /**
     * send metrics
     *
     * @param path url path template, e.g. /metrics/collect
     * @param taskIds task id list
     * @param agentId agent id
     * @param metricsPayload metrics payload
     * @return void
     */
    @POST("{path}")
    Call<Void> sendMetrics(@Path(value = "path", encoded = true) String path, @Query("taskIds") List<Long> taskIds,
        @Query("agentId") Long agentId, @Body List<Metric> metricsPayload);

    /**
     * send data metrics
     *
     * @param path url path template, e.g. /metrics/collect
     * @param taskId task id
     * @param agentId agent id
     * @param dataList metrics payload
     * @return void
     */
    @POST("{path}")
    Call<Void> sendDataMetrics(@Path(value = "path", encoded = true) String path, @Query("taskId") Long taskId,
        @Query("agentId") Long agentId, @Body List<Map<String, Object>> dataList);
}
