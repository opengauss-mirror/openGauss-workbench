package org.opengauss.admin.plugin;

import com.gitee.starblues.bootstrap.SpringPluginBootstrap;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * PluginApplication
 *
 * @author liu
 * @since 2022-10-01
 */
@EnableScheduling
@SpringBootApplication(exclude={
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        RedisAutoConfiguration.class,
        RedisRepositoriesAutoConfiguration.class
}, scanBasePackages = {"com.tools.monitor", "org.opengauss.admin.plugin"})
@ComponentScan(basePackages = {"com.tools.monitor.*", "org.opengauss.admin.plugin.*"})
public class PluginApplication extends SpringPluginBootstrap {

    public static void main(String[] args) {
        new PluginApplication().run(args);
    }
}
