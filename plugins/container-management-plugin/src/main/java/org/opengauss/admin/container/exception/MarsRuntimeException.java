/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 * MarsRuntimeException.java
 *
 * IDENTIFICATION
 * plugins/container-management-plugin/src/main/java/org/opengauss/admin/container/kubernetes/MarsRuntimeException.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.container.exception;

import org.opengauss.admin.container.config.ErrorCodeMessage;
import org.opengauss.admin.container.constant.CommonConstant;

/**
 * 借鉴middle异常类
 * 在异常后期望结果被封装为csap result时，可选择抛出该RuntimeException
 *
 * @since 2024-2-13 17:19
 */

public class MarsRuntimeException extends RuntimeException {
    private String errorCode;
    private String errorName;
    private String errorMessage;

    /**
     * MarsRuntimeException
     *
     * @param error ErrorCodeMessage
     */
    public MarsRuntimeException(ErrorCodeMessage error) {
        super(error.phrase());
        this.errorCode = String.valueOf(error.value());
        this.errorName = error.name();
        this.errorMessage = error.phrase();
    }

    /**
     * MarsRuntimeException
     *
     * @param errMsg errMsg
     */
    public MarsRuntimeException(String errMsg) {
        super(errMsg);
        this.errorCode = CommonConstant.RETURN_CODE_FAIL;
        this.errorName = "通用错误";
        this.errorMessage = errMsg;
    }

    /**
     * 获取错误码
     *
     * @return errorCode
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * set errorCode
     *
     * @param errorCode errorCode
     */
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * 获取errorName
     *
     * @return errorName
     */
    public String getErrorName() {
        return errorName;
    }

    /**
     * setErrorName
     *
     * @param errorName errorName
     */
    public void setErrorName(String errorName) {
        this.errorName = errorName;
    }

    /**
     * getErrorMessage
     *
     * @return errorMessage
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * setErrorMessage
     *
     * @param errorMessage errorMessage
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
