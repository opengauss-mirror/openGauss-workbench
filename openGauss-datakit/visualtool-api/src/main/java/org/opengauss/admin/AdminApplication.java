/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 * AdminApplication.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-api/src/main/java/org/opengauss/admin/AdminApplication.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin;

import com.gitee.starblues.loader.launcher.SpringBootstrap;
import com.gitee.starblues.loader.launcher.SpringMainBootstrap;

import cn.hutool.core.thread.ThreadUtil;

import org.opengauss.admin.common.utils.AesGcmUtils;
import org.opengauss.admin.common.utils.RsaUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * Application Main
 *
 * @author xielibo
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan({"org.opengauss.admin", "org.opengauss.agent"})
public class AdminApplication implements SpringBootstrap {
    public static void main(String[] args) {
        SpringMainBootstrap.launch(AdminApplication.class, args);
    }

    @Override
    public void run(String[] args) throws Exception {
        ThreadUtil.execute(RsaUtils::publicKey);
        ThreadUtil.execute(() -> {
            ThreadUtil.sleep(1000);
            AesGcmUtils.initializeKey();
        });
        SecurityContextHolder.setStrategyName("MODE_INHERITABLETHREADLOCAL");
        SpringApplication.run(AdminApplication.class, args);
    }

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
    @Bean
    public RequestContextListener requestContextListener() {
        return new RequestContextListener();
    }
}
