/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package org.opengauss.visualtool.api;

import static org.opengauss.global.Constants.getRequestSpecification;
import static org.opengauss.global.Constants.getRequestSpecificationWithoutToken;

import io.restassured.RestAssured;
import io.restassured.response.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hamcrest.Matchers;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.util.List;

/**
 * SysHandleDiagnosticController test
 *
 * @since 2026/08/18
 */
public class SysHandleDiagnosticControllerTest {
    private static final Logger logger = LogManager.getLogger(SysHandleDiagnosticControllerTest.class);

    @Test
    public void setTestBasePath() {
        RestAssured.basePath = "/system";
        logger.info("SysHandleDiagnosticControllerTest start.");
    }

    @Test(priority = 1)
    public void cachedPluginResourceKeyTest() {
        getRequestSpecification()
                .when()
                .get("/handler/plugin/resource/key")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200))
                .body("data", Matchers.notNullValue());
    }

    @Test(priority = 1)
    public void cachedAllPluginResourceTest() {
        getRequestSpecification()
                .when()
                .get("/handler/plugin/all/resource")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200))
                .body("data", Matchers.notNullValue());
    }

    @Test(priority = 1)
    public void cachedPluginResourceByKeyTest() {
        Response keyResponse = getRequestSpecification()
                .when()
                .get("/handler/plugin/resource/key");
        List<String> keys = keyResponse.jsonPath().getList("data");
        if (keys == null || keys.isEmpty()) {
            throw new SkipException("No cached plugin resource key available, skip plugin resource query by key.");
        }
        getRequestSpecification()
                .queryParam("key", keys.get(0))
                .when()
                .get("/handler/plugin/resource")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200))
                .body("data", Matchers.notNullValue());
    }

    @Test(priority = 1)
    public void cachedPluginResourceByUnknownKeyTest() {
        getRequestSpecification()
                .queryParam("key", "__unknown_plugin_key__")
                .when()
                .get("/handler/plugin/resource")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200));
    }

    @Test(priority = 1)
    public void cachedPluginResourceMissingKeyTest() {
        getRequestSpecification()
                .when()
                .get("/handler/plugin/resource")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(400));
    }

    @Test(priority = 2)
    public void monitorStatusTest() {
        getRequestSpecification()
                .when()
                .get("/handler/monitor/status")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200))
                .body("data", Matchers.anyOf(Matchers.is(true), Matchers.is(false)));
    }

    @Test(priority = 3, dependsOnMethods = "monitorStatusTest")
    public void monitorStartTest() {
        getRequestSpecification()
                .when()
                .get("/handler/monitor/start")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200));
        assertMonitorStatus(true);
    }

    @Test(priority = 3, dependsOnMethods = "monitorStartTest")
    public void monitorPauseTest() {
        getRequestSpecification()
                .when()
                .get("/handler/monitor/pause")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200));
        assertMonitorStatus(false);
    }

    @Test(priority = 3, dependsOnMethods = "monitorPauseTest")
    public void monitorResumeTest() {
        getRequestSpecification()
                .when()
                .get("/handler/monitor/resume")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200));
        assertMonitorStatus(true);
    }

    @Test(priority = 4)
    public void monitorStatusWithoutTokenTest() {
        getRequestSpecificationWithoutToken()
                .when()
                .get("/handler/monitor/status")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(401));
    }

    @Test(priority = 5, dependsOnMethods = "monitorResumeTest")
    public void monitorCloseTest() {
        getRequestSpecification()
                .when()
                .get("/handler/monitor/close")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200));
        assertMonitorStatus(false);
        logger.info("Handle monitor closed; it stays disabled until the DataKit service is restarted.");
    }

    private void assertMonitorStatus(boolean isExpected) {
        getRequestSpecification()
                .when()
                .get("/handler/monitor/status")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200))
                .body("data", Matchers.is(isExpected));
    }
}
