/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.config;

import cn.hutool.core.collection.CollectionUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.nctigba.alert.monitor.entity.NctigbaEnv;
import com.nctigba.alert.monitor.mapper.NctigbaEnvMapper;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.exception.ServiceException;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.plugin.facade.HostUserFacade;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ElasticsearchProvider
 *
 * @since 2023/8/2 11:40
 */
@Component
@EnableCaching
public class ElasticsearchProvider {
    @Autowired
    private NctigbaEnvMapper envMapper;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostFacade hostFacade;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostUserFacade hostUserFacade;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private EncryptionUtils encryptionUtils;

    /**
     * set ElasticsearchClient
     *
     * @return ElasticsearchClient
     */
    @Cacheable(cacheNames = "elastic-clients")
    public ElasticsearchClient client() {
        NctigbaEnv env = envMapper.selectOne(
            Wrappers.<NctigbaEnv>lambdaQuery().eq(NctigbaEnv::getType, NctigbaEnv.Type.ELASTICSEARCH));
        List<OpsHostUserEntity> hostUserList = hostUserFacade.listHostUserByHostId(env.getHostid());
        if (CollectionUtil.isEmpty(hostUserList)) {
            throw new ServiceException("host user is null");
        }
        List<OpsHostUserEntity> esUserList = hostUserList.stream().filter(
            item -> item.getUsername().equals(env.getUsername())).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(esUserList)) {
            throw new ServiceException("the elasticsearch user is not exist");
        }
        OpsHostUserEntity esUser = esUserList.get(0);
        // used in develop: encryptionUtils.getKey();
        // Account and password authentication
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(env.getUsername(),
            encryptionUtils.decrypt(esUser.getPassword())));

        // Create the low-level client
        OpsHostEntity host = hostFacade.getById(env.getHostid());
        HttpHost[] httpHosts = {new HttpHost(host.getPublicIp(), env.getPort())};
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
}
