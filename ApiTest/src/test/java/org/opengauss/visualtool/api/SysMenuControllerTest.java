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
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.opengauss.global.Constants.getRequestSpecification;
import static org.opengauss.global.Constants.getRequestSpecificationWithoutToken;

/**
 * SysMenuController test
 *
 * @since 2024/10/28
 */
public class SysMenuControllerTest {
    private static final Logger logger = LogManager.getLogger(SysMenuControllerTest.class);

    private static final String TEST_MENU_NAME_PREFIX = "__apitest_menu_";
    private static final int TEST_MENU_ORDER_NUM = 9999;

    private String testMenuName;
    private Integer createdMenuId;

    @Test
    public void setTestBasePath() {
        RestAssured.basePath = "/system/menu";
        logger.info("SysMenuControllerTest start.");
    }

    @Test(priority = 1)
    public void listTest() {
        getRequestSpecification()
                .when()
                .get("/list")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200))
                .body("data", Matchers.notNullValue())
                .body("data.menuName", Matchers.hasItem("资源中心"));
    }

    @Test(priority = 1)
    public void getInfoTest() {
        Integer menuId = resolveFirstMenuId();
        getRequestSpecification()
                .pathParam("menuId", menuId)
                .when()
                .get("/{menuId}")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200))
                .body("data.menuId", Matchers.equalTo(menuId));
    }

    @Test(priority = 1)
    public void treeselectTest() {
        getRequestSpecification()
                .when()
                .get("/treeselect")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200))
                .body("data", Matchers.notNullValue())
                .body("data.label", Matchers.hasItem("资源中心"));
    }

    @Test(priority = 1)
    public void roleMenuTreeselectTest() {
        getRequestSpecification()
                .pathParam("roleId", 1)
                .when()
                .get("/roleMenuTreeselect/{roleId}")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200))
                .body("menus", Matchers.notNullValue())
                .body("checkedKeys", Matchers.notNullValue())
                .body("menus.label", Matchers.hasItem("资源中心"));
    }

    @Test(priority = 2)
    public void addMenuTest() {
        testMenuName = TEST_MENU_NAME_PREFIX + System.currentTimeMillis();
        Map<String, Object> requestBody = buildMenuBody(testMenuName, "/__apitest__/" + System.currentTimeMillis());

        getRequestSpecification()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post()
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200));

        createdMenuId = resolveMenuIdByName(testMenuName);
        Assert.assertNotNull(createdMenuId, "the created menu should be queryable by its unique name");
    }

    @Test(priority = 3, dependsOnMethods = "addMenuTest")
    public void editTest() {
        String updatedName = testMenuName + "_updated";
        Map<String, Object> requestBody = buildMenuBody(updatedName, "/__apitest__/updated");
        requestBody.put("menuId", createdMenuId);

        getRequestSpecification()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put()
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200));

        getRequestSpecification()
                .pathParam("menuId", createdMenuId)
                .when()
                .get("/{menuId}")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200))
                .body("data.menuName", Matchers.equalTo(updatedName));
    }

    @Test(priority = 4, dependsOnMethods = "editTest")
    public void removeTest() {
        getRequestSpecification()
                .pathParam("menuId", createdMenuId)
                .when()
                .delete("/{menuId}")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200));

        getRequestSpecification()
                .pathParam("menuId", createdMenuId)
                .when()
                .get("/{menuId}")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200))
                .body("data", Matchers.nullValue());
    }

    @Test(priority = 5)
    public void listWithoutTokenTest() {
        getRequestSpecificationWithoutToken()
                .when()
                .get("/list")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(401));
    }

    private Map<String, Object> buildMenuBody(String name, String path) {
        Map<String, Object> body = new HashMap<>();
        body.put("menuName", name);
        body.put("menuType", "M");
        body.put("orderNum", TEST_MENU_ORDER_NUM);
        body.put("parentId", 0);
        body.put("path", path);
        return body;
    }

    private Integer resolveFirstMenuId() {
        Response response = getRequestSpecification()
                .when()
                .get("/list");
        return response.jsonPath().getInt("data[0].menuId");
    }

    private Integer resolveMenuIdByName(String name) {
        Response response = getRequestSpecification()
                .when()
                .get("/list");
        return response.jsonPath().getInt("data.find { it.menuName == '" + name + "' }.menuId");
    }
}
