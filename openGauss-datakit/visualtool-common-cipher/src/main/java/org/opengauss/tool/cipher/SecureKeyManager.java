/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.tool.cipher;

import java.lang.invoke.VarHandle;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * SecureRsaKeyManager
 *
 * @author: wangchao
 * @Date: 2025/11/27 10:23
 * @since 7.0.0-RC3
 **/
public class SecureKeyManager {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Safely erase keyPair
     *
     * @param keyPair keyPair
     */
    public static void secureWipeKeyPair(KeyPair keyPair) {
        if (keyPair.getPrivate() != null) {
            byte[] privateKeyBytes = keyPair.getPrivate().getEncoded();
            if (privateKeyBytes != null) {
                secureWipe(privateKeyBytes);
            }
        }
        if (keyPair.getPublic() != null) {
            byte[] publicKeyBytes = keyPair.getPublic().getEncoded();
            if (publicKeyBytes != null) {
                secureWipe(publicKeyBytes);
            }
        }
        memoryBarrier();
    }

    /**
     * Safely erase byte arrays
     *
     * @param data data
     */
    public static void secureWipe(byte[] data) {
        if (data == null) {
            return;
        }
        for (int i = 0; i < 7; i++) {
            SECURE_RANDOM.nextBytes(data);
            Arrays.fill(data, (byte) 0xFF);
            Arrays.fill(data, (byte) 0x00);
            Arrays.fill(data, (byte) 0x55);
            Arrays.fill(data, (byte) 0xAA);
        }
        Arrays.fill(data, (byte) 0);
    }

    /**
     * Memory barrier to prevent compiler optimization
     */
    public static void memoryBarrier() {
        VarHandle.fullFence();
    }

    /**
     * Safely erase char arrays
     *
     * @param array char array
     */
    public static void secureWipe(char[] array) {
        if (array == null) {
            return;
        }
        for (int i = 0; i < 3; i++) {
            Arrays.fill(array, (char) 0);
            Arrays.fill(array, (char) 0xFF);
            Arrays.fill(array, (char) 0);
        }
        memoryBarrier();
    }
}
