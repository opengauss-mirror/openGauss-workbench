/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package org.opengauss.utils;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.codec.binary.Base64;
import org.hamcrest.Matchers;

import static org.opengauss.global.Constants.getRequestSpecification;

/**
 * EncryptionUtils
 *
 * @since 2024/11/4
 */
public class EncryptionUtils {
    private static String publicKey;

    /**
     * encrypt password
     *
     * @param plainText plain text
     * @return cipher text
     */
    public static String encrypt(String plainText) {
        RSA rsa = new RSA(null, publicKey);
        byte[] encrypt = rsa.encrypt(StrUtil.bytes(plainText, CharsetUtil.CHARSET_UTF_8), KeyType.PublicKey);
        return StrUtil.str(Base64.encodeBase64(encrypt), CharsetUtil.CHARSET_UTF_8);
    }

    /**
     * get encryption public key
     */
    public static void getEncryptionKey() {
        if (ObjectUtil.isEmpty(publicKey)) {
            RestAssured.basePath = "/encryption";

            Response response = getRequestSpecification()
                    .when()
                    .get("/getKey");
            response.then()
                    .body("code", Matchers.equalTo(200))
                    .body("key", Matchers.notNullValue());

            publicKey = response.jsonPath().getString("key");
        }
    }
}
