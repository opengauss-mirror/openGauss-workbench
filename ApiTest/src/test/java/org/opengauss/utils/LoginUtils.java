/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package org.opengauss.utils;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import org.hamcrest.Matchers;
import org.opengauss.global.AppConfigLoader;
import org.opengauss.visualtool.api.model.LoginBody;

/**
 * LoginUtils is used to log in to DataKit.
 *
 * @since 2024/11/5
 */
public class LoginUtils {
    /**
     * log in to DataKit and get token
     *
     * @return token
     */
    public static String login() {
        LoginBody body = new LoginBody(AppConfigLoader.getAppConfig().getDatakit().getUsername(),
            RsaUtils.encryptByPublicKey(AppConfigLoader.getAppConfig().getDatakit().getPassword()));
        Response response = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(body)
            .when()
            .post(RestAssured.baseURI + "/login");
        response.then().body("code", Matchers.equalTo(200));
        return response.jsonPath().getString("token");
    }
}
