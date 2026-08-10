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
    private static final String PUB_KEY_JSON_MSG_FIELD = "msg";

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
            return doEncryptByPublicKey(plainText);
        } catch (ApiTestException e) {
            // the Datakit service may have regenerated its RSA key pair after a restart,
            // clear the cached public key and retry once with the latest one
            loginPubKey = null;
            return doEncryptByPublicKey(plainText);
        }
    }

    private static String doEncryptByPublicKey(String plainText) {
        try {
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(Base64.decodeBase64(getLoginPubKey()));
            KeyFactory keyFactory = KeyFactory.getInstance(ENCRYPTION_ALGORITHM);
            PublicKey tempPublicKey = keyFactory.generatePublic(x509EncodedKeySpec);
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, tempPublicKey);
            byte[] result = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeBase64String(result);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException e) {
            logger.error("Encryption algorithm error.", e);
            throw new ApiTestException("Encryption algorithm error.", e);
        } catch (InvalidKeySpecException | BadPaddingException | InvalidKeyException e) {
            logger.error("Encryption failed.", e);
            throw new ApiTestException("Encryption failed.", e);
        }
    }

    /**
     * Get the login public key from the Datakit server, the key is fetched only once and then cached
     *
     * @return base64 encoded X509 RSA public key
     */
    public static String getLoginPubKey() {
        synchronized (RsaUtils.class) {
            if (StrUtil.isNotEmpty(loginPubKey)) {
                return loginPubKey;
            }
            Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .when()
                .get(RestAssured.baseURI + "/pubKey");
            response.then().body("code", Matchers.equalTo(200));
            loginPubKey = resolvePublicKey(response);
            logger.info("Login public key obtained successfully.");
            return loginPubKey;
        }
    }

    /**
     * Resolve and validate the public key from the /pubKey response, the key is carried
     * in the "msg" field, consistent with the frontend usage
     *
     * @param response response of the /pubKey request
     * @return base64 encoded X509 RSA public key
     */
    public static String resolvePublicKey(Response response) {
        if (response == null) {
            throw new ApiTestException("Response is null when resolving public key.");
        }
        String key = response.jsonPath().getString(PUB_KEY_JSON_MSG_FIELD);
        if (isValidPublicKey(key)) {
            return key;
        }
        throw new ApiTestException("Invalid login public key from /pubKey, response: " + response.asString());
    }

    /**
     * Check whether the given base64 string is a valid X509 RSA public key
     *
     * @param base64Key base64 encoded X509 RSA public key
     * @return true if the key is valid
     */
    public static boolean isValidPublicKey(String base64Key) {
        if (StrUtil.isEmpty(base64Key)) {
            return false;
        }
        try {
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.decodeBase64(base64Key));
            KeyFactory.getInstance(ENCRYPTION_ALGORITHM).generatePublic(keySpec);
            return true;
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            return false;
        }
    }
}
