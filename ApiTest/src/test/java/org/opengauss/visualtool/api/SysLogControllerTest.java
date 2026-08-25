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
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.opengauss.global.Constants.getRequestSpecification;
import static org.opengauss.global.Constants.getRequestSpecificationWithoutToken;

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
                .statusCode(200)
                .body("code", Matchers.equalTo(200))
                .body("data.totalSizeCap", Matchers.notNullValue())
                .body("data.maxHistory", Matchers.notNullValue())
                .body("data.level", Matchers.notNullValue())
                .body("data.maxFileSize", Matchers.notNullValue());
    }

    @Test(priority = 1)
    public void saveAllLogConfigTest() {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("totalSizeCap", "10gb");
        requestBody.put("maxHistory", 30);
        requestBody.put("level", "info");
        requestBody.put("maxFileSize", "5mb");

        getRequestSpecification()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post()
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200));

        getRequestSpecification()
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200))
                .body("data.level", Matchers.equalTo("info"))
                .body("data.maxFileSize", Matchers.equalTo("5mb"))
                .body("data.totalSizeCap", Matchers.equalTo("10gb"));
    }

    @Test(priority = 1)
    public void filesTest() {
        Response response = getRequestSpecification()
                .get("/files");
        response.then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200))
                .body("data", Matchers.notNullValue());
        fileNames = response.jsonPath().getList("data.name");
    }

    @Test(dependsOnMethods = "filesTest")
    public void downLoadTest() {
        String filename = resolveDownloadFileName();
        getRequestSpecification()
                .param("filename", filename)
                .when()
                .get("/download")
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
                .statusCode(200)
                .body("code", Matchers.equalTo(200));
    }

    @Test(priority = 2)
    public void getAllLogConfigWithoutTokenTest() {
        getRequestSpecificationWithoutToken()
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(401));
    }

    private String resolveDownloadFileName() {
        if (fileNames != null && fileNames.contains("sys.log")) {
            return "sys.log";
        }
        Response response = getRequestSpecification()
                .when()
                .get("/files");
        List<Map<String, Object>> files = response.jsonPath().getList("data");
        if (files != null) {
            for (Map<String, Object> file : files) {
                Object size = file.get("size");
                if (size instanceof Number && ((Number) size).longValue() > 0) {
                    return String.valueOf(file.get("name"));
                }
            }
        }
        throw new SkipException("No non-empty log file available in the environment for download test.");
    }
}
