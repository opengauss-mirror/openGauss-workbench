/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package org.opengauss.utils;

import cn.hutool.core.util.StrUtil;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hamcrest.Matchers;
import org.opengauss.exception.ApiTestException;
import org.opengauss.global.AppConfigLoader;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

/**
 * RSA encryption and decryption tool class
 *
 * @since 2024/10/24
 */
public class RsaUtils {
    private static final Logger logger = LogManager.getLogger(RsaUtils.class);
    private static final String ENCRYPTION_ALGORITHM = "RSA";

    private static String loginPubKey;

    static {
        AppConfigLoader.loadConfig();
    }

    /**
     * RSA encryption
     *
     * @param plainText plain text
     * @return encrypted text
     */
    public static String encryptByPublicKey(String plainText) {
        try {
            getLoginPubKey();
            X509EncodedKeySpec x509EncodedKeySpec2 = new X509EncodedKeySpec(Base64.decodeBase64(loginPubKey));
            KeyFactory keyFactory = KeyFactory.getInstance(ENCRYPTION_ALGORITHM);
            PublicKey tempPublicKey = keyFactory.generatePublic(x509EncodedKeySpec2);
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, tempPublicKey);
            byte[] result = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeBase64String(result);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException e) {
            logger.error("Encryption Algorithm  error. ", e);
            throw new ApiTestException("Encryption Algorithm error. ", e);
        } catch (InvalidKeySpecException | BadPaddingException | InvalidKeyException e) {
            logger.error("Encryption failed. ", e);
            throw new ApiTestException("Encryption failed. ", e);
        }
    }

    private static void getLoginPubKey() {
        synchronized (RsaUtils.class) {
            if (StrUtil.isEmpty(loginPubKey)) {
                Response response = RestAssured.given()
                    .contentType(ContentType.JSON)
                    .when()
                    .get(RestAssured.baseURI + "/pubKey");
                response.then().body("code", Matchers.equalTo(200));
                loginPubKey = response.jsonPath().getString("msg");
                logger.warn("Login public key: {}", loginPubKey);
            }
        }
    }
}