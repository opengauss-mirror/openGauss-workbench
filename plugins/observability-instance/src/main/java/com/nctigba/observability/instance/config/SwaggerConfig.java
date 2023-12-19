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
 *  SwaggerConfig.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/main/java/com/nctigba/observability/instance/config/SwaggerConfig.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.config;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.ArrayList;

/**
 * Config for swagger
 *
 * @since 2023/12/1
 */
@Profile({"dev"})
@SpringBootConfiguration
public class SwaggerConfig {
    /**
     * Config bean
     *
     * @return Docket bean
     * @since 2023/12/1
     */
    @Bean
    public Docket docket() {
        return new Docket(DocumentationType.OAS_30)
                .apiInfo(apiInfo())
                .select()
                .build().enableUrlTemplating(true);
    }

    /**
     * Api info
     *
     * @return ApiInfo entity
     * @since 2023/12/1
     */
    private ApiInfo apiInfo() {
        Contact contact = new Contact("", "", "");
        return new ApiInfo(
                "API for Instance Monitoring Plugin",
                "",
                "v1.0",
                "",
                contact,
                "",
                "",
                new ArrayList()
        );
    }
}