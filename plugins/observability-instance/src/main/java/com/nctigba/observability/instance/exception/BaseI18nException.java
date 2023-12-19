/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  BaseI18nException.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/main/java/com/nctigba/observability/instance/exception/BaseI18nException.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.exception;

import com.nctigba.observability.instance.util.MessageSourceUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 功能描述
 *
 * @author liupengfei
 * @since 2023/6/20
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class BaseI18nException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private Integer code;

    private String message;

    private Object[] args;

    public BaseI18nException(String message) {
        this.message = message;
    }

    public BaseI18nException(String message, Integer code) {
        this.message = message;
        this.code = code;
    }

    public BaseI18nException(String message, Integer code, Object[] args) {
        this.message = message;
        this.code = code;
        this.args = args;
    }

    public BaseI18nException(String message, Throwable e) {
        super(message, e);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return MessageSourceUtils.getMsg(message, args);
    }

    public Integer getCode() {
        return code;
    }

}
