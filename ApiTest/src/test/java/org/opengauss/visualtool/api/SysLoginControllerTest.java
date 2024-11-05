/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package org.opengauss.visualtool.api;

import io.restassured.RestAssured;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opengauss.utils.LoginUtils;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;

import static org.opengauss.global.Constants.getRequestSpecification;

/**
 * SysLoginController test
 *
 * @since 2024/10/21
 */
public class SysLoginControllerTest {
    private static final Logger logger = LogManager.getLogger(SysLoginControllerTest.class);

    @Test
    public void setTestBasePath() {
        RestAssured.basePath = "/";
        logger.info("SysLoginControllerTest start.");
    }

    @Test(priority = 1)
    public void loginTest() {
        LoginUtils.login();
    }

    @Test(priority = 1)
    public void getInfoTest() {
        getRequestSpecification()
                .when()
                .get("/getInfo")
                .then()
                .body("code", Matchers.equalTo(200));
    }

    @Test(priority = 1)
    public void getRoutersTest() {
        getRequestSpecification()
                .when()
                .get("/getRouters")
                .then()
                .body("code", Matchers.equalTo(200));
    }

    @Test(priority = 1)
    public void getIndexStanceRouteTest() {
        getRequestSpecification()
                .when()
                .get("/getIndexInstanceRouters")
                .then()
                .body("code", Matchers.equalTo(200));
    }
}
