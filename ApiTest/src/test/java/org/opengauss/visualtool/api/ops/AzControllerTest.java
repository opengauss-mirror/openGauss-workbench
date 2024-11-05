/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package org.opengauss.visualtool.api.ops;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opengauss.global.Constants;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.opengauss.global.Constants.getRequestSpecification;

/**
 * AzController test
 *
 * @since 2024/10/30
 */
public class AzControllerTest {
    private static final Logger logger = LogManager.getLogger(AzControllerTest.class);

    private Map<String, String> addAz = new HashMap<>();
    private String addAzAddress = "测试地址";
    private String changeAddAzAddress = "改变后的测试地址";
    private final String addAzName = "testAz";

    @Test
    public void setTestBasePath() {
        RestAssured.basePath = "/az";
        logger.info("AzControllerTest start.");
    }

    @Test(priority = 1)
    public void addTest() {
        addAz.put("address", addAzAddress);
        addAz.put("name", addAzName);

        getRequestSpecification()
                .contentType(ContentType.JSON)
                .body(addAz)
                .when()
                .post()
                .then()
                .body("code", Matchers.equalTo(200));
    }

    @Test(dependsOnMethods = "addTest")
    public void hasNameTest() {
        getRequestSpecification()
                .param("name", addAzName)
                .when()
                .get("/hasName")
                .then()
                .body("code", Matchers.equalTo(200))
                .body("data", Matchers.equalTo(true));
    }

    @Test(dependsOnMethods = "hasNameTest")
    public void pageTest() {
        Response response = getRequestSpecification()
                .params(Constants.PAGE_PARAMS)
                .when()
                .get("/page");

        response.then()
                .body("code", Matchers.equalTo(200))
                .body("rows.name", Matchers.hasItems("az1", addAzName));

        addAz.put("azId", response.jsonPath().getString("rows.find { it.name == '" + addAzName + "' }.azId"));
    }

    @Test(dependsOnMethods = "pageTest")
    public void listAllTest() {
        getRequestSpecification()
                .when()
                .get("/listAll")
                .then()
                .body("code", Matchers.equalTo(200))
                .body("data.address", Matchers.hasItem(addAzAddress));
    }

    @Test(dependsOnMethods = "listAllTest")
    public void editTest() {
        addAz.put("address", changeAddAzAddress);

        getRequestSpecification()
                .contentType(ContentType.JSON)
                .pathParam("azId", addAz.get("azId"))
                .body(addAz)
                .when()
                .put("/{azId}")
                .then()
                .body("code", Matchers.equalTo(200));
    }

    @Test(dependsOnMethods = "editTest")
    public void getTest() {
        getRequestSpecification()
                .pathParam("azId", addAz.get("azId"))
                .when()
                .get("/{azId}")
                .then()
                .body("code", Matchers.equalTo(200))
                .body("data.address", Matchers.equalTo(changeAddAzAddress));
    }

    @Test(dependsOnMethods = "getTest")
    public void delTest() {
        getRequestSpecification()
                .pathParam("azId", addAz.get("azId"))
                .when()
                .delete("/{azId}")
                .then()
                .body("code", Matchers.equalTo(200));
    }
}
