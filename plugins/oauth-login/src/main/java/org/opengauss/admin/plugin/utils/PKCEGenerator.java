/*
 * Copyright (c) 2024 Huawei Technologies Co.,Ltd.
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
 * PKCEGenerator.java
 *
 * IDENTIFICATION
 * oauth-login/src/main/java/org/opengauss/admin/plugin/utils/PKCEGenerator.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.utils;

import org.opengauss.admin.plugin.constants.MyConstants;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * PKCEGenerator
 *
 * @author duanguoqiang
 * @since 2024/6/22
 **/
public class PKCEGenerator {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * generateCodeVerifier
     *
     * @return String code_verifier
     */
    public static String generateCodeVerifier() {
        // Generates a random string of 128 length as code_verifier.
        byte[] verifierBytes = new byte[128];
        SECURE_RANDOM.nextBytes(verifierBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(verifierBytes);
    }

    /**
     * generateCodeChallenge
     *
     * @param String code_verifier
     * @return String hash_result
     */
    public static String generateCodeChallenge(String codeVerifier) throws NoSuchAlgorithmException {
        // Hash code_verifier using the SHA-256 hash algorithm.
        MessageDigest digest = MessageDigest.getInstance(MyConstants.PKCE_CODE_CHALLENGE_METHOD);
        byte[] bytes = digest.digest(codeVerifier.getBytes(StandardCharsets.UTF_8));
        // Base64 encodes the hash result and removes the fill characters.
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}