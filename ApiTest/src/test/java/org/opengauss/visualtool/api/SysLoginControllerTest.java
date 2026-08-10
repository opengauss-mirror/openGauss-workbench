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
import org.opengauss.global.AppConfigLoader;
import org.opengauss.utils.LoginUtils;
import org.opengauss.utils.RsaUtils;
import org.opengauss.visualtool.api.model.LoginBody;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.opengauss.global.Constants.getRequestSpecification;
import static org.opengauss.global.Constants.getRequestSpecificationWithoutToken;

/**
 * SysLoginController test
 *
 * @since 2024/10/21
 */
public class SysLoginControllerTest {
    private static final Logger logger = LogManager.getLogger(SysLoginControllerTest.class);
    private static final String JWT_SEGMENT_SEPARATOR = "\\.";
    private static final int JWT_SEGMENT_COUNT = 3;

    @Test
    public void setTestBasePath() {
        RestAssured.basePath = "/";
        logger.info("SysLoginControllerTest start.");
    }

    @Test(priority = 1)
    public void loginTest() {
        String token = LoginUtils.login();
        Assert.assertNotNull(token, "token should not be null");
        Assert.assertFalse(token.isEmpty(), "token should not be empty");
        Assert.assertEquals(token.split(JWT_SEGMENT_SEPARATOR).length, JWT_SEGMENT_COUNT,
            "token should be a JWT with three segments");
    }

    @Test(priority = 1)
    public void pubKeyTest() {
        Response response = RestAssured.given()
            .contentType(ContentType.JSON)
            .when()
            .get(RestAssured.baseURI + "/pubKey");
        response.then()
            .statusCode(200)
            .body("code", Matchers.equalTo(200));
        String key = RsaUtils.resolvePublicKey(response);
        Assert.assertTrue(RsaUtils.isValidPublicKey(key), "pubKey should be a valid X509 RSA public key");
        // the public key stays stable within one Datakit service lifecycle
        String secondKey = RsaUtils.resolvePublicKey(RestAssured.given()
            .contentType(ContentType.JSON)
            .when()
            .get(RestAssured.baseURI + "/pubKey"));
        Assert.assertEquals(secondKey, key, "pubKey should stay stable across requests");
    }

    @Test(priority = 1)
    public void getInfoTest() {
        getRequestSpecification()
            .when()
            .get("/getInfo")
            .then()
            .statusCode(200)
            .body("code", Matchers.equalTo(200))
            .body("user", Matchers.notNullValue())
            .body("user.userId", Matchers.notNullValue())
            .body("roles", Matchers.notNullValue())
            .body("permissions", Matchers.notNullValue());
    }

    @Test(priority = 1)
    public void getRoutersTest() {
        getRequestSpecification()
            .when()
            .get("/getRouters")
            .then()
            .statusCode(200)
            .body("code", Matchers.equalTo(200))
            .body("data", Matchers.notNullValue());
    }

    @Test(priority = 1)
    public void getIndexStanceRouteTest() {
        getRequestSpecification()
            .when()
            .get("/getIndexInstanceRouters")
            .then()
            .statusCode(200)
            .body("code", Matchers.equalTo(200));
    }

    @Test(priority = 2)
    public void loginWithWrongPasswordTest() {
        String encryptedWrongPassword = RsaUtils.encryptByPublicKey("wrong-password");
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(new LoginBody(getUsername(), encryptedWrongPassword))
            .when()
            .post(RestAssured.baseURI + "/login")
            .then()
            .statusCode(200)
            .body("code", Matchers.not(Matchers.equalTo(200)));
    }

    @Test(priority = 2)
    public void loginWithPlainPasswordTest() {
        // the password must be RSA encrypted before being sent to /login
        String plainPassword = AppConfigLoader.getAppConfig().getDatakit().getPassword();
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(new LoginBody(getUsername(), plainPassword))
            .when()
            .post(RestAssured.baseURI + "/login")
            .then()
            .statusCode(200)
            .body("code", Matchers.not(Matchers.equalTo(200)));
    }

    @Test(priority = 2)
    public void getInfoWithoutTokenTest() {
        getRequestSpecificationWithoutToken()
            .when()
            .get("/getInfo")
            .then()
            .statusCode(200)
            .body("code", Matchers.equalTo(401));
    }

    @Test(priority = 2)
    public void getRoutersWithoutTokenTest() {
        getRequestSpecificationWithoutToken()
            .when()
            .get("/getRouters")
            .then()
            .statusCode(200)
            .body("code", Matchers.equalTo(401));
    }

    @Test(priority = 2)
    public void getIndexStanceRouteWithoutTokenTest() {
        getRequestSpecificationWithoutToken()
            .when()
            .get("/getIndexInstanceRouters")
            .then()
            .statusCode(200)
            .body("code", Matchers.equalTo(401));
    }

    private static String getUsername() {
        return AppConfigLoader.getAppConfig().getDatakit().getUsername();
    }
}
