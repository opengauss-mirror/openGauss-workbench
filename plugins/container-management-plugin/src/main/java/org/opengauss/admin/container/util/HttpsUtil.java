/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
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
 *  HttpsUtil.java
 *
 *  IDENTIFICATION
 *  plugins/container-management-plugin/src/main/java/org/opengauss/admin/container/util/HttpsUtil.java
 *
 *  -------------------------------------------------------------------------
 */

package org.opengauss.admin.container.util;

import org.springframework.http.HttpStatus;

/**
 * HttpsUtil
 *
 * @since 2024-5-16
 */
public class HttpsUtil {
    /**
     * http请求是否成功
     *
     * @param statusCode code
     * @return boolean
     */
    public static Boolean isSuccess(Integer statusCode) {
        Integer tempStatusCode = statusCode == Integer.MIN_VALUE ? 2 : Math.abs(statusCode);
        while (tempStatusCode >= 10) {
            tempStatusCode = tempStatusCode / 10;
        }
        return tempStatusCode == 2;
    }

    /**
     * isNotFound
     *
     * @param statusCode code
     * @return boolean
     */
    public static Boolean isNotFound(Integer statusCode) {
        return HttpStatus.NOT_FOUND.value() == statusCode;
    }
}
