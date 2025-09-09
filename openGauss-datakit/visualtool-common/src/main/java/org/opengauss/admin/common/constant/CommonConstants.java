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
 * CommonConstants.java
 *
 * IDENTIFICATION
 * openGauss-datakit/visualtool-common/src/main/java/org/opengauss/admin/common/constant/CommonConstants.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.common.constant;

/**
 * CommonConstants
 *
 * @author wangchao
 * @since 2024/10/29 09:26
 */
public interface CommonConstants {

    /**
     * host root username
     */
    String ROOT_USER = "root";

    /**
     * line split \n
     */
    String LINE_SPLITTER = "\n";

    /**
     * datakit sys_setting ext param :datakit server host name
     */
    String SETTING_SERVER_HOST_NAME = "datakit_server_host";

    /**
     * admin user id
     */
    Integer ADMIN_USER_ID = 1;
}
