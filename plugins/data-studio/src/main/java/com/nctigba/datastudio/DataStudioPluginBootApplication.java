package com.nctigba.datastudio;

import com.gitee.starblues.bootstrap.EmptyMainApplicationContext;
import com.gitee.starblues.spring.MainApplicationContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile({ "dev", "gba" })
@SpringBootApplication(exclude = {
		HibernateJpaAutoConfiguration.class,
		RedisAutoConfiguration.class,
		RedisRepositoriesAutoConfiguration.class })
public class DataStudioPluginBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(DataStudioPluginBootApplication.class, args);
	}

	@Bean
	@Primary
	public MainApplicationContext test() {
		return new EmptyMainApplicationContext();
	}
}
