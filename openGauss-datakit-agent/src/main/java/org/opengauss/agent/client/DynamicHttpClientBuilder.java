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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import org.opengauss.agent.config.AgentSslContext;
import org.opengauss.agent.constant.AgentConstants;
import org.opengauss.agent.exception.AgentException;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

/**
 * DynamicHttpClientBuilder
 *
 * @author: wangchao
 * @Date: 2025/5/22 10:33
 * @since 7.0.0-RC2
 **/
public class DynamicHttpClientBuilder {
    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .setTimeZone(TimeZone.getTimeZone("UTC")) // 全局时区
        .setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"))
        .registerModule(new SimpleModule().addSerializer(Integer.class, new ToStringSerializer()) // 将Integer序列化为字符串
            .addSerializer(Integer.TYPE, new ToStringSerializer()) // 处理基本类型 Integer
            .addSerializer(Long.class, new ToStringSerializer()) // 将Long序列化为字符串
            .addSerializer(Long.TYPE, new ToStringSerializer())); // 处理基本类型long

    /**
     * createHttpClient
     *
     * @param baseUrl baseUrl
     * @return MetricHttpClient
     */
    public static MetricHttpClient createHttpClient(String baseUrl) {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(formatBaseUrl(baseUrl))
            .client(defaultClient())
            .addConverterFactory(JacksonConverterFactory.create(MAPPER))
            .build();
        return retrofit.create(MetricHttpClient.class);
    }

    private static String formatBaseUrl(String rawUrl) {
        return rawUrl.endsWith("/") ? rawUrl : rawUrl + "/";
    }

    private static OkHttpClient defaultClient() {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .connectionPool(new ConnectionPool(5, 5, TimeUnit.MINUTES));
        String protocol = ServerUrlBuilder.getAgentServerProtocol();
        if (AgentConstants.ConfigProperties.HTTPS.equalsIgnoreCase(protocol)) {
            configureTrustAll(clientBuilder);
        }
        return clientBuilder.build();
    }

    private static void configureTrustAll(OkHttpClient.Builder clientBuilder) {
        try {
            SSLContext sslContext = AgentSslContext.configureSslContext();
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            clientBuilder.sslSocketFactory(sslSocketFactory, AgentSslContext.getX509TrustManager());
            clientBuilder.hostnameVerifier((hostname, session) -> true);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new AgentException("Failed to configure unsafe SSL", e);
        }
    }
}
