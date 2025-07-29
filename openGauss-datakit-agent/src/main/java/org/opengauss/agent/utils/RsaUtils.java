/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.agent.utils;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.CryptoException;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.codec.binary.Base64;
import org.opengauss.agent.exception.AgentException;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * RsaUtils
 *
 * @author wangchao
 * @since 2024/10/29 09:26
 */
@Slf4j
public class RsaUtils {
    private static final AtomicReference<String> PUBLIC_KEY_CACHE = new AtomicReference<>();
    private static final AtomicReference<String> PRIVATE_KEY_CACHE = new AtomicReference<>();

    static {
        initRsaKeyPair();
    }

    private static void initRsaKeyPair() {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(4096);
            KeyPair keyPair = gen.generateKeyPair();
            PUBLIC_KEY_CACHE.set(Base64.encodeBase64String(keyPair.getPublic().getEncoded()));
            PRIVATE_KEY_CACHE.set(Base64.encodeBase64String(keyPair.getPrivate().getEncoded()));
        } catch (NoSuchAlgorithmException e) {
            log.error("RSA key pair generation failed", e);
            throw new AgentException("Critical failure: RSA key generation failed", e);
        }
    }

    /**
     * get public key
     *
     * @return public key
     */
    public static String publicKey() {
        if (StrUtil.isEmpty(PUBLIC_KEY_CACHE.get())) {
            initRsaKeyPair();
        }
        return PUBLIC_KEY_CACHE.get();
    }

    /**
     * encrypt
     *
     * @param plainText plain text
     * @return cipher text
     */
    public static String encrypt(String plainText) {
        return encrypt(plainText, PUBLIC_KEY_CACHE.get());
    }

    /**
     * encrypt
     *
     * @param plainText plain text
     * @param publicKey public key
     * @return cipher text
     */
    public static String encrypt(String plainText, String publicKey) {
        RSA rsa = new RSA(null, publicKey);
        byte[] encrypt = rsa.encrypt(StrUtil.bytes(plainText, CharsetUtil.CHARSET_UTF_8), KeyType.PublicKey);
        return Base64.encodeBase64String(encrypt);
    }

    /**
     * decrypt
     *
     * @param cipherText cipher text
     * @return plain text
     */
    public static String decrypt(String cipherText) {
        return decrypt(cipherText, PRIVATE_KEY_CACHE.get());
    }

    /**
     * decrypt
     *
     * @param cipherText cipher text
     * @param privateKey private key
     * @return plain text
     */
    public static String decrypt(String cipherText, String privateKey) {
        try {
            RSA rsa = new RSA(privateKey, null);
            byte[] decrypt = rsa.decrypt(Base64.decodeBase64(cipherText), KeyType.PrivateKey);
            return StrUtil.str(decrypt, CharsetUtil.CHARSET_UTF_8);
        } catch (CryptoException e) {
            log.error("RSA decryption failed for cipherText", e);
            throw new SecurityException("Decryption failed. Possible tampering detected", e);
        }
    }
}