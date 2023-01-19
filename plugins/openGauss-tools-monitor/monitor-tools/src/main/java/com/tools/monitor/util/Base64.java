package com.tools.monitor.util;

/**
 * Base64
 *
 * @author liu
 * @since 2022-10-01
 */
public class Base64 {
    public static String encode(String message) {
        java.util.Base64.Encoder encoder = java.util.Base64.getMimeEncoder();
        return encoder.encodeToString(message.getBytes());
    }

    public static String decode(String encodeMessage) {
        java.util.Base64.Decoder decoder = java.util.Base64.getMimeDecoder();
        byte[] decode = decoder.decode(encodeMessage);
        return new String(decode);
    }
}
