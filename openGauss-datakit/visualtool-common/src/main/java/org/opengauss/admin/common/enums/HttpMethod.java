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
 * HttpMethod.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/enums/HttpMethod.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.common.enums;

import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Request Method
 *
 * @author xielibo
 */
public enum HttpMethod {
    //GET
    GET,
    //HEAD
    HEAD,
    //POST
    POST,
    //PUT
    PUT,
    //PATCH
    PATCH,
    //DELETE
    DELETE,
    //OPTIONS
    OPTIONS,
    //TRACE
    TRACE;

    private static final Map<String, HttpMethod> MAPPINGS = new HashMap<>(16);

    static {
        for (HttpMethod httpMethod : values()) {
            MAPPINGS.put(httpMethod.name(), httpMethod);
        }
    }

    @Nullable
    public static HttpMethod resolve(@Nullable String method) {
        return (method != null ? MAPPINGS.get(method) : null);
    }

    public boolean matches(String method) {
        return (this == resolve(method));
    }
}
