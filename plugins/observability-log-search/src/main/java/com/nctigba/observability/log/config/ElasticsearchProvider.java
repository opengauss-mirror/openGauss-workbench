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

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.gitee.starblues.bootstrap.annotation.AutowiredType.Type;
import com.nctigba.observability.log.model.entity.NctigbaEnvDO;
import com.nctigba.observability.log.model.entity.NctigbaEnvDO.type;
import com.nctigba.observability.log.mapper.NctigbaEnvMapper;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;

@Component
@EnableCaching
public class ElasticsearchProvider {
	@Autowired
	private NctigbaEnvMapper envMapper;
	@Autowired
	@AutowiredType(Type.PLUGIN_MAIN)
	private HostFacade hostFacade;

	@Cacheable(cacheNames = "elastic-clients")
	public ElasticsearchClient client() {
		var env = envMapper.selectOne(Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getType, type.ELASTICSEARCH));
		var host = hostFacade.getById(env.getHostid());
		// Split ip when clustering
		HttpHost[] httpHosts = { new HttpHost(host.getPublicIp(), env.getPort()) };

		// Account and password authentication
		final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("elastic", "changeme"));

		// Create the low-level client
		RestClient restClient = RestClient.builder(httpHosts)
				.setHttpClientConfigCallback(
						httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider))
				.setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder.setConnectTimeout(100000).setSocketTimeout(100000))
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
