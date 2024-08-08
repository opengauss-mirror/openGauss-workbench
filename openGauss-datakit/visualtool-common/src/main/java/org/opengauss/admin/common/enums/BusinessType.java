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
 * BusinessType.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/enums/BusinessType.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.common.enums;

/**
 * Business Type
 *
 * @author xielibo
 */
public enum BusinessType {
    /**
     * other
     */
    OTHER,

    /**
     * insert
     */
    INSERT,

    /**
     * update
     */
    UPDATE,

    /**
     * delete
     */
    DELETE,

    /**
     * grant
     */
    GRANT,

    /**
     * start
     */
    START,
    
    /**
     * stop
     */
    STOP,

    /**
     * install
     */
    INSTALL,

    /**
     * uninstall
     */
    UNINSTALL,
    /**
     * reset
     */
    RESET,
    /**
     * confirm
     */
    CONFIRM,
    /**
     * check_environment
     */
    CHECK_ENVIRONMENT,
    /**
     * batch_install
     */
    BATCH_INSTALL,
    /**
     * re install
     */
    RE_INSTALL,
    /**
     * copy
     */
    COPY
}
