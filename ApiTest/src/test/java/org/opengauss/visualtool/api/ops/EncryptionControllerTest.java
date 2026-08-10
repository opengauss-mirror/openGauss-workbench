/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package org.opengauss.visualtool.api.ops;

import io.restassured.RestAssured;
import io.restassured.response.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hamcrest.Matchers;
import org.opengauss.utils.RsaUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.opengauss.global.Constants.getRequestSpecification;
import static org.opengauss.global.Constants.getRequestSpecificationWithoutToken;

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

        Response response = getRequestSpecification()
            .when()
            .get("/getKey");
        response.then()
            .statusCode(200)
            .body("code", Matchers.equalTo(200))
            .body("key", Matchers.notNullValue());
        Assert.assertTrue(RsaUtils.isValidPublicKey(response.jsonPath().getString("key")),
            "key should be a valid X509 RSA public key");
    }

    @Test(priority = 2)
    public void getKeyWithoutTokenTest() {
        RestAssured.basePath = "/encryption";

        getRequestSpecificationWithoutToken()
            .when()
            .get("/getKey")
            .then()
            .statusCode(200)
            .body("code", Matchers.equalTo(401));
    }
}
