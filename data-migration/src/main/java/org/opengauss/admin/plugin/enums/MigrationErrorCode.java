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
 * MigrationErrorCode.java
 *
 * IDENTIFICATION
 * data-migration/src/main/java/org/opengauss/admin/plugin/enums/MigrationErrorCode.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.plugin.enums;

/**
 * migration error code
 *
 * @author xielibo
 */
public enum MigrationErrorCode {

    MAIN_TASK_NOT_EXISTS_ERROR(50101, "main task not exists error"),
    MAIN_TASK_IS_RUNNING_ERROR(50102, "main task is running"),


    SUB_TASK_NOT_EXISTS_ERROR(50151, "task not exists error"),
    SUB_TASK_NOT_IN_INCREMENTAL_ERROR(50152, "task status is not incremental"),
    SUB_TASK_NOT_IN_INCREMENTAL_STOP_ERROR(50153, "task status is not incremental stop"),
    SUB_TASK_NOT_CONDITIONS_REVERSE_ERROR(50154, "target database does not meet the conditions for reverse migration"),
    SUB_TASK_NOT_SUPPORT_REVERSE_ERROR(50155, "target database not support reverse migration"),

    PORTAL_INSTALL_ERROR(50120, "portal install error"),
    PORTAL_INSTALL_PATH_NOT_HAS_WRITE_PERMISSION_ERROR(50121, "portal installPath not has write permission error"),

    PORTAL_DELETE_ERROR(50122, "cannot delete portal while task is running on it"),
    PORTAL_CREATE_INSTALL_PATH_FAILED(50123, "create portal installPath failed: permission denied");

    private final Integer code;
    private final String msg;

    MigrationErrorCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

}
