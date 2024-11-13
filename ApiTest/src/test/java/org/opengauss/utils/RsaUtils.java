/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package org.opengauss.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opengauss.exception.ApiTestException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * RSA encryption and decryption tool class
 *
 * @since 2024/10/24
 */
public class RsaUtils {
    private static final Logger logger = LogManager.getLogger(RsaUtils.class);
    private static final String ENCRYPTION_ALGORITHM = "RSA";
    private static final String PRIVATE_KEY =
            "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAPFEkDAkYwcE4odb\n"
            + "eccUdpfKUfK35mtc0M6xttmoLCB9MasT0nDoD5ZBj+OIMSpOG4wouFvvPz//Dp+Y\n"
            + "+7SQt0CZzWeqSajM45W37UGBGqxVyQmPDt6JF+WqwNkQiC2dQxfrV7IxwgY8fPF0\n"
            + "vdeaVo9/VCmA9cFZBo2XgjuonYuhAgMBAAECgYEAq7rZxuqfcgeQFjiOXZ27LB/e\n"
            + "ZJ1xbUoLdpQYSqThg96Y0+SwDZ2gOptAB/yQwkQGZ6U0VHve0XaCuibyQnwfcoI7\n"
            + "ZzqxqGnna2+WL2lOePWGuHn7Yub2Aw+94FRPti6F5Nms37XJ6NQ2XikmeXZJvU8G\n"
            + "bO2UiceBHone54i9eAECQQD4qxfelo/+UmxbYtokak+nenV3+jr+SEkOhNN9jLVf\n"
            + "OvrPQ4mTEgTLCxd8Wp7aN9Q9vc83H6woPz6i1TwaLV9RAkEA+GGcuYTOSJM9V8pJ\n"
            + "3KJ55gWfSBlw/yZPQ9od+OzsjUE2nq54mNbpWb3KhNpF5TNj4m2tMyFWJSwTwftF\n"
            + "JSFzUQJAfUQDFDgYdW8j8q1LYojDc4S25CeDzFCxrMSwnVBilYRqkDpfdVzgWUBP\n"
            + "Jm+oEhmJq0iQuB4WZXfmn7R3QGCW4QJBAItnxd3+IwVq1oAigmg6LO6kcyy1Us1y\n"
            + "BAUSM8ZVu8LgOja/t+IYpSoAMt1z4Mzulf4tDovnBwGgBorWa42Wg7ECQQD0myzE\n"
            + "TM2NJQxFKSo4FEogYH6OOK236xooe95pzwxiSy/3tdDU3C2ohAcRJvUU839BXvyO\n"
            + "zexW0T/Mnc5U6X9+";
    private static final String PUBLIC_KEY =
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDxRJAwJGMHBOKHW3nHFHaXylHy"
            + "t+ZrXNDOsbbZqCwgfTGrE9Jw6A+WQY/jiDEqThuMKLhb7z8//w6fmPu0kLdAmc1n"
            + "qkmozOOVt+1BgRqsVckJjw7eiRflqsDZEIgtnUMX61eyMcIGPHzxdL3XmlaPf1Qp"
            + "gPXBWQaNl4I7qJ2LoQIDAQAB";

    /**
     * encryption method
     *
     * @param plainText plain text
     * @return encrypted password
     */
    public static String encryptByPublicKey(String plainText) {
        try {
            X509EncodedKeySpec x509EncodedKeySpec2 = new X509EncodedKeySpec(Base64.decodeBase64(PUBLIC_KEY));
            KeyFactory keyFactory = KeyFactory.getInstance(ENCRYPTION_ALGORITHM);
            PublicKey tempPublicKey = keyFactory.generatePublic(x509EncodedKeySpec2);
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, tempPublicKey);
            byte[] result = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeBase64String(result);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
                | InvalidKeySpecException | BadPaddingException | InvalidKeyException e) {
            logger.error("Encryption failed. ", e);
            throw new ApiTestException("Encryption failed. ", e);
        }
    }

    /**
     * decryption method
     *
     * @param cipherText cipher text
     * @return decrypted password
     */
    public static String decryptByPrivateKey(String cipherText) {
        return decryptByPrivateKey(PRIVATE_KEY, cipherText);
    }

    private static String decryptByPrivateKey(String privateKeyString, String cipherText) {
        try {
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec5 = new PKCS8EncodedKeySpec(Base64.decodeBase64(privateKeyString));
            KeyFactory keyFactory = KeyFactory.getInstance(ENCRYPTION_ALGORITHM);
            PrivateKey tempPrivateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec5);
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, tempPrivateKey);
            byte[] result = cipher.doFinal(Base64.decodeBase64(cipherText));
            return new String(result, StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
                | InvalidKeySpecException | BadPaddingException | InvalidKeyException e) {
            logger.error("Decryption failed. ", e);
            throw new ApiTestException("Decryption failed. ", e);
        }
    }
}