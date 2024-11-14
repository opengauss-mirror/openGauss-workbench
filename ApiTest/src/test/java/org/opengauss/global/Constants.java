/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package org.opengauss.global;

import cn.hutool.core.util.ObjectUtil;
import io.restassured.RestAssured;
import io.restassured.config.SSLConfig;
import io.restassured.http.Header;
import io.restassured.specification.RequestSpecification;
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
    private static final String TOKEN;
    private static final Header TOKEN_HEADER;

    static {
        TOKEN = LoginUtils.login();
        TOKEN_HEADER = new Header("Authorization", "Bearer " + TOKEN);

        PAGE_PARAMS.put("pageNum", 1);
        PAGE_PARAMS.put("pageSize", 10);
    }

    /**
     * get RequestSpecification with Authorization Header
     *
     * @return RequestSpecification
     */
    public static RequestSpecification getRequestSpecification() {
        RestAssured.config = RestAssured.config().sslConfig(new SSLConfig().relaxedHTTPSValidation());
        if (ObjectUtil.isEmpty(TOKEN)) {
            return RestAssured.given();
        }
        return RestAssured.given().header(TOKEN_HEADER);
    }
}
