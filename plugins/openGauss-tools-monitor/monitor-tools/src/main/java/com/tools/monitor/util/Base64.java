/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */

package com.tools.monitor.util;

import java.nio.charset.StandardCharsets;

/**
 * Base64
 *
 * @author liu
 * @since 2022-10-01
 */
public class Base64 {
    /**
     * base64
     *
     * @param message message
     * @return String
     */
    public static String encode(String message) {
        java.util.Base64.Encoder encoder = java.util.Base64.getMimeEncoder();
        return encoder.encodeToString(message.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * base64
     *
     * @param encodeMessage encodeMessage
     * @return String
     */
    public static String decode(String encodeMessage) {
        java.util.Base64.Decoder decoder = java.util.Base64.getMimeDecoder();
        byte[] decode = decoder.decode(encodeMessage);
        return new String(decode, StandardCharsets.UTF_8);
    }
}
