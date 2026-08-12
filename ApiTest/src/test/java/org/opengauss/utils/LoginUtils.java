/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package org.opengauss.utils;

import cn.hutool.core.util.StrUtil;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import org.opengauss.exception.ApiTestException;
import org.opengauss.global.AppConfigLoader;
import org.opengauss.visualtool.api.model.LoginBody;

/**
 * LoginUtils is used to log in to DataKit.
 *
 * @since 2024/11/5
 */
public class LoginUtils {
    private static final String LOGIN_PATH = "/login";

    /**
     * log in to DataKit and get token
     *
     * @return token
     */
    public static String login() {
        String username = AppConfigLoader.getAppConfig().getDatakit().getUsername();
        String encryptedPassword = RsaUtils.encryptByPublicKey(
            AppConfigLoader.getAppConfig().getDatakit().getPassword());
        Response response = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(new LoginBody(username, encryptedPassword))
            .when()
            .post(RestAssured.baseURI + LOGIN_PATH);
        if (response.statusCode() != 200 || response.jsonPath().getInt("code") != 200) {
            throw new ApiTestException("Login failed, response: " + response.asString());
        }
        String token = response.jsonPath().getString("token");
        if (StrUtil.isEmpty(token)) {
            throw new ApiTestException("Login succeeded but no token in response: " + response.asString());
        }
        return token;
    }
}
