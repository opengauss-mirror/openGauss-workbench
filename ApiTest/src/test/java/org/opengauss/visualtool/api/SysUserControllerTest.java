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
import org.opengauss.utils.RsaUtils;
import org.opengauss.visualtool.api.model.SysUser;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;

import static org.opengauss.global.Constants.getRequestSpecification;

/**
 * SysUserController test
 *
 * @since 2024/10/30
 */
public class SysUserControllerTest {
    private static final Logger logger = LogManager.getLogger(SysUserControllerTest.class);

    private SysUser addUser;
    private final String passwordForAddingUser = "password";
    private final String remarkForUserChange = "edit test";
    private final String statusForUserChange = "1";

    @Test
    public void setTestBasePath() {
        RestAssured.basePath = "/system/user";
        logger.info("SysUserControllerTest start.");
    }

    @Test(priority = 1)
    public void addTest() {
        addUser = SysUser.builder()
                .userName("testUser")
                .nickName("testUserNick")
                .password(RsaUtils.encryptByPublicKey(passwordForAddingUser))
                .remark("testUser remark")
                .status("0").build();

        getRequestSpecification()
                .contentType(ContentType.JSON)
                .body(addUser)
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

        String adminUsername = "admin";
        response.then()
                .body("code", Matchers.equalTo(200))
                .body("rows.userName", Matchers.hasItems(adminUsername, addUser.getUserName()));

        int anInt = response.jsonPath().getInt(
                "rows.find { it.userName == '" + addUser.getUserName() + "' }.userId");
        addUser.setUserId(anInt);
    }

    @Test(dependsOnMethods = "listTest")
    public void editTest() {
        addUser.setPassword(null);
        addUser.setRemark(remarkForUserChange);

        getRequestSpecification()
                .contentType(ContentType.JSON)
                .body(addUser)
                .when()
                .put()
                .then()
                .body("code", Matchers.equalTo(200));
    }

    @Test(dependsOnMethods = "editTest")
    public void changeStatusTest() {
        addUser.setPassword(null);
        addUser.setStatus(statusForUserChange);

        getRequestSpecification()
                .contentType(ContentType.JSON)
                .body(addUser)
                .when()
                .put("/changeStatus")
                .then()
                .body("code", Matchers.equalTo(200));
    }

    @Test(dependsOnMethods = "changeStatusTest")
    public void getInfoTest() {
        getRequestSpecification()
                .pathParam("userId", addUser.getUserId())
                .when()
                .get("/{userId}")
                .then()
                .body("code", Matchers.equalTo(200))
                .body("data.userName", Matchers.equalTo(addUser.getUserName()))
                .body("data.remark", Matchers.equalTo(remarkForUserChange))
                .body("data.status", Matchers.equalTo(statusForUserChange));
    }

    @Test(dependsOnMethods = "getInfoTest")
    public void resetPwdTest() {
        SysUser requestBody = SysUser.builder()
                .userId(addUser.getUserId())
                .password(RsaUtils.encryptByPublicKey(passwordForAddingUser))
                .build();

        getRequestSpecification()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put("/resetPwd")
                .then()
                .body("code", Matchers.equalTo(200));
    }

    @Test(dependsOnMethods = "resetPwdTest")
    public void removeTest() {
        getRequestSpecification()
                .pathParam("userIds", addUser.getUserId())
                .when()
                .delete("/{userIds}")
                .then()
                .body("code", Matchers.equalTo(200));
    }

    @Test(priority = 1)
    public void getInfoWithoutUserIdTest() {
        getRequestSpecification()
                .when()
                .get("/")
                .then()
                .body("code", Matchers.equalTo(200))
                .body("roles", Matchers.notNullValue())
                .body("data", Matchers.nullValue());
    }

    @Test(priority = 1)
    public void addWithEmptyPasswordTest() {
        SysUser invalidUser = SysUser.builder()
                .userName("apiTestEmptyPwd")
                .nickName("apiTestEmptyPwdNick")
                .status("0").build();

        getRequestSpecification()
                .contentType(ContentType.JSON)
                .body(invalidUser)
                .when()
                .post()
                .then()
                .body("code", Matchers.equalTo(50132));
    }

    @Test(priority = 1)
    public void addWithTooLongUserNameTest() {
        SysUser invalidUser = SysUser.builder()
                .userName("a".repeat(31))
                .nickName("apiTestLongNameNick")
                .password(RsaUtils.encryptByPublicKey(passwordForAddingUser))
                .status("0").build();

        getRequestSpecification()
                .contentType(ContentType.JSON)
                .body(invalidUser)
                .when()
                .post()
                .then()
                .body("code", Matchers.equalTo(50120));
    }

    @Test(priority = 1)
    public void addWithDuplicateUserNameTest() {
        SysUser invalidUser = SysUser.builder()
                .userName("admin")
                .nickName("apiTestDupNameNick")
                .password(RsaUtils.encryptByPublicKey(passwordForAddingUser))
                .status("0").build();

        getRequestSpecification()
                .contentType(ContentType.JSON)
                .body(invalidUser)
                .when()
                .post()
                .then()
                .body("code", Matchers.equalTo(50115));
    }

    @Test(priority = 1)
    public void addWithTooLongNickNameTest() {
        SysUser invalidUser = SysUser.builder()
                .userName("apiTestLongNickName")
                .nickName("a".repeat(31))
                .password(RsaUtils.encryptByPublicKey(passwordForAddingUser))
                .status("0").build();

        getRequestSpecification()
                .contentType(ContentType.JSON)
                .body(invalidUser)
                .when()
                .post()
                .then()
                .body("code", Matchers.equalTo(50121));
    }

    @Test(priority = 1)
    public void addWithTooLongRemarkTest() {
        SysUser invalidUser = SysUser.builder()
                .userName("apiTestLongRemark")
                .nickName("apiTestLongRemarkNick")
                .password(RsaUtils.encryptByPublicKey(passwordForAddingUser))
                .remark("a".repeat(201))
                .status("0").build();

        getRequestSpecification()
                .contentType(ContentType.JSON)
                .body(invalidUser)
                .when()
                .post()
                .then()
                .body("code", Matchers.equalTo(50122));
    }

    @Test(priority = 1)
    public void removeCurrentUserTest() {
        getRequestSpecification()
                .pathParam("userIds", 1)
                .when()
                .delete("/{userIds}")
                .then()
                .body("code", Matchers.equalTo(500));
    }
}
