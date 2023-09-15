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
 */

package org.opengauss.admin.common.utils;

/**
 * Description: permission judge tools class
 *
 * @version 1.0
 * @date: 2023/9/13
 * @since JDK 11
 */
public class PermissionUtils {
    /**
     * judge user had root permission
     *
     * @param userName userName
     * @return boolean
     **/
    public static boolean hasRootPermission(String userName) {
        return "root".equals(userName);
    }
}
