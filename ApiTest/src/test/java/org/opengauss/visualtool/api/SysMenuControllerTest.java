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
import org.testng.annotations.Test;

import java.util.HashMap;

import static org.opengauss.global.Constants.getRequestSpecification;

/**
 * SysMenuController test
 *
 * @since 2024/10/28
 */
public class SysMenuControllerTest {
    private static final Logger logger = LogManager.getLogger(SysLogControllerTest.class);

    private int menuId;
    private HashMap<String, Object> requestBody = new HashMap<>();

    @Test
    public void setTestBasePath() {
        RestAssured.basePath = "/system/menu";
        logger.info("SysMenuControllerTest start.");
    }

    @Test(priority = 1)
    public void listTest() {
        Response response = getRequestSpecification()
                .when()
                .get("/list");
        response.then()
                .body("code", Matchers.equalTo(200))
                .body("data.menuName", Matchers.hasItem("资源中心"));
        menuId = response.jsonPath().getInt("data.find { it.menuName == '资源中心' }.menuId");
    }

    @Test(dependsOnMethods = "listTest")
    public void getInfoTest() {
        getRequestSpecification()
                .pathParam("menuId", menuId)
                .when()
                .get("/{menuId}")
                .then()
                .body("code", Matchers.equalTo(200))
                .body("data.menuName", Matchers.equalTo("资源中心"));
    }

    @Test(priority = 1)
    public void treeselectTest() {
        getRequestSpecification()
                .when()
                .get("/treeselect")
                .then()
                .body("code", Matchers.equalTo(200))
                .body("data.label", Matchers.hasItem("资源中心"));
    }

    @Test(priority = 1)
    public void roleMenuTreeselectTest() {
        getRequestSpecification()
                .pathParam("roleId", 2)
                .when()
                .get("/roleMenuTreeselect/{roleId}")
                .then()
                .body("code", Matchers.equalTo(200))
                .body("menus.label", Matchers.hasItem("资源中心"));
    }

    @Test(priority = 1)
    public void addMenuTest() {
        requestBody.put("menuId", 3000);
        requestBody.put("menuName", "测试目录");
        requestBody.put("menuType", "M");
        requestBody.put("orderNum", 10);
        requestBody.put("parentId", 0);
        requestBody.put("path", "/test");

        getRequestSpecification()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post()
                .then().body("code", Matchers.equalTo(200));
    }

    @Test(dependsOnMethods = "addMenuTest")
    public void editTest() {
        getRequestSpecification()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put()
                .then()
                .body("code", Matchers.equalTo(200));
    }

    @Test(dependsOnMethods = "editTest")
    public void removeTest() {
        getRequestSpecification()
                .pathParam("menuId", requestBody.get("menuId"))
                .when()
                .delete("/{menuId}")
                .then()
                .body("code", Matchers.equalTo(200));
    }
}
