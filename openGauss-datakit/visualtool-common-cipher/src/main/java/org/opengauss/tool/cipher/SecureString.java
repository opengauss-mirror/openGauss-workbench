/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.tool.cipher;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * SecureString
 *
 * @author: wangchao
 * @Date: 2025/11/27 13:00
 * @since 7.0.0-RC3
 **/
public class SecureString implements AutoCloseable, Serializable {
    private static final long serialVersionUID = 42L;

    private final Charset charset = StandardCharsets.UTF_8;
    private ByteBuffer buffer;

    /**
     * SecureString
     *
     * @param data data
     */
    public SecureString(byte[] data) {
        if (data != null) {
            buffer = ByteBuffer.allocateDirect(data.length);
            buffer.put(data);
            buffer.flip();
            Arrays.fill(data, (byte) 0);
        }
    }

    /**
     * SecureString
     *
     * @param data data
     */
    public SecureString(String data) {
        if (data != null) {
            byte[] bytes = data.getBytes(charset);
            buffer = ByteBuffer.allocateDirect(bytes.length);
            buffer.put(bytes);
            buffer.flip();
            Arrays.fill(bytes, (byte) 0);
        }
    }

    /**
     * Secure useString  operation
     *
     * @param operation operation
     * @param <T> SecureByteOperation
     * @return operation res
     */
    public <T extends Serializable> T useString(SecureByteOperation<T> operation) {
        if (buffer == null) {
            return operation.execute(null);
        }
        try {
            byte[] temp = new byte[buffer.remaining()];
            buffer.get(temp);
            return operation.execute(temp);
        } finally {
            buffer.rewind();
        }
    }

    @Override
    public void close() {
        if (buffer != null) {
            for (int i = 0; i < buffer.capacity(); i++) {
                buffer.put(i, (byte) 0);
            }
            buffer = null;
        }
    }

    /**
     * SecureByteOperation
     *
     * @param <T> SecureByteOperation
     */
    @FunctionalInterface
    public interface SecureByteOperation<T extends Serializable> {
        /**
         * execute
         *
         * @param data data
         * @return SecureByteOperation
         */
        T execute(byte[] data);
    }
}
