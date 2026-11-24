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

package com.tools.monitor.exception;

import lombok.Data;

/**
 * ParamsException
 *
 * @author liu
 * @since 2022-10-01
 */
@Data
public class ParamsException extends RuntimeException {
    private Integer code = 300;

    private String message = "System abnormality";

    /**
     * ParamsException
     */
    public ParamsException() {
        super("System abnormality!");
    }

    /**
     * ParamsException
     *
     * @param message message
     */
    public ParamsException(String message) {
        super(message);
        this.message = message;
    }

    /**
     * ParamsException
     *
     * @param code code
     */
    public ParamsException(Integer code) {
        super("System abnormality!");
        this.code = code;
    }

    /**
     * ParamsException
     *
     * @param code code
     * @param message message
     */
    public ParamsException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}
