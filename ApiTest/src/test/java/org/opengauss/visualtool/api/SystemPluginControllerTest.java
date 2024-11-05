/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package org.opengauss.visualtool.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hamcrest.Matchers;
import org.opengauss.exception.ApiTestException;
import org.opengauss.global.AppConfigLoader;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Stream;

import static org.opengauss.global.Constants.getRequestSpecification;

/**
 * SystemPluginController test
 *
 * @since 2024/10/29
 */
public class SystemPluginControllerTest {
    private static final Logger logger = LogManager.getLogger(SystemPluginControllerTest.class);

    private final String baseOpsPluginId = "base-ops";
    private Path baseOpsJarPath;

    @Test
    public void setTestBasePath() {
        RestAssured.basePath = "/system/plugins";
        logger.info("SystemPluginControllerTest start.");
    }

    @Test(priority = 1)
    public void listTest() {
        getRequestSpecification()
                .when()
                .get("/list")
                .then()
                .body("code", Matchers.equalTo(200))
                .body("rows.pluginId", Matchers.hasItem(baseOpsPluginId));
    }

    @Test(priority = 1)
    public void listExtensionInfoTest() {
        getRequestSpecification()
                .when()
                .get("/extensions/list")
                .then()
                .body("code", Matchers.equalTo(200))
                .body("rows.pluginId", Matchers.hasItem(baseOpsPluginId));
    }

    @Test(priority = 1)
    public void installCountTest() {
        getRequestSpecification()
                .when()
                .get("/count")
                .then()
                .body("code", Matchers.equalTo(200))
                .body("data", Matchers.greaterThan(0));
    }

    @Test(priority = 1)
    public void listContentTest() {
        getRequestSpecification()
                .when()
                .get("/listContent")
                .then()
                .body("code", Matchers.equalTo(200))
                .body("data.pluginId", Matchers.hasItem(baseOpsPluginId));
    }

    @Test(priority = 1)
    public void stopTest() {
        getRequestSpecification()
                .pathParam("id", baseOpsPluginId)
                .when()
                .post("/stop/{id}")
                .then()
                .body("code", Matchers.equalTo(200));
    }

    @Test(dependsOnMethods = "stopTest")
    public void startTest() {
        getRequestSpecification()
                .pathParam("id", baseOpsPluginId)
                .when()
                .post("/start/{id}")
                .then()
                .body("code", Matchers.equalTo(200));
    }

    @Test(priority = 1)
    public void getTest() {
        getRequestSpecification()
                .pathParam("id", baseOpsPluginId)
                .when()
                .post("/get/{id}")
                .then()
                .body("code", Matchers.equalTo(200))
                .body("data.pluginId", Matchers.equalTo(baseOpsPluginId));
    }

    @Test(priority = 1)
    public void savePluginConfigDataTest() {
        HashMap<String, String> requestBody = new HashMap<>();
        requestBody.put("pluginId", baseOpsPluginId);
        requestBody.put("configData", "test config data");

        getRequestSpecification()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/pluginConfigData")
                .then()
                .body("code", Matchers.equalTo(200));
    }

    @Test(dependsOnMethods = "startTest")
    public void uninstallTest() {
        findBaseOpsJarPath();

        getRequestSpecification()
                .pathParam("id", baseOpsPluginId)
                .when()
                .post("/uninstall/{id}")
                .then()
                .body("code", Matchers.equalTo(200));
    }

    @Test(dependsOnMethods = "uninstallTest")
    public void installTest() {
        getRequestSpecification()
                .multiPart("file", baseOpsJarPath.toFile())
                .when()
                .post("/install")
                .then()
                .body("code", Matchers.equalTo(200))
                .body("data.pluginId", Matchers.equalTo(baseOpsPluginId));
    }

    private void findBaseOpsJarPath() {
        Path dirPath = Paths.get(AppConfigLoader.getAppConfig().getDatakit().getPluginPath());
        try (Stream<Path> paths = Files.walk(dirPath)) {
            Optional<Path> matchingFile = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().contains(baseOpsPluginId))
                    .findFirst();

            if (matchingFile.isPresent()) {
                baseOpsJarPath = matchingFile.get();
            } else {
                throw new ApiTestException("Find base-ops jar file failed: No matching file found.");
            }
        } catch (IOException e) {
            throw new ApiTestException("Find base-ops jar file failed.", e);
        }
    }
}
