package com.nctigba.observability.log;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import com.gitee.starblues.bootstrap.EmptyMainApplicationContext;
import com.gitee.starblues.spring.MainApplicationContext;

@Profile({"dev", "gba"})
@SpringBootApplication(exclude = {
        HibernateJpaAutoConfiguration.class,
        RedisAutoConfiguration.class,
        RedisRepositoriesAutoConfiguration.class})
public class ObservabilityPluginBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(ObservabilityPluginBootApplication.class, args);
        System.out.printf("Email notification plug-in started successfully");
    }

    @Bean
    @Primary
    public MainApplicationContext test() {
        return new EmptyMainApplicationContext();
    }
}
