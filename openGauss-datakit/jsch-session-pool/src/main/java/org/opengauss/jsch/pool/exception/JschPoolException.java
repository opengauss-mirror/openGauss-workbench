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
