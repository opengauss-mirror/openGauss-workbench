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
 *  DataSourceConfig.java
 *
 *  IDENTIFICATION
 *  plugins/observability-log-search/src/main/java/com/nctigba/observability/log/config/DataSourceConfig.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.log.config;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.creator.DruidDataSourceCreator;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.gitee.starblues.bootstrap.PluginContextHolder;
import com.gitee.starblues.spring.environment.EnvironmentProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @author LZW
 * @date 2023/1/5
 */
@Configuration
public class DataSourceConfig {
	@Autowired
	DruidDataSourceCreator druidDataSourceCreator;

	@Bean
	@ConfigurationProperties(prefix = "spring.datasource")
	public DataSource dataSource() {
		EnvironmentProvider environmentProvider = PluginContextHolder.getEnvironmentProvider();
		// read config from dataKit platform
		DataSourceProperty primaryProperty = new DataSourceProperty();
		primaryProperty.setUrl(environmentProvider.getString("spring.datasource.url"));
		primaryProperty.setUsername(environmentProvider.getString("spring.datasource.username"));
		primaryProperty.setPassword(environmentProvider.getString("spring.datasource.password"));
		primaryProperty.setDriverClassName(environmentProvider.getString("spring.datasource.driver-class-name"));
		DataSource primary = druidDataSourceCreator.doCreateDataSource(primaryProperty);
		var d=new DynamicRoutingDataSource();
		d.addDataSource("primary", primary);
		d.setPrimary("primary");
		return d;
	}
}