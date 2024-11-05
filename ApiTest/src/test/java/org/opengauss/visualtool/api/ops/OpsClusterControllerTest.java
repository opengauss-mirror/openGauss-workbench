/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package org.opengauss.visualtool.api.ops;

import io.restassured.RestAssured;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;

import static org.opengauss.global.Constants.getRequestSpecification;

/**
 * JdbcDbClusterNodeController test
 *
 * @since 2024/10/31
 */
public class OpsClusterControllerTest {
    private static final Logger logger = LogManager.getLogger(OpsClusterControllerTest.class);

    @Test
    public void setTestBasePath() {
        RestAssured.basePath = "/opsCluster";
        logger.info("OpsClusterControllerTest start.");
    }

    @Test(priority = 1)
    public void listClusterTest() {
        getRequestSpecification()
                .when()
                .get("/listCluster")
                .then()
                .body("code", Matchers.equalTo(200));
    }

    @Test(priority = 1)
    public void summaryTest() {
        getRequestSpecification()
                .when()
                .get("/summary")
                .then()
                .body("code", Matchers.equalTo(200));
    }

    @Test(priority = 1)
    public void threadPoolMonitorTest() {
        getRequestSpecification()
                .when()
                .get()
                .then()
                .body("code", Matchers.equalTo(200));
    }
}
