/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package org.opengauss.visualtool.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.opengauss.global.Constants.getRequestSpecification;

/**
 * SysLogController test
 *
 * @since 2024/10/28
 */
public class SysLogControllerTest {
    private static final Logger logger = LogManager.getLogger(SysLogControllerTest.class);

    private List<String> fileNames;

    @Test
    public void setTestBasePath() {
        RestAssured.basePath = "/system/log";
        logger.info("SysLogControllerTest start.");
    }

    @Test(priority = 1)
    public void getAllLogConfigTest() {
        getRequestSpecification()
                .when()
                .get()
                .then()
                .body("code", Matchers.equalTo(200));
    }

    @Test(priority = 1)
    public void saveAllLogConfigTest() {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("totalSizeCap", "10gb");
        requestBody.put("maxHistory", "30");
        requestBody.put("level", "info");
        requestBody.put("maxFileSize", "5mb");

        getRequestSpecification()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post()
                .then()
                .body("code", Matchers.equalTo(200));
    }

    @Test(priority = 1)
    public void filesTest() {
        Response response = getRequestSpecification()
                .get("/files");
        response.then()
                .body("code", Matchers.equalTo(200));
        fileNames = response.jsonPath().getList("data.name");
    }

    @Test(dependsOnMethods = "filesTest")
    public void downLoadTest() {
        getRequestSpecification()
                .param("filename", fileNames.get(0))
                .when()
                .head("/download")
                .then()
                .statusCode(200)
                .contentType(Matchers.containsString(ContentType.BINARY.toString()))
                .header("Content-Length", Matchers.not(Matchers.equalTo("0")));
    }

    @Test(priority = 1)
    public void testPrintTest() {
        getRequestSpecification()
                .when()
                .get("/print")
                .then()
                .body("code", Matchers.equalTo(200));
    }
}
