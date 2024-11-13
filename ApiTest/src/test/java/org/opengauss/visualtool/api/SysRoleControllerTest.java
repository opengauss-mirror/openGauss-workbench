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
import java.util.Map;

import static org.opengauss.global.Constants.getRequestSpecification;

/**
 * SysRoleController test
 *
 * @since 2024/10/29
 */
public class SysRoleControllerTest {
    private static final Logger logger = LogManager.getLogger(SysProfileControllerTest.class);

    private Map<String, Object> sysRole = new HashMap<>();

    private String testRoleName = "测试角色";

    private int testRoleId = 0;

    private String testRoleRemark = "测试角色备注信息";

    private String testRoleChangeStatus = "1";

    @Test
    public void setTestBasePath() {
        RestAssured.basePath = "/system/role";
        logger.info("SysRoleControllerTest start.");
    }

    @Test(priority = 1)
    public void addTest() {
        sysRole.put("roleName", testRoleName);
        sysRole.put("menuIds", new int[] {2});
        sysRole.put("status", "0");

        getRequestSpecification()
                .contentType(ContentType.JSON)
                .body(sysRole)
                .when()
                .post()
                .then()
                .body("code", Matchers.equalTo(200));
    }

    @Test(dependsOnMethods = "addTest")
    public void listTest() {
        Response response = getRequestSpecification()
                .when()
                .get("/list");
        response.then()
                .body("rows.roleName", Matchers.hasItems("超级管理员", testRoleName));

        testRoleId = response.jsonPath().getInt("rows.find { it.roleName == '" + testRoleName + "' }.roleId");
    }

    @Test(dependsOnMethods = "listTest")
    public void editTest() {
        sysRole.put("roleId", testRoleId);
        sysRole.put("remark", testRoleRemark);

        getRequestSpecification()
                .contentType(ContentType.JSON)
                .body(sysRole)
                .when()
                .put()
                .then()
                .body("code", Matchers.equalTo(200));
    }

    @Test(dependsOnMethods = "editTest")
    public void changeStatusTest() {
        sysRole.clear();
        sysRole.put("roleId", testRoleId);
        sysRole.put("status", testRoleChangeStatus);

        getRequestSpecification()
                .contentType(ContentType.JSON)
                .body(sysRole)
                .when()
                .put("/changeStatus")
                .then()
                .body("code", Matchers.equalTo(200));
    }

    @Test(dependsOnMethods = "changeStatusTest")
    public void dataScopeTest() {
        sysRole.clear();
        sysRole.put("roleId", testRoleId);
        sysRole.put("menuIds", new int[] {2, 5});

        getRequestSpecification()
                .contentType(ContentType.JSON)
                .body(sysRole)
                .when()
                .put("/dataScope")
                .then()
                .body("code", Matchers.equalTo(200));
    }

    @Test(dependsOnMethods = "dataScopeTest")
    public void getInfoTest() {
        getRequestSpecification()
                .pathParam("roleId", testRoleId)
                .when()
                .get("/{roleId}")
                .then()
                .body("code", Matchers.equalTo(200))
                .body("data.roleName", Matchers.equalTo(testRoleName))
                .body("data.remark", Matchers.equalTo(testRoleRemark))
                .body("data.status", Matchers.equalTo(testRoleChangeStatus));
    }

    @Test(dependsOnMethods = "getInfoTest")
    public void removeTest() {
        getRequestSpecification()
                .pathParam("roleIds", testRoleId)
                .when()
                .delete("/{roleIds}")
                .then()
                .body("code", Matchers.equalTo(200));
    }

    @Test(priority = 1)
    public void optionselectTest() {
        Response response = getRequestSpecification()
                .when()
                .get("/optionselect");
        response.then()
                .body("code", Matchers.equalTo(200))
                .body("data.roleName", Matchers.hasItems("超级管理员"));
    }
}
