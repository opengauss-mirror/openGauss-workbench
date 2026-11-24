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
