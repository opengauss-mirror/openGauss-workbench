/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.common.utils;

import java.security.SecureRandom;

/**
 * RandomCharGenerator
 *
 * @author: wangchao
 * @Date: 2025/9/25 10:07
 * @since 7.0.0-RC2
 **/
public class RandomCharGenerator {
    private static final int LENGTH = 128;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final char[] CHAR_SET = {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V',
        'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r',
        's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
    };

    /**
     * generator base random string ,length=128
     *
     * @return random string
     */
    public static String generateRandomBased128String() {
        byte[] bytes = new byte[LENGTH];
        RANDOM.nextBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < LENGTH; i++) {
            sb.append(CHAR_SET[Math.abs(bytes[i]) % CHAR_SET.length]);
        }
        return sb.toString();
    }
}
