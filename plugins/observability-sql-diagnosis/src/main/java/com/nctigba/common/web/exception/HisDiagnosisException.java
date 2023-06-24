/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.common.web.exception;

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