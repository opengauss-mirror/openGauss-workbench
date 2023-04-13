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
 * DadResult.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/domain/model/ops/olk/dadReq/DadResult.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.domain.model.ops.olk.dadReq;

import cn.hutool.core.util.ObjectUtil;

import java.util.HashMap;

public class DadResult extends HashMap<String, Object> {
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
    public static final int SUCCESS = 200;
    public static final int ERROR = 500;
    private static final long serialVersionUID = 1L;

    public DadResult() {
    }

    public DadResult(int code, String msg) {
        super.put(CODE_TAG, code);
        super.put(MSG_TAG, msg);
    }

    public DadResult(int code, String msg, Object data) {
        super.put(CODE_TAG, code);
        super.put(MSG_TAG, msg);
        if (data != null) {
            super.put(DATA_TAG, data);
        }
    }

    public static DadResult success() {
        return DadResult.success("success");
    }

    public static DadResult success(Object data) {
        return DadResult.success("success", data);
    }

    public static DadResult success(String msg) {
        return DadResult.success(msg, null);
    }

    public static DadResult success(String msg, Object data) {
        return new DadResult(SUCCESS, msg, data);
    }

    public static DadResult error() {
        return DadResult.error("fail");
    }

    public static DadResult error(String msg) {
        return DadResult.error(msg, null);
    }

    public static DadResult error(String msg, Object data) {
        return new DadResult(ERROR, msg, data);
    }

    public static DadResult error(int code, String msg) {
        return new DadResult(code, msg, null);
    }

    public boolean isSuccess() {
        if (ObjectUtil.isNull(get(CODE_TAG))) {
            return false;
        }
        return get(CODE_TAG).equals(SUCCESS);
    }
}
