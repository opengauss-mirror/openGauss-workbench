package com.nctigba.observability.log;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import com.gitee.starblues.bootstrap.SpringPluginBootstrap;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        RedisAutoConfiguration.class,
        RedisRepositoriesAutoConfiguration.class
})
@EnableScheduling
public class ObservabilityPluginApplication extends SpringPluginBootstrap {

    public static void main(String[] args) {
        new ObservabilityPluginApplication().run(args);
        System.out.printf("Email notification plug-in started successfully");
    }
}
