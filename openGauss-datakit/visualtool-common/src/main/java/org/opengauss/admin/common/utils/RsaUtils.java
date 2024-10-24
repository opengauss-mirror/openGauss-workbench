/**
 Copyright ruoyi.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package org.opengauss.admin.common.utils;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.codec.binary.Base64;
import org.opengauss.admin.common.exception.ServiceException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * Description: RSA encrypt and decode
 *
 * @author xielibo
 * @version 1.0
 * @date: 2021/9/28 11:40 PM
 * @since JDK 1.8
 */
@Slf4j
public class RsaUtils {
    // Rsa
    public static String privateKey = "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAPFEkDAkYwcE4odb\n" +
            "eccUdpfKUfK35mtc0M6xttmoLCB9MasT0nDoD5ZBj+OIMSpOG4wouFvvPz//Dp+Y\n" +
            "+7SQt0CZzWeqSajM45W37UGBGqxVyQmPDt6JF+WqwNkQiC2dQxfrV7IxwgY8fPF0\n" +
            "vdeaVo9/VCmA9cFZBo2XgjuonYuhAgMBAAECgYEAq7rZxuqfcgeQFjiOXZ27LB/e\n" +
            "ZJ1xbUoLdpQYSqThg96Y0+SwDZ2gOptAB/yQwkQGZ6U0VHve0XaCuibyQnwfcoI7\n" +
            "ZzqxqGnna2+WL2lOePWGuHn7Yub2Aw+94FRPti6F5Nms37XJ6NQ2XikmeXZJvU8G\n" +
            "bO2UiceBHone54i9eAECQQD4qxfelo/+UmxbYtokak+nenV3+jr+SEkOhNN9jLVf\n" +
            "OvrPQ4mTEgTLCxd8Wp7aN9Q9vc83H6woPz6i1TwaLV9RAkEA+GGcuYTOSJM9V8pJ\n" +
            "3KJ55gWfSBlw/yZPQ9od+OzsjUE2nq54mNbpWb3KhNpF5TNj4m2tMyFWJSwTwftF\n" +
            "JSFzUQJAfUQDFDgYdW8j8q1LYojDc4S25CeDzFCxrMSwnVBilYRqkDpfdVzgWUBP\n" +
            "Jm+oEhmJq0iQuB4WZXfmn7R3QGCW4QJBAItnxd3+IwVq1oAigmg6LO6kcyy1Us1y\n" +
            "BAUSM8ZVu8LgOja/t+IYpSoAMt1z4Mzulf4tDovnBwGgBorWa42Wg7ECQQD0myzE\n" +
            "TM2NJQxFKSo4FEogYH6OOK236xooe95pzwxiSy/3tdDU3C2ohAcRJvUU839BXvyO\n" +
            "zexW0T/Mnc5U6X9+";

    /**
     * private key decryption
     *
     * @param text             text
     * @return
     */
    public static String decryptByPrivateKey(String text) throws ServiceException {
        return decryptByPrivateKey(privateKey, text);
    }

    /**
     * public key decryption
     *
     * @param publicKeyString publicKeyString
     * @param text            text
     */
    public static String decryptByPublicKey(String publicKeyString, String text) throws Exception {
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(Base64.decodeBase64(publicKeyString));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(x509EncodedKeySpec);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        byte[] result = cipher.doFinal(Base64.decodeBase64(text));
        return new String(result);
    }

    /**
     * private key encryption
     *
     * @param privateKeyString privateKeyString
     * @param text             text
     */
    public static String encryptByPrivateKey(String privateKeyString, String text) throws Exception {
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(privateKeyString));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey tempPrivateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, tempPrivateKey);
        byte[] result = cipher.doFinal(text.getBytes());
        return Base64.encodeBase64String(result);
    }

    /**
     * private key decryption
     *
     * @param privateKeyString privateKeyString
     * @param text             text
     */
    public static String decryptByPrivateKey(String privateKeyString, String text) throws ServiceException {
        try {
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec5 = new PKCS8EncodedKeySpec(Base64.decodeBase64(privateKeyString));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey tempPrivateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec5);
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, tempPrivateKey);
            byte[] result = cipher.doFinal(Base64.decodeBase64(text));
            return new String(result, StandardCharsets.UTF_8);
        } catch (InvalidKeyException | InvalidKeySpecException e) {
            log.error("Invalid params", e);
            throw new ServiceException("Invalid params");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
            | BadPaddingException e) {
            log.error("decrypt failed", e);
            throw new ServiceException("decrypt failed");
        }
    }

    /**
     * public key encryption
     *
     * @param publicKeyString publicKeyString
     * @param text            text
     */
    public static String encryptByPublicKey(String publicKeyString, String text) throws Exception {
        X509EncodedKeySpec x509EncodedKeySpec2 = new X509EncodedKeySpec(Base64.decodeBase64(publicKeyString));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(x509EncodedKeySpec2);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] result = cipher.doFinal(text.getBytes());
        return Base64.encodeBase64String(result);
    }

    /**
     * Build an RSA key pair
     *
     * @return RsaKeyPair
     */
    public static RsaKeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(1024);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        RSAPublicKey rsaPublicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) keyPair.getPrivate();
        String publicKeyString = Base64.encodeBase64String(rsaPublicKey.getEncoded());
        String privateKeyString = Base64.encodeBase64String(rsaPrivateKey.getEncoded());
        return new RsaKeyPair(publicKeyString, privateKeyString);
    }

    /**
     * RSA Model
     */
    public static class RsaKeyPair {
        private final String publicKey;
        private final String privateKey;

        public RsaKeyPair(String publicKey, String privateKey) {
            this.publicKey = publicKey;
            this.privateKey = privateKey;
        }

        public String getPublicKey() {
            return publicKey;
        }

        public String getPrivateKey() {
            return privateKey;
        }
    }
}