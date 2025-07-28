/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  ElasticsearchProvider.java
 *
 *  IDENTIFICATION
 *  plugins/observability-log-search/src/main/java/com/nctigba/observability/log/config/ElasticsearchProvider.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.log.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.gitee.starblues.bootstrap.annotation.AutowiredType.Type;
import com.nctigba.observability.log.mapper.NctigbaEnvMapper;
import com.nctigba.observability.log.model.entity.NctigbaEnvDO;
import com.nctigba.observability.log.model.entity.NctigbaEnvDO.InstallType;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@EnableCaching
public class ElasticsearchProvider {
    @Autowired
    private NctigbaEnvMapper envMapper;
    @Autowired
    @AutowiredType(Type.PLUGIN_MAIN)
    private HostFacade hostFacade;

    /**
     * Elasticsearch clients
     *
     * @return ElasticsearchClient
     */
    @Cacheable(cacheNames = "elastic-clients")
    public ElasticsearchClient client() {
        var env = envMapper.selectOne(
                Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getType, InstallType.ELASTICSEARCH));
        var host = hostFacade.getById(env.getHostid());
        // Split ip when clustering
        HttpHost[] httpHosts = {new HttpHost(host.getPublicIp(), env.getPort())};

        // Create the low-level client
        RestClient restClient = RestClient.builder(httpHosts)
                .setRequestConfigCallback(
                        requestConfigBuilder -> requestConfigBuilder.setConnectTimeout(100000).setSocketTimeout(100000))
                .build();

        // Create the transport with a Jackson mapper
        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());

        // And create the API client
        return new ElasticsearchClient(transport);
    }

    /**
     * Clear cache
     */
    @CacheEvict(cacheNames = "elastic-clients")
    public void clear() {
        log.info("clear cache");
    }
}
