/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
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

    static {
        loadConfig();
    }

    /**
     * load the application configuration
     */
    public static void loadConfig() {
        synchronized (AppConfigLoader.class) {
            if (Objects.isNull(appConfig)) {
                String configFilePath = "src/test/resources/application.yml";
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
