/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.tool.cipher;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.codec.binary.Base64;
import org.opengauss.tool.cipher.exception.SecureException;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Description: RSA encrypt and decode
 *
 * @author: wangchao
 * @Date: 2025/11/27 10:23
 * @since 7.0.0-RC2
 **/
@Slf4j
public class RsaUtils implements AutoCloseable {
    private static final Map<Integer, RsaUtils> INSTANCES = new ConcurrentHashMap<>();
    private static final ReentrantLock INSTANCE_LOCK = new ReentrantLock();
    private static final int DEFAULT_KEY_SIZE = 4096;

    private final AtomicReference<KeyPair> keyPairRef = new AtomicReference<>();
    private final AtomicReference<SecureString> privateKeyStringRef = new AtomicReference<>();
    private final int keySize;
    private final SecureRandom secureRandom;

    private RsaUtils(int keySize) {
        this.keySize = keySize;
        this.secureRandom = new SecureRandom();
    }

    /**
     * Get singleton instance of default key size
     *
     * @return rsa utils instance
     */
    public static RsaUtils getInstance() {
        return getInstance(DEFAULT_KEY_SIZE);
    }

    private static RsaUtils getInstance(int keySize) {
        RsaUtils instance = INSTANCES.get(keySize);
        if (instance == null) {
            INSTANCE_LOCK.lock();
            try {
                instance = INSTANCES.get(keySize);
                if (instance == null) {
                    instance = new RsaUtils(keySize);
                    instance.generateKeyPair();
                    INSTANCES.put(keySize, instance);
                }
            } finally {
                INSTANCE_LOCK.unlock();
            }
        }
        return instance;
    }

    private synchronized void generateKeyPair() {
        try {
            clearKeyPair();
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(keySize, secureRandom);
            KeyPair keyPair = keyGen.generateKeyPair();
            keyPairRef.set(keyPair);
            byte[] privateKeyBytes = keyPair.getPrivate().getEncoded();
            SecureString securePrivateKey = new SecureString(Base64.encodeBase64String(privateKeyBytes));
            privateKeyStringRef.set(securePrivateKey);
            SecureKeyManager.secureWipe(privateKeyBytes);
            SecureKeyManager.memoryBarrier();
        } catch (NoSuchAlgorithmException ex) {
            log.warn("init generateKeyPair error {}", ex.getMessage());
            throw new SecureException("Initialization key failed", ex);
        }
    }

    /**
     * Safely obtain public key (can be shared securely)
     *
     * @return public key
     */
    public String publicKey() {
        KeyPair keyPair = keyPairRef.get();
        if (keyPair == null) {
            throw new SecureException("Key pair not initialized");
        }
        return Base64.encodeBase64String(keyPair.getPublic().getEncoded());
    }

    /**
     * Safely use private keys to perform operations
     *
     * @param operation operation
     * @return operation result
     */
    private <T extends Serializable> T executeWithSecurePrivateKey(SecurePrivateKeyOperation<T> operation) {
        SecureString securePrivateKey = privateKeyStringRef.get();
        if (securePrivateKey == null) {
            throw new IllegalStateException("Key pair not initialized");
        }
        return securePrivateKey.useString(privateKeyBytes -> {
            try {
                byte[] decodedKey = Base64.decodeBase64(privateKeyBytes);
                PrivateKey privateKey = KeyFactory.getInstance("RSA")
                    .generatePrivate(new PKCS8EncodedKeySpec(decodedKey));
                T result = operation.execute(privateKey);
                SecureKeyManager.secureWipe(decodedKey);
                return result;
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                throw new SecureException("Private key operation failed", e);
            }
        });
    }

    /**
     * Secure Signature
     *
     * @param data data
     * @return data sign
     */
    public String sign(String data) {
        if (data == null || data.trim().isEmpty()) {
            throw new SecureException("签名数据不能为空");
        }
        return executeWithSecurePrivateKey(privateKey -> {
            try (SecureString secureData = new SecureString(data)) {
                return secureData.useString(dataBytes -> sign(privateKey, dataBytes));
            }
        });
    }

    private String sign(PrivateKey privateKey, byte[] dataBytes) {
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(dataBytes);
            byte[] signatureBytes = signature.sign();
            String result = Base64.encodeBase64String(signatureBytes);
            SecureKeyManager.secureWipe(signatureBytes);
            return result;
        } catch (SignatureException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new SecureException("sign failed", e);
        }
    }

    /**
     * Secure Signature
     *
     * @param secureData secureData
     * @return data sign
     */
    public String sign(SecureString secureData) {
        return executeWithSecurePrivateKey(
            privateKey -> secureData.useString(dataBytes -> sign(privateKey, dataBytes)));
    }

    /**
     * verify signature
     *
     * @param data data
     * @param sign sign
     * @return sign verify result
     */
    public boolean verify(String data, String sign) {
        KeyPair keyPair = keyPairRef.get();
        if (keyPair == null) {
            throw new SecureException("Key pair not initialized");
        }
        try (SecureString secureData = new SecureString(data)) {
            return secureData.useString(dataBytes -> {
                try {
                    PublicKey publicKey = keyPair.getPublic();
                    Signature signature = Signature.getInstance("SHA256withRSA");
                    signature.initVerify(publicKey);
                    signature.update(dataBytes);
                    return signature.verify(Base64.decodeBase64(sign));
                } catch (SignatureException | NoSuchAlgorithmException | InvalidKeyException e) {
                    throw new SecureException("Signature verification failed", e);
                }
            });
        }
    }

    /**
     * encrypt
     *
     * @param plainText  plainText
     * @return encrypt result
     */
    public String encrypt(String plainText) {
        if (plainText == null || plainText.trim().isEmpty()) {
            throw new SecureException("Clear text data cannot be empty");
        }
        try (SecureString securePlainText = new SecureString(plainText)) {
            return securePlainText.useString(plainBytes -> {
                PublicKey publicKey = keyPairRef.get().getPublic();
                return encrypt(plainBytes, publicKey);
            });
        }
    }

    private String encrypt(byte[] plainBytes, PublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedBytes = cipher.doFinal(plainBytes);
            String result = Base64.encodeBase64String(encryptedBytes);
            SecureKeyManager.secureWipe(encryptedBytes);
            return result;
        } catch (NoSuchPaddingException | InvalidKeyException | BadPaddingException | NoSuchAlgorithmException
            | IllegalBlockSizeException e) {
            throw new SecureException("Encryption failed", e);
        }
    }

    /**
     * encrypt
     *
     * @param plainText plainText
     * @param publicKey publicKey
     * @return encrypt result
     */
    public String encrypt(String plainText, String publicKey) {
        if (plainText == null || plainText.trim().isEmpty()) {
            throw new SecureException("Clear text data cannot be empty");
        }
        if (publicKey == null || publicKey.trim().isEmpty()) {
            throw new SecureException("The public key cannot be empty");
        }
        try (SecureString securePublicKey = new SecureString(publicKey);
            SecureString securePlainText = new SecureString(plainText)) {
            return securePublicKey.useString(publicKeyBytes -> securePlainText.useString(plainBytes -> {
                PublicKey key = convertBase64ToPublicKey(publicKeyBytes);
                return encrypt(plainBytes, key);
            }));
        }
    }

    private PublicKey convertBase64ToPublicKey(byte[] base64PublicKey) {
        try {
            byte[] keyBytes = Base64.decodeBase64(base64PublicKey);
            try {
                X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                PublicKey publicKey = keyFactory.generatePublic(keySpec);
                SecureKeyManager.secureWipe(keyBytes);
                return publicKey;
            } finally {
                SecureKeyManager.secureWipe(keyBytes);
            }
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new SecureException("Public key format error", e);
        }
    }

    /**
     * Secure Decryption - Return SecureString Object
     *
     * @param cipherText cipherText
     * @return SecureString Secure result
     */
    public SecureString decryptSecure(String cipherText) {
        return executeWithSecurePrivateKey(privateKey -> {
            byte[] encryptedBytes = null;
            byte[] decryptedBytes = null;
            try {
                Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                cipher.init(Cipher.DECRYPT_MODE, privateKey);
                encryptedBytes = Base64.decodeBase64(cipherText);
                decryptedBytes = cipher.doFinal(encryptedBytes);
                return new SecureString(decryptedBytes);
            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
                | IllegalBlockSizeException | BadPaddingException e) {
                throw new SecureException("Decryption failed", e);
            } finally {
                SecureKeyManager.secureWipe(decryptedBytes);
                SecureKeyManager.secureWipe(encryptedBytes);
            }
        });
    }

    /**
     * decrypt
     *
     * @param cipherText cipherText
     * @return decrypt res
     */
    public String decrypt(String cipherText) {
        try (SecureString secureResult = decryptSecure(cipherText)) {
            return secureResult.useString(bytes -> new String(bytes, StandardCharsets.UTF_8));
        }
    }

    /**
     * Secure Cleanup Key Pair
     */
    public synchronized void clearKeyPair() {
        SecureString securePrivateKey = privateKeyStringRef.getAndSet(null);
        if (securePrivateKey != null) {
            securePrivateKey.close();
        }
        KeyPair keyPair = keyPairRef.getAndSet(null);
        if (keyPair != null) {
            SecureKeyManager.secureWipeKeyPair(keyPair);
        }
        SecureKeyManager.memoryBarrier();
    }

    @Override
    public void close() {
        clearKeyPair();
        INSTANCE_LOCK.lock();
        try {
            INSTANCES.entrySet().removeIf(entry -> entry.getValue() == this);
        } finally {
            INSTANCE_LOCK.unlock();
        }
    }

    /**
     * Private key operation function interface
     */
    @FunctionalInterface
    public interface SecurePrivateKeyOperation<T extends Serializable> {
        /**
         * Private key operation interface
         *
         * @param privateKey privateKey
         * @return operate result
         */
        T execute(PrivateKey privateKey);
    }
}
