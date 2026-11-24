/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.global;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import io.restassured.RestAssured;
import io.restassured.config.SSLConfig;
import lombok.Getter;

import org.opengauss.exception.ApiTestException;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

/**
 * the application configuration loader
 *
 * @since 2024/11/4
 */
public class AppConfigLoader {
    @Getter
    private static AppConfig appConfig;

    /**
     * load the application configuration
     */
    public static void loadConfig() {
        synchronized (AppConfigLoader.class) {
            if (Objects.isNull(appConfig)) {
                String configFilePath = System.getProperty("apitest.config");
                if (Objects.isNull(configFilePath) || configFilePath.isBlank()) {
                    configFilePath = System.getenv("APITEST_CONFIG");
                }
                if (Objects.isNull(configFilePath) || configFilePath.isBlank()) {
                    configFilePath = "src/test/resources/application.yml";
                }
                ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
                try {
                    appConfig = mapper.readValue(new File(configFilePath), AppConfig.class);
                } catch (IOException e) {
                    throw new ApiTestException(e);
                }
                RestAssured.baseURI = AppConfigLoader.getAppConfig().getDatakit().getServerUrl();
                RestAssured.config = RestAssured.config().sslConfig(new SSLConfig().relaxedHTTPSValidation());
            }
        }
    }
}
