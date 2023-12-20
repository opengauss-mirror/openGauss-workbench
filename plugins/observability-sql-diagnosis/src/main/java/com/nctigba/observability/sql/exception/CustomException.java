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
 *  CustomException.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/exception/CustomException.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.exception;

import com.nctigba.observability.sql.enums.CustomExceptionEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * CustomException
 *
 * @author luomeng
 * @since 2023/6/20
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class CustomException extends RuntimeException {
    private static final long serialVersionUID = -8284004937326482732L;
    private Integer code;

    public CustomException(String msg) {
        this(CustomExceptionEnum.INTERNAL_SERVER_ERROR, msg);
    }

    public CustomException(Integer code, String msg) {
        this(code, msg, null);
    }

    public CustomException(Throwable throwable) {
        this(CustomExceptionEnum.INTERNAL_SERVER_ERROR, throwable);
    }

    public CustomException(String msg, Throwable throwable) {
        this(CustomExceptionEnum.INTERNAL_SERVER_ERROR, msg, throwable);
    }

    public CustomException(Integer code, String msg, Throwable throwable) {
        super(msg, throwable, false, false);
        this.code = code;
    }

    public CustomException(CustomExceptionEnum enu) {
        this(enu.getCode(), enu.getMsg());
    }

    public CustomException(CustomExceptionEnum enu, String msg) {
        this(enu, msg, null);
    }

    public CustomException(CustomExceptionEnum enu, Throwable throwable) {
        this(enu, enu.getMsg(), throwable);
    }

    public CustomException(CustomExceptionEnum enu, String msg, Throwable throwable) {
        this(enu.getCode(), msg, throwable);
    }
}