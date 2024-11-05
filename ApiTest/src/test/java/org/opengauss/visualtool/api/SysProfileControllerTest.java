/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package org.opengauss.visualtool.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opengauss.global.AppConfigLoader;
import org.opengauss.utils.RsaUtils;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;

import java.io.File;
import java.util.HashMap;

import static org.opengauss.global.Constants.getRequestSpecification;

/**
 * SysProfileController test
 *
 * @since 2024/10/29
 */
public class SysProfileControllerTest {
    private static final Logger logger = LogManager.getLogger(SysProfileControllerTest.class);

    private HashMap<String, Object> userProfile = new HashMap<>();

    @Test
    public void setTestBasePath() {
        RestAssured.basePath = "/system/user/profile";
        logger.info("SysProfileControllerTest start.");
    }

    @Test(priority = 1)
    public void profileTest() {
        Response response = getRequestSpecification()
                .when()
                .get();

        response.then()
                .body("code", Matchers.equalTo(200))
                .body("data.userName",
                        Matchers.equalTo(AppConfigLoader.getAppConfig().getDatakit().getUsername()));

        userProfile.put("userId", response.jsonPath().getInt("data.userId"));
        userProfile.put("userName", AppConfigLoader.getAppConfig().getDatakit().getUsername());
        userProfile.put("nickName", response.jsonPath().getString("data.nickName"));
        userProfile.put("phonenumber", response.jsonPath().getString("data.phonenumber"));
        userProfile.put("email", response.jsonPath().getString("data.email"));
    }

    @Test(dependsOnMethods = "profileTest")
    public void updateProfileTest() {
        userProfile.put("phonenumber", "phoneNumber");
        userProfile.put("email", "example@example.com");

        getRequestSpecification()
                .contentType(ContentType.JSON)
                .body(userProfile)
                .when()
                .put()
                .then()
                .body("code", Matchers.equalTo(200));
    }

    @Test(priority = 1)
    public void updatePwdTest() {
        String newPassword = "newPassword";
        String encryptedNewPassword = RsaUtils.encryptByPublicKey(newPassword);
        String encryptedOldPassword = RsaUtils.encryptByPublicKey(
                AppConfigLoader.getAppConfig().getDatakit().getPassword());

        HashMap<String, String> requestMap = new HashMap<>();
        requestMap.put("oldPassword", encryptedOldPassword);
        requestMap.put("newPassword", encryptedNewPassword);

        getRequestSpecification()
                .contentType(ContentType.JSON)
                .body(requestMap)
                .when()
                .put("/updatePwd")
                .then()
                .body("code", Matchers.equalTo(200));

        requestMap.put("oldPassword", encryptedNewPassword);
        requestMap.put("newPassword", encryptedOldPassword);

        getRequestSpecification()
                .contentType(ContentType.JSON)
                .body(requestMap)
                .when()
                .put("/updatePwd")
                .then()
                .body("code", Matchers.equalTo(200));
    }

    @Test(priority = 1)
    public void avatarTest() {
        getRequestSpecification()
                .multiPart("avatarfile", new File("src/test/resources/images/default-avatar.png"))
                .when()
                .post("/avatar")
                .then()
                .body("code", Matchers.equalTo(200));
    }
}
