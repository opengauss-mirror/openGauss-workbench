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
 * -------------------------------------------------------------------------
 *
 * EncryptionUtils.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/service/ops/impl/EncryptionUtils.java
 *
 * -------------------------------------------------------------------------
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Objects;

/**
 * @author lhf
 * @date 2022/11/28 14:20
 **/
@Slf4j
@Component
public class EncryptionUtils {
    private static final String GCM_ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;
    private static final byte[] IV = new byte[GCM_IV_LENGTH];

    private static String publicKey;
    private static String privateKey;

    @Autowired
    private OpsEncryptionMapper encryptionMapper;

    @Autowired
    private IEncryptionService encryptionService;

    public String encrypt(String plainText) {
        RSA rsa = new RSA(null, publicKey);
        byte[] encrypt = rsa.encrypt(StrUtil.bytes(plainText, CharsetUtil.CHARSET_UTF_8), KeyType.PublicKey);
        return StrUtil.str(Base64.encodeBase64(encrypt), CharsetUtil.CHARSET_UTF_8);
    }

    public String decrypt(String cipherText) {
        try {
            RSA rsa = new RSA(privateKey, null);
            byte[] decrypt = rsa.decrypt(Base64.decodeBase64(cipherText), KeyType.PrivateKey);
            return StrUtil.str(decrypt, CharsetUtil.CHARSET_UTF_8);
        } catch (CryptoException e) {
            return cipherText;
        }
    }

    public String decrypt(String cipherText, String keyStr) {
        log.info("decrypt, cipherText:{},key:{}", cipherText, keyStr);
        if (StrUtil.isEmpty(cipherText) || StrUtil.isEmpty(keyStr)) {
            throw new RuntimeException("cipherText or key is empty");
        }

        byte[] decryptedText;
        try {
            SecretKey key = new SecretKeySpec(Base64.decodeBase64(keyStr), "AES");
            // Get Cipher Instance
            Cipher cipher = Cipher.getInstance(GCM_ALGORITHM);

            // Create SecretKeySpec
            SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), "AES");

            // Create GCMParameterSpec
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, IV);

            // Initialize Cipher for DECRYPT_MODE
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);

            // Perform Decryption
            decryptedText = cipher.doFinal(Base64.decodeBase64(cipherText));
        } catch (Exception e) {
            log.error("decrypt fail", e);
            throw new RuntimeException("decrypt fail");
        }

        return new String(decryptedText);
    }

    public String encrypt(String plainText, String keyStr) {
        log.info("encrypt, plainText:{},key:{}", plainText, keyStr);
        if (StrUtil.isEmpty(plainText) || StrUtil.isEmpty(keyStr)) {
            throw new RuntimeException("cipherText or key is empty");
        }
        byte[] cipherText = null;
        try {
            SecretKey key = new SecretKeySpec(Base64.decodeBase64(keyStr), "AES");
            // Get Cipher Instance
            Cipher cipher = Cipher.getInstance(GCM_ALGORITHM);

            // Create SecretKeySpec
            SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), "AES");

            // Create GCMParameterSpec
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, IV);

            // Initialize Cipher for ENCRYPT_MODE
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);

            // Perform Encryption
            cipherText = cipher.doFinal(plainText.getBytes());
        } catch (Exception e) {
            log.error("encrypt fail", e);
            throw new RuntimeException("encrypt fail");
        }

        return Base64.encodeBase64String(cipherText);
    }

    public static String generateSecretKey(String identity) {
        KeyGenerator keyGener = null;
        try {
            keyGener = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            log.error("NoSuchAlgorithmException", e);
        }

        if (StrUtil.isEmpty(identity)) {
            keyGener.init(256);
        } else {
            try {
                SecureRandom instanceStrong = SecureRandom.getInstanceStrong();
                instanceStrong.setSeed(identity.getBytes(StandardCharsets.UTF_8));
                keyGener.init(256, instanceStrong);
            } catch (NoSuchAlgorithmException e) {
                log.error("NoSuchAlgorithmException", e);
            }
        }

        return Base64.encodeBase64String(keyGener.generateKey().getEncoded());
    }

    public String getKey() {
        if (StrUtil.isEmpty(publicKey)) {
            refreshKeyPair();
        }

        return publicKey;
    }

    public static synchronized void generateKeyPair() {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(512);
            KeyPair keyPair = gen.generateKeyPair();
            PublicKey pubKey = keyPair.getPublic();
            PrivateKey priKey = keyPair.getPrivate();
            publicKey = Base64.encodeBase64String(pubKey.getEncoded());
            privateKey = Base64.encodeBase64String(priKey.getEncoded());
        } catch (Exception e) {
            log.error("gen key pair fail", e);
        }
    }

    public void refreshKeyPair() {
        this.refreshKeyPair(true);
    }

    /**
     * 刷新公钥和私钥
     *
     * @param isEnforce 是否强制
     */
    public void refreshKeyPair(boolean isEnforce) {
        OpsEncryptionEntity opsEncryptionEntity = encryptionMapper.selectOne(null);
        if (Objects.isNull(opsEncryptionEntity)) {
            opsEncryptionEntity = new OpsEncryptionEntity();
            log.info("encryption key not found, generate it.");
            generateKeyPair();
            saveKeyPair(opsEncryptionEntity);
        } else if (StrUtil.isEmpty(publicKey) || isEnforce) {
            log.info("refresh encryption key.");
            publicKey = opsEncryptionEntity.getPublicKey();
            privateKey = opsEncryptionEntity.getPrivateKey();
        } else {
            log.info("dont refresh encryption key.");
        }
    }

    private void saveKeyPair(OpsEncryptionEntity opsEncryptionEntity) {
        opsEncryptionEntity.setEncryptionId("1");
        opsEncryptionEntity.setPublicKey(publicKey);
        opsEncryptionEntity.setPrivateKey(privateKey);
        encryptionService.save(opsEncryptionEntity);
    }
}
