/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package org.opengauss.visualtool.api.ops;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hamcrest.Matchers;
import org.opengauss.global.Constants;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.opengauss.global.Constants.getRequestSpecification;

/**
 * AzController test
 *
 * @since 2024/10/30
 */
public class AzControllerTest {
    private static final Logger logger = LogManager.getLogger(AzControllerTest.class);

    private static final String AZ_NAME = "az-test-" + UUID.randomUUID().toString().replace("-", "");
    private static final String AZ_ADDRESS = "测试地址";
    private static final String CHANGED_AZ_ADDRESS = "改变后的测试地址";

    private final Map<String, String> az = new HashMap<>();
    private boolean isDeletedByTest;

    @BeforeClass
    public void setUp() {
        RestAssured.basePath = "/az";
        logger.info("AzControllerTest start.");
    }

    @Test
    public void addTest() {
        az.put("name", AZ_NAME);
        az.put("address", AZ_ADDRESS);

        getRequestSpecification()
                .contentType(ContentType.JSON)
                .body(az)
                .when()
                .post()
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200));
    }

    @Test(dependsOnMethods = "addTest")
    public void hasNameTest() {
        getRequestSpecification()
                .param("name", AZ_NAME)
                .when()
                .get("/hasName")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200))
                .body("data", Matchers.equalTo(true));
    }

    @Test(dependsOnMethods = "hasNameTest")
    public void pageTest() {
        Response response = getRequestSpecification()
                .params(Constants.PAGE_PARAMS)
                .param("name", AZ_NAME)
                .when()
                .get("/page");

        response.then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200))
                .body("rows.name", Matchers.hasItem(AZ_NAME));

        az.put("azId", response.jsonPath().getString("rows.find { it.name == '" + AZ_NAME + "' }.azId"));
    }

    @Test(dependsOnMethods = "pageTest")
    public void listAllTest() {
        getRequestSpecification()
                .when()
                .get("/listAll")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200))
                .body("data.name", Matchers.hasItem(AZ_NAME))
                .body("data.address", Matchers.hasItem(AZ_ADDRESS));
    }

    @Test(dependsOnMethods = "listAllTest")
    public void editTest() {
        az.put("address", CHANGED_AZ_ADDRESS);

        getRequestSpecification()
                .contentType(ContentType.JSON)
                .pathParam("azId", az.get("azId"))
                .body(az)
                .when()
                .put("/{azId}")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200));
    }

    @Test(dependsOnMethods = "editTest")
    public void getTest() {
        getRequestSpecification()
                .pathParam("azId", az.get("azId"))
                .when()
                .get("/{azId}")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200))
                .body("data.name", Matchers.equalTo(AZ_NAME))
                .body("data.address", Matchers.equalTo(CHANGED_AZ_ADDRESS));
    }

    @Test(dependsOnMethods = "getTest")
    public void delTest() {
        String azId = az.get("azId");
        getRequestSpecification()
                .pathParam("azId", azId)
                .when()
                .delete("/{azId}")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200));

        isDeletedByTest = true;
    }

    /**
     * Best-effort cleanup so a failed chain does not leave the AZ created by this test.
     */
    @AfterClass(alwaysRun = true)
    public void cleanUp() {
        if (isDeletedByTest) {
            return;
        }
        String azId = az.get("azId");
        if (azId == null || azId.isEmpty()) {
            azId = findAzIdByName().orElse(null);
        }
        if (azId == null || azId.isEmpty()) {
            return;
        }
        try {
            getRequestSpecification()
                    .pathParam("azId", azId)
                    .when()
                    .delete("/{azId}");
        } catch (RuntimeException | AssertionError e) {
            logger.warn("Failed to clean up AZ {}: {}", AZ_NAME, e.getMessage());
        }
    }

    private Optional<String> findAzIdByName() {
        try {
            Response response = getRequestSpecification()
                    .param("name", AZ_NAME)
                    .when()
                    .get("/page");
            return Optional.ofNullable(response.jsonPath()
                    .getString("rows.find { it.name == '" + AZ_NAME + "' }.azId"));
        } catch (RuntimeException | AssertionError e) {
            return Optional.empty();
        }
    }
}
