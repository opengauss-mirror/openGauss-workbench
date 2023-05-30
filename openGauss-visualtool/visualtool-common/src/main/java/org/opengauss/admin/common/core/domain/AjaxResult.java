/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
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
 * AjaxResult.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/core/domain/AjaxResult.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.common.core.domain;

import org.opengauss.admin.common.enums.ResponseCode;
import org.opengauss.admin.common.utils.StringUtils;

import java.util.HashMap;

/**
 * Ajax Result Response
 *
 * @author xielibo
 */
public class AjaxResult extends HashMap<String, Object> {
    private static final long serialVersionUID = 1L;

    /**
     * code
     */
    public static final String CODE_TAG = "code";

    /**
     * message
     */
    public static final String MSG_TAG = "msg";

    /**
     * data
     */
    public static final String DATA_TAG = "data";

    public AjaxResult() {
    }
    public AjaxResult(int code, String msg) {
        super.put(CODE_TAG, code);
        super.put(MSG_TAG, msg);
    }

    public AjaxResult(int code, String msg, Object data) {
        super.put(CODE_TAG, code);
        super.put(MSG_TAG, msg);
        if (StringUtils.isNotNull(data)) {
            super.put(DATA_TAG, data);
        }
    }
    public static AjaxResult success() {
        return AjaxResult.success("success");
    }

    public static AjaxResult success(Object data) {
        return AjaxResult.success("success", data);
    }

    public static AjaxResult success(String msg) {
        return AjaxResult.success(msg, null);
    }

    public static AjaxResult success(String msg, Object data) {
        return new AjaxResult(ResponseCode.SUCCESS.code(), msg, data);
    }

    public static AjaxResult error() {
        return AjaxResult.error("fail");
    }

    public static AjaxResult error(String msg) {
        return AjaxResult.error(msg, null);
    }

    public static AjaxResult error(Integer code) {
        return AjaxResult.error(code, ResponseCode.getInstance(code).msg());
    }

    /**
     * error response encapsulation
     *
     * @param code error code
     * @param msg error message
     * @param data data
     * @return Response Result
     */
    public static AjaxResult errorAttachedData(Integer code, String msg, Object data) {
        return new AjaxResult(code, msg, data);
    }

    public static AjaxResult error(String msg, Object data) {
        return new AjaxResult(ResponseCode.ERROR.code(), msg, data);
    }

    public static AjaxResult error(int code, String msg) {
        return new AjaxResult(code, msg, null);
    }

    public boolean isOk() {
        Object code = get(CODE_TAG);
        return code != null && code.equals(ResponseCode.SUCCESS.code());
    }
}
