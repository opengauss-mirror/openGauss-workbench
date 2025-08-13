/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.jsch.pool.exception;

import com.jcraft.jsch.JSchException;

/**
 * JschPoolException
 *
 * @author: wangchao
 * @Date: 2025/8/11 16:14
 * @since 7.0.0-RC2
 **/
public class JschPoolException extends JSchException {
    /**
     * jsch pool exception
     *
     * @param message exception message
     * @param e throwable
     */
    public JschPoolException(String message, Throwable e) {
        super(message, e);
    }

    /**
     * jsch pool exception
     *
     * @param message exception message
     */
    public JschPoolException(String message) {
        super(message);
    }
}
