package org.opengauss.admin.plugin;

import com.gitee.starblues.bootstrap.SpringPluginBootstrap;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

@SpringBootApplication(exclude={
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
})
public class DataSyncMysqlPluginApplication extends SpringPluginBootstrap {

    public static void main(String[] args) {
        new DataSyncMysqlPluginApplication().run(args);
        System.out.printf("Mysql Synctool started.");
    }
}
