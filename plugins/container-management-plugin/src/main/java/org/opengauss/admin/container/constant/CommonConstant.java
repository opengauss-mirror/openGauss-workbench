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
 *  CommonConstants.java
 *
 *  IDENTIFICATION
 *  plugins/container-management-plugin/src/main/java/org/opengauss/admin/container/constant/CommonConstant.java
 *
 *  -------------------------------------------------------------------------
 */

package org.opengauss.admin.container.constant;

/**
 * CommonConstant
 *
 * @since 2024-5-20
 */
public class CommonConstant {
    /**
     * 响应值
     */
    public static final String RETURN_CODE_SUCCESS = "0";
    public static final String RETURN_CODE_FAIL = "-999";
    public static final String KIND = "kind";

    /**
     * 这个是特殊的，表示1/1000 byte
     */
    public static final String SMALLM = "m";

    /**
     * 1000进制
     */
    public static final int NUM_THOUSAND = 1000;

    /**
     * 存储的单位进制
     */
    public static final int NUM_SIZE_MEMORY = 1024;
    public static final Integer TWO_THOUSAND = 2000;
}
