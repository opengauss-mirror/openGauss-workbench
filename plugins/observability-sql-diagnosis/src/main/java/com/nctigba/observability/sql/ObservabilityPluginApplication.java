package com.nctigba.observability.sql;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import com.gitee.starblues.bootstrap.SpringPluginBootstrap;

@SpringBootApplication(exclude = {
		DataSourceAutoConfiguration.class,
		DruidDataSourceAutoConfigure.class,
		HibernateJpaAutoConfiguration.class,
		RedisAutoConfiguration.class,
		RedisRepositoriesAutoConfiguration.class })
public class ObservabilityPluginApplication extends SpringPluginBootstrap {
	public static void main(String[] args) {
		new ObservabilityPluginApplication().run(args);
	}
}
