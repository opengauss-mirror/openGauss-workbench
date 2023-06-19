/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

@Slf4j
public class SecretUtil {

    private static final String KEY = "R0JBLU5DVEktRFM=";
    private static final String IV = "1234567890000000";

    public static String desEncrypt(String data) {
        return desEncrypt(data, KEY, IV);
    }

    private static String desEncrypt(String data, String key, String iv) {
        try {
            byte[] encrypted1 = new Base64().decode(data);

            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] original = cipher.doFinal(encrypted1);
            return new String(original).trim();
        } catch (Exception e) {
            log.info(e.toString());
            return null;
        }
    }
}