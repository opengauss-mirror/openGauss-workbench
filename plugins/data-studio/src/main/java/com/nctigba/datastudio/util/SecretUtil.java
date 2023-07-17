/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.opengauss.admin.common.exception.CustomException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * SecretUtil
 *
 * @since 2023-6-26
 */
@Slf4j
public class SecretUtil {

    private static final String KEY = "R0JBLU5DVEktRFM=";
    private static final String IV = "1234567890000000";

    /**
     * des encrypt
     *
     * @param data data
     * @return String
     */
    public static String desEncrypt(String data) {
        try {
            byte[] encrypted1 = new Base64().decode(data);
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes(), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(IV.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] original = cipher.doFinal(encrypted1);
            return new String(original).trim();
        } catch (InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException
            | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
            throw new CustomException(e.getMessage());
        }
    }
}