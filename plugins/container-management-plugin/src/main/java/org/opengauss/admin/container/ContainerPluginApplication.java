/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
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
 * ContainerPluginApplication.java
 *
 * IDENTIFICATION
 * plugins/container-management-plugin/src/main/java/org/opengauss/admin/container/ContainerPluginApplication.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.container;

import com.gitee.starblues.bootstrap.SpringPluginBootstrap;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Spring Boot 应用程序的配置类
 *
 * @since 2024-08-29
 */
@SpringBootApplication()
@EnableTransactionManagement
@EnableAsync
@EnableScheduling
@MapperScan("org.opengauss.admin.container.mapper")
public class ContainerPluginApplication extends SpringPluginBootstrap {
    /**
     * 主方法，启动容器插件应用
     *
     * @param args 应用启动参数
     */
    public static void main(String[] args) {
        new ContainerPluginApplication().run(args);
    }
}
