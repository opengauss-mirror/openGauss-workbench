/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.nctigba.observability.sql.mapper.NctigbaEnvMapper;
import com.nctigba.observability.sql.model.NctigbaEnv;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Component;

/**
 * ElasticsearchProvider
 *
 * @author luomeng
 * @since 2023/6/11
 */
@Component
@EnableCaching
public class ElasticsearchProvider {
    @Autowired
    private NctigbaEnvMapper envMapper;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostFacade hostFacade;

    @Cacheable(cacheNames = "elastic-clients")
    public ElasticsearchClient client() {
        var env = envMapper.selectOne(
                Wrappers.<NctigbaEnv>lambdaQuery().eq(NctigbaEnv::getType, NctigbaEnv.envType.ELASTICSEARCH));
        var host = hostFacade.getById(env.getHostid());
        // Split ip when clustering
        HttpHost[] httpHosts = {new HttpHost(host.getPublicIp(), env.getPort())};
        // Account and password authentication
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("elastic", "changeme"));
        // Create the low-level client
        RestClient restClient = RestClient.builder(httpHosts)
                .setHttpClientConfigCallback(
                        httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider))
                .setRequestConfigCallback(
                        requestConfigBuilder -> requestConfigBuilder.setConnectTimeout(100000).setSocketTimeout(100000))
                .build();

        // Create the transport with a Jackson mapper
        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        // And create the API client
        return new ElasticsearchClient(transport);
    }

    @CacheEvict(cacheNames = "elastic-clients")
    public void clear() {
    }
}
