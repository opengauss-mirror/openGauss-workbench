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
 *  InstanceException.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/main/java/com/nctigba/observability/instance/exception/InstanceException.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class InstanceException extends BaseI18nException {
    private static final long serialVersionUID = 1L;

    public InstanceException(String message, Integer code) {
        super(message, code);
    }

    public InstanceException(String message, Integer code, Object[] args) {
        super(message, code, args);
    }

    public InstanceException(String message) {
        super(message, 500);
    }

    public InstanceException(String message, Throwable cause) {
        super(message, cause);
    }
}