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
 * TaskOperate.java
 *
 * IDENTIFICATION
 * data-migration/src/main/java/org/opengauss/admin/plugin/enums/TaskOperate.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.plugin.enums;

/**
 * main task status
 *
 * @author xielibo
 */
public enum TaskOperate {

    RUN(1, "run"),
    STOP_INCREMENTAL(2, "stop_incremental"),
    START_REVERSE(3, "start_reverse"),
    FINISH_MIGRATION(100, "finish_migration"),
    ;

    private final Integer code;
    private final String command;

    TaskOperate(Integer code, String command) {
        this.code = code;
        this.command = command;
    }

    public Integer getCode() {
        return code;
    }

    public String getCommand() {
        return command;
    }

}
