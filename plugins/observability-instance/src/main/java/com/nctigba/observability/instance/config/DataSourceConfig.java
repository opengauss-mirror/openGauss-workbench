package com.nctigba.observability.instance.config;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.gitee.starblues.bootstrap.PluginContextHolder;
import com.gitee.starblues.spring.environment.EnvironmentProvider;
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
	@Bean
	@ConfigurationProperties(prefix = "spring.datasource")
	public DataSource dataSource() {
		EnvironmentProvider environmentProvider = PluginContextHolder.getEnvironmentProvider();
		// read config from dataKit platform
		String url = environmentProvider.getString("spring.datasource.url");
		String username = environmentProvider.getString("spring.datasource.username");
		String password = environmentProvider.getString("spring.datasource.password");
		String driverClassName = environmentProvider.getString("spring.datasource.driver-class-name");

		DataSource primary = DataSourceBuilder.create().driverClassName(driverClassName).url(url).username(username)
				.password(password).build();
		var d=new DynamicRoutingDataSource();
		d.addDataSource("primary", primary);
		d.setPrimary("primary");
		return d;
	}
}