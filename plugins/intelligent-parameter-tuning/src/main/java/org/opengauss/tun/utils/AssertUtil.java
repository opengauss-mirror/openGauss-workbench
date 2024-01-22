/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
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
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.tun.utils;

import org.opengauss.admin.common.exception.ServiceException;

/**
 * AssertUtil
 *
 * @author liu
 * @since 2023-09-17
 */
public class AssertUtil {
    /**
     * isTrue
     *
     * @param isFlag isFlag
     * @param msg    msg
     */
    public static void isTrue(Boolean isFlag, String msg) {
        if (isFlag) {
            throw new ServiceException(msg);
        }
    }

    /**
     * save
     *
     * @param expectedValue expectedValue
     * @param actualValue actualValue
     * @param msg msg
     */
    public static void save(int expectedValue, int actualValue, String msg) {
        if (expectedValue != actualValue) {
            throw new ServiceException(msg);
        }
    }
}
