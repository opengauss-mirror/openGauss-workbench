package org.opengauss.admin;

import com.gitee.starblues.loader.launcher.SpringBootstrap;
import com.gitee.starblues.loader.launcher.SpringMainBootstrap;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * Application Main
 *
 * @author xielibo
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class AdminApplication implements SpringBootstrap {

    public static void main(String[] args) {
        SpringMainBootstrap.launch(AdminApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  server started.   ლ(´ڡ`ლ)ﾞ");
    }

    @Override
    public void run(String[] args) throws Exception {
        SpringApplication.run(AdminApplication.class, args);
    }

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
    @Bean
    public RequestContextListener requestContextListener(){
        return new RequestContextListener();
    }
}
