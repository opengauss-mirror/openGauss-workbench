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

package org.opengauss.admin.common.utils;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import org.opengauss.admin.common.exception.SecureException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * AesGcmUtils
 *
 * @author wangchao
 * @since 2024/10/29 09:26
 */
@Slf4j
public class AesGcmUtils {
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder B64_ENCODER = Base64.getEncoder();
    private static final Base64.Decoder B64_DECODER = Base64.getDecoder();
    private static final Object LOCK = new Object();

    private static volatile SecretKey aesKey = null;

    /**
     * initialize key
     */
    public static void initializeKey() {
        if (aesKey == null) {
            synchronized (LOCK) {
                if (aesKey == null) {
                    aesKey = KeyLoader.loadOrGenerateKey();
                }
            }
            log.info("initialize AES key success");
        }
    }

    /**
     * encrypt plaintext
     *
     * @param plaintext plaintext
     * @return ciphertext
     */
    public static String encrypt(String plaintext) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, aesKey, spec);
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + ciphertext.length);
            byteBuffer.put(iv);
            byteBuffer.put(ciphertext);
            return B64_ENCODER.encodeToString(byteBuffer.array());
        } catch (InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException e) {
            throw new SecureException("AES encrypt algorithm error :" + e.getMessage());
        } catch (NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
            throw new SecureException("AES encrypt failed :" + e.getMessage());
        }
    }

    /**
     * decrypt ciphertext
     *
     * @param base64Ciphertext ciphertext
     * @return plaintext
     */
    public static String decrypt(String base64Ciphertext) {
        try {
            byte[] data = B64_DECODER.decode(base64Ciphertext);
            ByteBuffer byteBuffer = ByteBuffer.wrap(data);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);
            byte[] encryptedData = new byte[byteBuffer.remaining()];
            byteBuffer.get(encryptedData);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, aesKey, spec);
            return new String(cipher.doFinal(encryptedData), StandardCharsets.UTF_8);
        } catch (InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException e) {
            throw new SecureException("AES decrypt algorithm error :" + e.getMessage());
        } catch (NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
            throw new SecureException("AES decrypt failed :" + e.getMessage());
        }
    }

    static class KeyLoader {
        private static final int AES_KEY_SIZE = 256;
        private static final String KEYSTORE_TYPE = "JCEKS";
        private static final String KEY_ALGORITHM = "AES";
        private static final String KEY_ALIAS = "aes_alias_key";
        private static final String AES_KEY_ENV = "DATA_KIT_AES_KEY";
        private static final Path KEYSTORE_PATH = Paths.get(System.getProperty("user.home"), ".secure",
            "datakit.jceks");

        /**
         * KeyStore, load or generate key from keystore.jceks.
         * if not exist, generate a new one and saved in keystore.jceks.
         * the jceks file is saved in the current directory. like user.dir/keystore.jceks
         *
         * @return SecretKey
         */
        public static synchronized SecretKey loadOrGenerateKey() {
            if (StrUtil.isEmpty(System.getenv(AES_KEY_ENV)) && StrUtil.isEmpty(System.getProperty(AES_KEY_ENV))) {
                throw new SecureException("there must be have the environment variable: " + AES_KEY_ENV);
            }
            try {
                SecretKey initAesKey = null;
                char[] password = getAesKeyEnv();
                KeyStore keyStore = loadOrCreateKeystore(password);
                if (!keyStore.containsAlias(KEY_ALIAS)) {
                    initAesKey = generateAesKey();
                    saveSecretKey(keyStore, initAesKey, password);
                    saveKeystore(keyStore, password);
                } else {
                    Key key = keyStore.getKey(KEY_ALIAS, password);
                    if (key instanceof SecretKey aliasKey) {
                        initAesKey = aliasKey;
                    }
                }
                Arrays.fill(password, '\0');
                return initAesKey;
            } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
                throw new SecureException("load or generate key failed: " + e.getMessage());
            }
        }

        private static char[] getAesKeyEnv() {
            String aesEnvKey = System.getenv(AES_KEY_ENV);
            if (StrUtil.isNotEmpty(aesEnvKey)) {
                return aesEnvKey.toCharArray();
            }
            aesEnvKey = System.getProperty(AES_KEY_ENV);
            if (StrUtil.isNotEmpty(aesEnvKey)) {
                return aesEnvKey.toCharArray();
            } else {
                throw new SecureException("there must be have the environment variable: " + AES_KEY_ENV);
            }
        }

        private static KeyStore loadOrCreateKeystore(char[] password) throws SecureException {
            try {
                KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
                if (Files.exists(KEYSTORE_PATH)) {
                    try (InputStream is = Files.newInputStream(KEYSTORE_PATH)) {
                        keyStore.load(is, password);
                    }
                } else {
                    keyStore.load(null, null);
                }
                return keyStore;
            } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
                throw new SecureException("load or create keystore failed: " + e.getMessage());
            }
        }

        private static SecretKey generateAesKey() throws NoSuchAlgorithmException {
            KeyGenerator keyGen = KeyGenerator.getInstance(KEY_ALGORITHM);
            keyGen.init(AES_KEY_SIZE, SecureRandom.getInstanceStrong());
            return keyGen.generateKey();
        }

        private static void saveSecretKey(KeyStore ks, SecretKey key, char[] password) throws KeyStoreException {
            KeyStore.SecretKeyEntry entry = new KeyStore.SecretKeyEntry(key);
            KeyStore.PasswordProtection protParam = new KeyStore.PasswordProtection(password);
            ks.setEntry(KEY_ALIAS, entry, protParam);
        }

        private static void saveKeystore(KeyStore ks, char[] password) throws SecureException {
            try {
                Files.createDirectories(KEYSTORE_PATH.getParent());
                try (OutputStream os = Files.newOutputStream(KEYSTORE_PATH, StandardOpenOption.CREATE)) {
                    ks.store(os, password);
                } catch (CertificateException | KeyStoreException | NoSuchAlgorithmException e) {
                    throw new SecureException("save keystore failed: " + e.getMessage());
                }
            } catch (IOException e) {
                throw new SecureException("save keystore failed: " + e.getMessage());
            }
        }
    }
}