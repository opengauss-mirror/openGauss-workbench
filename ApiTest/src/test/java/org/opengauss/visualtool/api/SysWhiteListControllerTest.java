/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package org.opengauss.visualtool.api;

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
 * SysWhiteListController test
 *
 * @since 2024/10/30
 */
public class SysWhiteListControllerTest {
    private static final Logger logger = LogManager.getLogger(SysWhiteListControllerTest.class);

    private final String testWhiteListTitle = "test white list";
    private final String testWhiteListIpList = "192.168.0.115";
    private final String changeWhiteListIpList = "192.168.0.115,192.168.0.116";
    private Map<String, Object> sysWhiteList = new HashMap<>();

    @Test
    public void setTestBasePath() {
        RestAssured.basePath = "/sys/whiteList";
        logger.info("SysWhiteListControllerTest start.");
    }

    @Test(priority = 1)
    public void addTest() {
        sysWhiteList.put("title", testWhiteListTitle);
        sysWhiteList.put("ipList", testWhiteListIpList);

        getRequestSpecification()
                .contentType(ContentType.JSON)
                .body(sysWhiteList)
                .when()
                .post()
                .then()
                .body("code", Matchers.equalTo(200));
    }

    @Test(dependsOnMethods = "addTest")
    public void listTest() {
        Response response = getRequestSpecification()
                .params(Constants.PAGE_PARAMS)
                .when()
                .get("/list");

        response.then()
                .body("code", Matchers.equalTo(200))
                .body("rows.title", Matchers.hasItem(testWhiteListTitle));

        sysWhiteList.put(
                "id", response.jsonPath().getInt("rows.find { it.title == '" + testWhiteListTitle + "' }.id"));
    }

    @Test(dependsOnMethods = "listTest")
    public void listAllTest() {
        getRequestSpecification()
                .when()
                .get("/list/all")
                .then()
                .body("code", Matchers.equalTo(200))
                .body("data.ipList", Matchers.hasItem(testWhiteListIpList));
    }

    @Test(dependsOnMethods = "listAllTest")
    public void editTest() {
        sysWhiteList.put("ipList", changeWhiteListIpList);

        getRequestSpecification()
                .contentType(ContentType.JSON)
                .body(sysWhiteList)
                .put()
                .then()
                .body("code", Matchers.equalTo(200));
    }

    @Test(dependsOnMethods = "editTest")
    public void getInfoTest() {
        getRequestSpecification()
                .pathParam("id", sysWhiteList.get("id"))
                .when()
                .get("/{id}")
                .then()
                .body("code", Matchers.equalTo(200))
                .body("data.title", Matchers.equalTo(testWhiteListTitle))
                .body("data.ipList", Matchers.equalTo(changeWhiteListIpList));
    }

    @Test(dependsOnMethods = "getInfoTest")
    public void removeTest() {
        getRequestSpecification()
                .pathParam("ids", sysWhiteList.get("id"))
                .when()
                .delete("/{ids}")
                .then()
                .body("code", Matchers.equalTo(200));
    }
}
