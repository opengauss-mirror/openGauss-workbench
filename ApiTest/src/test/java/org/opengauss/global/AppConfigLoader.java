/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package org.opengauss.global;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.Getter;
import org.opengauss.exception.ApiTestException;

import java.io.File;
import java.io.IOException;

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

    private static void loadConfig() {
        String configFilePath = "src/test/resources/application.yml";
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            appConfig = mapper.readValue(new File(configFilePath), AppConfig.class);
        } catch (IOException e) {
            throw new ApiTestException(e);
        }
    }
}
