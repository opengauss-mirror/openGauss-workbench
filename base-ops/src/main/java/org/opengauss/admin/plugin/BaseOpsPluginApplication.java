package org.opengauss.admin.plugin;

import com.gitee.starblues.bootstrap.SpringPluginBootstrap;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication()
public class BaseOpsPluginApplication extends SpringPluginBootstrap {

    public static void main(String[] args) {
        new BaseOpsPluginApplication().run(args);
        System.out.printf("Base Ops Plugin Start Success.");
    }
}
