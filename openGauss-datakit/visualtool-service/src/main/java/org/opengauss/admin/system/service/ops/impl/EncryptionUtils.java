/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
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

package org.opengauss.admin.system.service.ops.impl;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.CryptoException;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.codec.binary.Base64;
import org.opengauss.admin.common.core.domain.entity.ops.OpsEncryptionEntity;
import org.opengauss.admin.system.mapper.ops.OpsEncryptionMapper;
import org.opengauss.admin.system.service.ops.IEncryptionService;
import org.opengauss.tool.cipher.AesGcmUtils;
import org.opengauss.tool.cipher.SecureKeyManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.*;
import java.util.Objects;

/**
 * @author lhf
 * @date 2022/11/28 14:20
 **/
@Slf4j
@Component
public class EncryptionUtils {
    private static final String SECRET = "secret";

    @Autowired
    private OpsEncryptionMapper encryptionMapper;

    @Autowired
    private IEncryptionService encryptionService;

    /**
     * encrypt plain text
     *
     * @param plainText plainText
     * @return encrypt result
     */
    public String encrypt(String plainText) {
        if (StrUtil.isEmpty(plainText)) {
            return "";
        }
        return useKeySecurely(KeyType.PublicKey, (keyType, publicKeyChars) -> {
            String publicKeyStr = new String(publicKeyChars);
            RSA rsa = new RSA(null, publicKeyStr);
            byte[] encrypt = rsa.encrypt(StrUtil.bytes(plainText, CharsetUtil.CHARSET_UTF_8), KeyType.PublicKey);
            return StrUtil.str(Base64.encodeBase64(encrypt), CharsetUtil.CHARSET_UTF_8);
        });
    }

    /**
     * decrypt cipher text
     *
     * @param cipherText cipherText
     * @return decrypt result
     */
    public String decrypt(String cipherText) {
        if (StrUtil.isEmpty(cipherText)) {
            return "";
        }
        return useKeySecurely(KeyType.PrivateKey, (keyType, privateKeyChars) -> {
            try {
                String privateKeyStr = new String(privateKeyChars);
                RSA rsa = new RSA(privateKeyStr, null);
                byte[] decrypt = rsa.decrypt(Base64.decodeBase64(cipherText), KeyType.PrivateKey);
                return StrUtil.str(decrypt, CharsetUtil.CHARSET_UTF_8);
            } catch (CryptoException e) {
                log.warn("Decryption failed, returning original text", e);
                return cipherText;
            }
        });
    }

    private String useKeySecurely(KeyType keyType, KeyOperation operation) {
        char[] keyChars = null;
        String keyString = null;
        try {
            String encryptedKey = encryptionService.getEncryptedKey(keyType.name());
            String decryptedKey = AesGcmUtils.decrypt(encryptedKey);
            keyChars = decryptedKey.toCharArray();
            return operation.execute(keyType, keyChars);
        } finally {
            SecureKeyManager.secureWipe(keyChars);
            if (keyString != null) {
                SecureKeyManager.secureWipe(keyString.toCharArray());
            }
        }
    }

    /**
     * KeyOperation  secure operation
     */
    @FunctionalInterface
    public interface KeyOperation {
        /**
         * secure operation
         *
         * @param keyType key type
         * @param key key
         * @return result
         */
        String execute(KeyType keyType, char[] key);
    }


    /**
     * get public key
     *
     * @return key public key
     */
    public String getKey() {
        return useKeySecurely(KeyType.PublicKey, (keyType, publicKeyChars) -> new String(publicKeyChars));
    }

    private KeyPair generateKeyPairSecurely() {
        char[] publicKeyChars = null;
        char[] privateKeyChars = null;
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(4096);
            KeyPair keyPair = gen.generateKeyPair();
            PublicKey pubKey = keyPair.getPublic();
            PrivateKey priKey = keyPair.getPrivate();
            String publicKeyStr = Base64.encodeBase64String(pubKey.getEncoded());
            String privateKeyStr = Base64.encodeBase64String(priKey.getEncoded());
            saveKeyPairSecurely(publicKeyStr, privateKeyStr);
            publicKeyChars = publicKeyStr.toCharArray();
            privateKeyChars = privateKeyStr.toCharArray();
            return keyPair;
        } catch (NoSuchAlgorithmException e) {
            log.error("Generate key pair securely failed", e);
            throw new CryptoException("Generate key pair securely failed", e);
        } finally {
            SecureKeyManager.secureWipe(publicKeyChars);
            SecureKeyManager.secureWipe(privateKeyChars);
        }
    }

    private void saveKeyPairSecurely(String publicKey, String privateKey) {
        OpsEncryptionEntity opsEncryptionEntity = new OpsEncryptionEntity();
        opsEncryptionEntity.setEncryptionId("1");
        opsEncryptionEntity.setPublicKey(AesGcmUtils.encrypt(publicKey));
        opsEncryptionEntity.setPrivateKey(AesGcmUtils.encrypt(privateKey));
        opsEncryptionEntity.setKeySecurity(SECRET);
        encryptionService.save(opsEncryptionEntity);
    }

    /**
     * refresh key pair ,first init key pair for current env
     */
    public void refreshKeyPair() {
        OpsEncryptionEntity opsEncryptionEntity = encryptionMapper.selectOne(null);
        if (Objects.isNull(opsEncryptionEntity)) {
            log.info("Encryption key not found, generate it securely.");
            generateKeyPairSecurely();
        } else {
            log.info("Encryption key exists, no need to refresh.");
        }
    }
}
