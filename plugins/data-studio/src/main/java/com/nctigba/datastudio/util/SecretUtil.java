package com.nctigba.datastudio.util;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class SecretUtil {
    
    private static String KEY = "R0JBLU5DVEktRFM=";
    private static String IV = "1234567890000000";

    public static String encrypt(String data) {
        return encrypt(data, KEY, IV);
    }
    
    public static String desEncrypt(String data) {
        return desEncrypt(data, KEY, IV);
    }
    
    static String encrypt(String data, String key, String iv) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            int blockSize = cipher.getBlockSize();
            byte[] dataBytes = data.getBytes();
            int plaintextLength = dataBytes.length;
            if (plaintextLength % blockSize != 0) {
                plaintextLength = plaintextLength + (blockSize - (plaintextLength % blockSize));
            }
            byte[] plaintext = new byte[plaintextLength];
            System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);
            SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");
            IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());
            cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
            byte[] encrypted = cipher.doFinal(plaintext);

            return new Base64().encodeToString(encrypted);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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
            e.printStackTrace();
            return null;
        }
    }
}