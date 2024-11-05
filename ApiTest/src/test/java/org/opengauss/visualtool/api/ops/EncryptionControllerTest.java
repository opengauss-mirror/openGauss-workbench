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
 * EncryptionController test
 *
 * @since 2024/10/30
 */
public class EncryptionControllerTest {
    private static final Logger logger = LogManager.getLogger(EncryptionControllerTest.class);

    @Test
    public void setTestBasePath() {
        RestAssured.basePath = "/encryption";
        logger.info("EncryptionControllerTest start.");
    }

    @Test(priority = 1)
    public void getKeyTest() {
        RestAssured.basePath = "/encryption";

        getRequestSpecification()
                .when()
                .get("/getKey")
                .then()
                .body("code", Matchers.equalTo(200))
                .body("key", Matchers.notNullValue());
    }
}
