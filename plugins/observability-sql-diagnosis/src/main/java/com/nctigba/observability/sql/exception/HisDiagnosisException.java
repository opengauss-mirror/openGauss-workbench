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
 *  HisDiagnosisException.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/exception/HisDiagnosisException.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.exception;

import org.opengauss.admin.common.exception.CustomException;

/**
 * HisDiagnosisException
 *
 * @author luomeng
 * @since 2023/6/20
 */
public class HisDiagnosisException extends CustomException {
    private static final long serialVersionUID = 1L;

    public HisDiagnosisException(String message, Integer code) {
        super(message, code);
    }

    public HisDiagnosisException(String message) {
        super(message, 500);
    }

    public HisDiagnosisException(String message, Throwable cause) {
        super(message, cause);
    }
}