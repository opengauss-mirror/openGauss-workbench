/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.admin.common.enums.ops;


/**
 * OpsHostPortUsedStatusEnum
 *
 * @author wangchao
 * @since 2024/06/15 09:26
 */
public enum OpsHostPortUsedStatusEnum {
    /**
     * not used
     */
    NO_USED("NO_USED"),

    /**
     * database used
     */
    DATABASE_USED("DATABASE_USED"),

    /**
     * database cm server used
     */
    DATABASE_CM_USED("DATABASE_CM_USED"),

    /**
     * host server used
     */
    HOST_USED("HOST_USED");

    private String status;

    OpsHostPortUsedStatusEnum(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
