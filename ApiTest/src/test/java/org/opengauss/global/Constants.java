/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package org.opengauss.global;

import cn.hutool.core.util.ObjectUtil;
import io.restassured.RestAssured;
import io.restassured.config.SSLConfig;
import io.restassured.http.Header;
import io.restassured.specification.RequestSpecification;

import org.opengauss.exception.ApiTestException;
import org.opengauss.utils.LoginUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * constants
 *
 * @since 2024/10/21
 */
public class Constants {
    /**
     * page params
     */
    public static final Map<String, Integer> PAGE_PARAMS = new HashMap<>();
    private static final String TOKEN_HEADER_NAME = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";
    private static String token;
    private static Header tokenHeader;

    static {
        PAGE_PARAMS.put("pageNum", 1);
        PAGE_PARAMS.put("pageSize", 10);
    }

    /**
     * load token
     */
    public static void loadToken() {
        token = LoginUtils.login();
        tokenHeader = new Header(TOKEN_HEADER_NAME, TOKEN_PREFIX + token);
    }

    /**
     * get RequestSpecification with Authorization Header
     *
     * @return RequestSpecification
     */
    public static RequestSpecification getRequestSpecification() {
        if (ObjectUtil.isEmpty(token)) {
            throw new ApiTestException("Token is not loaded, call loadToken() before sending requests.");
        }
        return getRequestSpecificationWithoutToken().header(tokenHeader);
    }

    /**
     * get RequestSpecification without Authorization Header
     *
     * @return RequestSpecification
     */
    public static RequestSpecification getRequestSpecificationWithoutToken() {
        RestAssured.config = RestAssured.config().sslConfig(new SSLConfig().relaxedHTTPSValidation());
        return RestAssured.given();
    }
}
