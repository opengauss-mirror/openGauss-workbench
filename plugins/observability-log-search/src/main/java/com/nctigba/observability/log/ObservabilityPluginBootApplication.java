/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  ObservabilityPluginBootApplication.java
 *
 *  IDENTIFICATION
 *  plugins/observability-log-search/src/main/java/com/nctigba/observability/log/ObservabilityPluginBootApplication.java
 *
 *  -------------------------------------------------------------------------
 */

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
import org.springframework.scheduling.annotation.EnableScheduling;

@Profile({"dev", "gba"})
@EnableScheduling
@SpringBootApplication(exclude = {
        HibernateJpaAutoConfiguration.class,
        RedisAutoConfiguration.class,
        RedisRepositoriesAutoConfiguration.class})
public class ObservabilityPluginBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(ObservabilityPluginBootApplication.class, args);
    }

    @Bean
    @Primary
    public MainApplicationContext test() {
        return new EmptyMainApplicationContext();
    }
}
