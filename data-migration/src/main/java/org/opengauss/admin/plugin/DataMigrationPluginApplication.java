package org.opengauss.admin.plugin;

import com.gitee.starblues.bootstrap.SpringPluginBootstrap;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication()
public class DataMigrationPluginApplication extends SpringPluginBootstrap {

    public static void main(String[] args) {
        new DataMigrationPluginApplication().run(args);
    }
}
