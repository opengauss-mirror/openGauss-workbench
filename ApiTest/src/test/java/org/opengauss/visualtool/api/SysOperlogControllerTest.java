/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package org.opengauss.visualtool.api;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hamcrest.Matchers;
import org.opengauss.global.Constants;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.opengauss.global.Constants.getRequestSpecification;
import static org.opengauss.global.Constants.getRequestSpecificationWithoutToken;

/**
 * SysOperlogController test
 *
 * @since 2024/10/29
 */
public class SysOperlogControllerTest {
    private static final Logger logger = LogManager.getLogger(SysOperlogControllerTest.class);

    private List<Integer> operIds;

    @Test
    public void setTestBasePath() {
        RestAssured.basePath = "/system/operlog";
        logger.info("SysOperlogControllerTest start.");
    }

    @Test(priority = 1)
    public void listTest() {
        Response response = getRequestSpecification()
                .params(Constants.PAGE_PARAMS)
                .when()
                .get("/list");
        response.then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200))
                .body("total", Matchers.notNullValue())
                .body("rows", Matchers.notNullValue());
        operIds = response.jsonPath().getList("rows.operId");
    }

    @Test(dependsOnMethods = "listTest")
    public void removeTest() {
        if (operIds == null || operIds.isEmpty()) {
            throw new SkipException("No operation log records found in the environment, skip remove test.");
        }
        String operIdsStr = operIds.stream().map(Object::toString).collect(Collectors.joining(","));

        getRequestSpecification()
                .pathParam("operIds", operIdsStr)
                .when()
                .delete("/{operIds}")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200));
    }

    @Test(dependsOnMethods = "removeTest")
    public void cleanTest() {
        getRequestSpecification()
                .when()
                .delete("/clean")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200));
    }

    @Test(priority = 2)
    public void listWithoutTokenTest() {
        getRequestSpecificationWithoutToken()
                .when()
                .get("/list")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(401));
    }
}
