package org.opengauss.admin.system.service.ops.impl;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.opengauss.admin.common.core.domain.entity.ops.OpsEncryptionEntity;
import org.opengauss.admin.system.mapper.ops.OpsEncryptionMapper;
import org.opengauss.admin.system.service.ops.IEncryptionService;
import org.opengauss.admin.system.service.ops.IHostUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
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
public class EncryptionUtils implements CommandLineRunner {

    private static String gcm256algorithm = "AES/GCM/PKCS5Padding";

    private static String rootKey = generateSecretKey(null);
    private static String workKey;

    public static final int AES_KEY_SIZE = 256;
    public static final int GCM_IV_LENGTH = 12;
    public static final int GCM_TAG_LENGTH = 16;
    public static byte[] IV = new byte[GCM_IV_LENGTH];

    private static String publicKey;
    private static String privateKey;

    @Autowired
    private OpsEncryptionMapper encryptionMapper;
    @Autowired
    private IHostUserService hostUserService;
    @Autowired
    private IEncryptionService encryptionService;

    public String encrypt(String plainText) {
        RSA rsa = new RSA(null, publicKey);
        byte[] encrypt = rsa.encrypt(StrUtil.bytes(plainText, CharsetUtil.CHARSET_UTF_8), KeyType.PublicKey);
        return StrUtil.str(Base64.encodeBase64(encrypt), CharsetUtil.CHARSET_UTF_8);
    }

    public String decrypt(String cipherText) {
        RSA rsa = new RSA(privateKey, null);
        byte[] decrypt = rsa.decrypt(Base64.decodeBase64(cipherText), KeyType.PrivateKey);
        return StrUtil.str(decrypt, CharsetUtil.CHARSET_UTF_8);
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
            Cipher cipher = Cipher.getInstance(gcm256algorithm);

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
            Cipher cipher = Cipher.getInstance(gcm256algorithm);

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
        log.info("refreshKeyPair");
        OpsEncryptionEntity opsEncryptionEntity = encryptionMapper.selectOne(null);
        if (Objects.isNull(opsEncryptionEntity)) {
            opsEncryptionEntity = new OpsEncryptionEntity();
            log.info("key pair not found");
            generateKeyPair();
            saveKeyPair(opsEncryptionEntity);
        } else {
            log.info("key pair found");
            publicKey = opsEncryptionEntity.getPublicKey();
            privateKey = opsEncryptionEntity.getPrivateKey();
        }
    }

    private void saveKeyPair(OpsEncryptionEntity opsEncryptionEntity) {
        opsEncryptionEntity.setEncryptionId("1");
        opsEncryptionEntity.setPublicKey(publicKey);
        opsEncryptionEntity.setPrivateKey(privateKey);
        encryptionService.save(opsEncryptionEntity);
    }

    @Override
    public void run(String... args) throws Exception {
        refreshKeyPair();
    }
}
