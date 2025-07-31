/**
 * Copyright ruoyi.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengauss.admin.common.utils;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.CryptoException;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.codec.binary.Base64;
import org.opengauss.admin.common.exception.SecureException;

import java.security.*;
import java.util.concurrent.atomic.AtomicReference;

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
            log.info("RSA key pair generation success");
        } catch (NoSuchAlgorithmException e) {
            log.error("RSA key pair generation failed", e);
            throw new SecureException("Critical failure: RSA key generation failed: " + e.getMessage());
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
     * encrypt with default public key
     *
     * @param plainText plain text
     * @return cipher text
     */
    public static String encrypt(String plainText) {
        return encrypt(plainText, PUBLIC_KEY_CACHE.get());
    }

    /**
     * encrypt with public key
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
     * decrypt with default private key
     *
     * @param cipherText cipher text
     * @return plain text
     */
    public static String decrypt(String cipherText) {
        return decrypt(cipherText, PRIVATE_KEY_CACHE.get());
    }

    /**
     * decrypt with private key
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