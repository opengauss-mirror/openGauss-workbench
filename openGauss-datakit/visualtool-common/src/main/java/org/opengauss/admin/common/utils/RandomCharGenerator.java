/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
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
