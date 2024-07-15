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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * @date 2024/5/31 15:42
 * @since 0.0
 */
public class PKCEGenerator {
    /**
     * generateCodeVerifier
     *
     * @return String code_verifier
     */
    public static String generateCodeVerifier() {
        // Generates a random string of 128 length as code_verifier.
        byte[] verifierBytes = new byte[128];
        new SecureRandom().nextBytes(verifierBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(verifierBytes);
    }

    /**
     * generateCodeChallenge
     *
     * @param codeVerifier code_verifier
     * @return String code_challenge
     */
    public static String generateCodeChallenge(String codeVerifier) throws NoSuchAlgorithmException {
        // Hash code_verifier using the SHA-256 hash algorithm.
        MessageDigest digest = MessageDigest.getInstance(MyConstants.PKCE_CODE_CHALLENGE_METHOD);
        byte[] bytes = digest.digest(codeVerifier.getBytes());
        // Base64 encodes the hash result and removes the fill characters.
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}